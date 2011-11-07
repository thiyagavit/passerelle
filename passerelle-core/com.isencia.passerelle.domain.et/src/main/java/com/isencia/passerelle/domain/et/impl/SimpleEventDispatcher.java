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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.domain.et.Event;
import com.isencia.passerelle.domain.et.EventDispatcher;
import com.isencia.passerelle.domain.et.EventHandler;
import com.isencia.passerelle.domain.et.EventRefusedException;

/**
 * A basic implementation of an event dispatcher, based on a BlockingQueue 
 * and a single thread for dispatching queued events.
 * 
 * @author delerw
 */
public class SimpleEventDispatcher implements EventDispatcher {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(SimpleEventDispatcher.class);
  
  private String name;
  
  private BlockingQueue<Event> eventQ = new LinkedBlockingQueue<Event>();
  private List<Event> eventHistory = new LinkedList<Event>();
  private List<Event> unhandledEvents = new LinkedList<Event>();
  
  // 1-thread executor for the eventQSink
  private ExecutorService queueDepletionExecutor;
  
  private Collection<EventHandler<? extends Event>> eventHandlers = new HashSet<EventHandler<? extends Event>>();

  // state variables for managing the shutdown sequence
  private boolean active = true;
  private boolean forcedShutdown = false;

  public SimpleEventDispatcher(String name) {
    this.name=name;
    
    eventHandlers.add(new SendEventHandler());
    eventHandlers.add(new FireEventHandler());
    
//    queueDepletionExecutor = Executors.newSingleThreadExecutor();
//    try {
//      queueDepletionExecutor.execute(new EventQueueSink());
//    } catch (RejectedExecutionException e) {
//      // should not happen
//      LOGGER.error("Failure to launch queueDepletionExecutor",e);
//    }
  }
  
  public void initialize() {
    for (EventHandler<? extends Event> evtHandler : eventHandlers) {
      evtHandler.initialize();
    }
  }
  
  @Override
  public void accept(Event e) throws EventRefusedException {
    try {
      eventQ.put(e);
    } catch (Exception e1) {
      throw new EventRefusedException(e, "Error accepting event", null, e1);
    }
  }
  
  

  @Override
  public boolean dispatch(long timeOut) {
    boolean eventDispatched = false;
    try {
      Event event = eventQ.poll(timeOut, TimeUnit.MILLISECONDS);
      if(event!=null) {
        eventHistory.add(0, event);
        for (EventHandler evtHandler : eventHandlers) {
          try {
            if(evtHandler.canHandle(event)) {
              evtHandler.handle(event);
              eventDispatched = true;
              break;
            }
          } catch (Exception e) {
            // TODO remove this logging?? The goal of the for-loop is to find a matching handler
            // the others are free to throw exceptions, and these should not polute the log files.
            LOGGER.error("",e);
          }
        }
        if(!eventDispatched) {
          unhandledEvents.add(0, event);
        }
      }
    } catch (InterruptedException e) {
      LOGGER.error("",e);
    }
    return eventDispatched;
  }
  
  public List<Event> getEventHistory() {
    return eventHistory;
  }
  
  public List<Event> getUnhandledEvents() {
    return unhandledEvents;
  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub
  }

  @Override
  public List<Event> shutdownNow() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    // TODO Auto-generated method stub
    return false;
  }

//  private class EventQueueSink implements Runnable {
//
//    @Override
//    public void run() {
//      LOGGER.info("Starting EventQueueSink for EventHandler {}", name);
//      
//      try {
//        boolean result = true;
//        while(result) {
//          // watch out this 1s is quite low (e.g. for backend calls)
//          result = dispatch(1000);
//        }
//      } catch (Exception e) {
//        // TODO add callback to Director, to make it aware of interrupted EventDispatcher
//        LOGGER.error("EventQueueSink loop interrupted for EventHandler "+name, e);
//      } finally {
//        LOGGER.info("Terminating EventQueueSink for EventHandler {}", name);
//      }
//    }
//    
//  }
}
