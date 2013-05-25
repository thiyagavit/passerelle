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
package com.isencia.passerelle.runtime.process.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.Event;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.FlowNotExecutingException;
import com.isencia.passerelle.runtime.FlowProcessingService;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.ProcessListener;
import com.isencia.passerelle.runtime.process.impl.executor.FlowExecutionFuture;
import com.isencia.passerelle.runtime.process.impl.executor.FlowExecutionTask;
import com.isencia.passerelle.runtime.process.impl.executor.FlowExecutor;

public class FlowProcessingServiceImpl implements FlowProcessingService {

  private final static Logger LOGGER = LoggerFactory.getLogger(FlowProcessingServiceImpl.class);

  private int maxConcurrentProcesses = 10;

  private Map<String, FlowExecutionFuture> flowExecutions = new ConcurrentHashMap<String, FlowExecutionFuture>();
//  private Map<FlowHandle, Set<ProcessHandle>> executionHandles = Collections.synchronizedMap(new HashMap<FlowHandle, Set<ProcessHandle>>());

  private ExecutorService flowExecutor;

  public FlowProcessingServiceImpl(int maxConcurrentProcesses) {
    LOGGER.info("Creating flow executor for {} max concurrent processes", maxConcurrentProcesses);
    this.maxConcurrentProcesses = maxConcurrentProcesses;
    flowExecutor = new FlowExecutor(maxConcurrentProcesses, maxConcurrentProcesses, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  @Override
  public ProcessHandle start(StartMode mode, FlowHandle flowHandle, String processContextId, Map<String, String> parameterOverrides, 
      ProcessListener listener, String... breakpointNames) {
    
    if (processContextId == null) {
      processContextId = UUID.randomUUID().toString();
    }

    LOGGER.debug("Context {} - Submitting execution of flow {}", processContextId, flowHandle.getCode());

    FlowExecutionTask fet = new FlowExecutionTask(mode, flowHandle, processContextId, parameterOverrides, listener, breakpointNames);
    FlowExecutionFuture fetFuture = (FlowExecutionFuture) flowExecutor.submit(fet);
    ProcessHandle procHandle = new ProcessHandleImpl(fetFuture);

    if(!procHandle.getExecutionStatus().isFinalStatus()) {
      flowExecutions.put(processContextId, fetFuture);
//    Set<ProcessHandle> procHandles = executionHandles.get(flowHandle);
//    if (procHandles == null) {
//      synchronized (executionHandles) {
//        procHandles = new HashSet<ProcessHandle>();
//        executionHandles.put(flowHandle, procHandles);
//      }
//    }
//    procHandles.add(procHandle);
    }

    return procHandle;
  }

  @Override
  public ProcessHandle addBreakpoints(ProcessHandle processHandle, String... extraBreakpoints) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessHandle removeBreakpoints(ProcessHandle processHandle, String... breakpointsToRemove) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessHandle getHandle(String processId) {
    FlowExecutionFuture fet = flowExecutions.get(processId);
    return fet!=null ? new ProcessHandleImpl(fet) : null;
  }

  @Override
  public ProcessHandle refresh(ProcessHandle processHandle) {
    FlowExecutionFuture fet = flowExecutions.get(processHandle.getProcessContextId());
    return fet!=null ? new ProcessHandleImpl(fet) : processHandle;
  }

  /**
   * Does not wait for the execution to have terminated!
   */
  @Override
  public ProcessHandle terminate(ProcessHandle processHandle) throws FlowNotExecutingException {
    FlowExecutionFuture fet = flowExecutions.get(processHandle.getProcessContextId());
    if(fet==null) {
      throw new FlowNotExecutingException(processHandle.getFlow().getCode());
    } else {
      fet.cancel(true);
      return new ProcessHandleImpl(fet);
    }
  }

  @Override
  public ProcessHandle suspend(ProcessHandle processHandle) throws FlowNotExecutingException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessHandle resume(ProcessHandle processHandle) throws FlowNotExecutingException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessHandle step(ProcessHandle processHandle) throws FlowNotExecutingException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessHandle signalEvent(ProcessHandle processHandle, Event event) throws FlowNotExecutingException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Event> getProcessEvents(ProcessHandle processHandle, int maxCount) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Event> getProcessEvents(String processId, int maxCount) {
    // TODO Auto-generated method stub
    return null;
  }

}
