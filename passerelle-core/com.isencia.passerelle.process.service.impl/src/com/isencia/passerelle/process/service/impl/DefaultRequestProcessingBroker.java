/* Copyright 2013 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.isencia.passerelle.process.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.process.service.RequestProcessingBroker;
import com.isencia.passerelle.process.service.RequestProcessingService;

/**
 * A simple default implementation in case no custom implementations get
 * registered as service impl.
 * 
 * @author erwin
 */
public class DefaultRequestProcessingBroker implements RequestProcessingBroker {

  private final static RequestProcessingBroker INSTANCE = new DefaultRequestProcessingBroker();

  public static RequestProcessingBroker getInstance() {
    return INSTANCE;
  }

  private Set<RequestProcessingService> services = new HashSet<RequestProcessingService>();

  private static ScheduledExecutorService delayTimer = Executors.newSingleThreadScheduledExecutor();

  private DefaultRequestProcessingBroker() {
  }

  @Override
  public Future<Request> process(Request request, Long timeout, TimeUnit unit) throws ProcessingException {
    // Get timeout handling working before accessing the services
    // to make sure that bad/blocking service implementations don't interfere
    // with it.
    registerTimeOutHandler(request, timeout, unit);

    Future<Request> futResult = null;
    for (RequestProcessingService service : services) {
      futResult = service.process(request, timeout, unit);
      if (futResult != null) {
        break;
      }
    }
    if (futResult != null) {
      return futResult;
    } else {
      throw new ProcessingException(ErrorCode.TASK_UNHANDLED, "No service found for " + request, null, null);
    }
  }

  private void registerTimeOutHandler(final Request request, Long timeout, TimeUnit unit) {
    if (timeout == null || unit == null || (timeout <= 0)) {
      return;
    }
    delayTimer.schedule(new TimeoutHandler(request.getProcessingContext().getProcessId(), request.getId()), timeout, unit);
  }

  @Override
  public boolean registerService(RequestProcessingService service) {
    return services.add(service);
  }

  @Override
  public boolean removeService(RequestProcessingService service) {
    return services.remove(service);
  }

  @Override
  public void clearServices() {
    services.clear();
  }

  public static final class TimeoutHandler implements Callable<Void> {
    private final String processID;
    private final Long taskID;

    public TimeoutHandler(String processID, Long taskID) {
      this.processID = processID;
      this.taskID = taskID;
    }

    public Void call() {
      ProcessManager procMgr = ProcessManagerServiceTracker.getService().getProcessManager(processID);
      if (procMgr != null) {
        Task task = procMgr.getTask(taskID);
        if (task!=null && !task.getProcessingContext().isFinished()) {
          procMgr.notifyTimeOut(task);
        }
      }
      return null;
    }
  }
}
