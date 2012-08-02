/* Copyright 2011 - iSencia Belgium NV

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

package com.isencia.passerelle.domain.et.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.domain.et.Event;
import com.isencia.passerelle.domain.et.EventHandler;

/**
 * A basic implementation of an event dispatcher, based on a BlockingQueue and a single thread for dispatching queued events. Should become configurable for
 * multiple threads as well. NOT YET READY FOR USE! Still need to define behaviour ico multiple threads, with external control on when to initiate
 * shutdown/wrapup.
 * 
 * @author delerw
 */
public class ThreadPoolEventDispatcher extends SimpleEventDispatcher {

  private final static Logger LOGGER = LoggerFactory.getLogger(ThreadPoolEventDispatcher.class);

  // theadpool for the eventQSinks
  private ExecutorService queueDepletionExecutor;

  public ThreadPoolEventDispatcher(String name, int threadCount, EventHandler... handlers) {
    super(name, handlers);

    queueDepletionExecutor = Executors.newFixedThreadPool(threadCount);
  }

  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  public boolean dispatch(long timeOut) throws InterruptedException {
    int evtCount = getPendingEventCount();
    boolean hasDispatchedSomething = false;
    if (evtCount > 0) {
      CompletionService<Boolean> execComplSvc = new ExecutorCompletionService<Boolean>(queueDepletionExecutor);
      for (int i = 0; i < evtCount; ++i) {
        execComplSvc.submit(new EventQueueSink(timeOut));
      }
      for (int i = 0; i < evtCount; ++i) {
        try {
          // Watch out! Order of the RHS is important.
          // The ...get() must come first as we want to ensure that we wait for ALL tasks to have completed.
          // If it would be at the end, the Java logical expression evaluation would no longer execute it once
          // the hasDispatchedSomething has become true!
          hasDispatchedSomething = execComplSvc.take().get() || hasDispatchedSomething;
        } catch (ExecutionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    getLogger().debug(this + " hasDispatchedSomething " + hasDispatchedSomething);
    return hasDispatchedSomething;
  }

  private class EventQueueSink implements Callable<Boolean> {
    private long timeout;

    public EventQueueSink(long timeout) {
      this.timeout = timeout;
    }

    public Boolean call() throws Exception {
      String name = getName() + " - " + Thread.currentThread().getName();
      getLogger().debug("Starting EventQueueSink {}", name);
      try {
        boolean hasDispatchedSomething = ThreadPoolEventDispatcher.super.dispatch(1000);
        getLogger().debug(name + " hasDispatchedSomething " + hasDispatchedSomething);
        return hasDispatchedSomething;
      } finally {
        getLogger().debug("Terminating EventQueueSink {}", name);
      }
    }
  }
}
