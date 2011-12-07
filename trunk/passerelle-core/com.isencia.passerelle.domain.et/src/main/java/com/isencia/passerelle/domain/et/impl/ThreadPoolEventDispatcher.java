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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic implementation of an event dispatcher, based on a BlockingQueue 
 * and a single thread for dispatching queued events. Should become configurable for multiple threads as well.
 * 
 * NOT YET READY FOR USE! 
 * Still need to define behaviour ico multiple threads, with external control on when to initiate shutdown/wrapup.
 * 
 * @author delerw
 */
public class ThreadPoolEventDispatcher extends SimpleEventDispatcher {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(ThreadPoolEventDispatcher.class);
  
  // 1-thread executor for the eventQSink
  private ExecutorService queueDepletionExecutor;

  public ThreadPoolEventDispatcher(String name) {
    super(name);
    
    queueDepletionExecutor = Executors.newSingleThreadExecutor();
    try {
      queueDepletionExecutor.execute(new EventQueueSink());
    } catch (RejectedExecutionException e) {
      // should not happen
      LOGGER.error("Failure to launch queueDepletionExecutor",e);
    }
  }
  
  private class EventQueueSink implements Runnable {
    public void run() {
      LOGGER.info("Starting EventQueueSink for EventHandler {}", getName());
      try {
        boolean result = true;
        while(result) {
          // watch out this 1s is quite low (e.g. for backend calls)
          result = dispatch(1000);
        }
      } catch (Exception e) {
        // TODO add callback to Director, to make it aware of interrupted EventDispatcher
        LOGGER.error("EventQueueSink loop interrupted for EventHandler {}",getName(), e);
      } finally {
        LOGGER.info("Terminating EventQueueSink for EventHandler {}", getName());
      }
    }
  }
}
