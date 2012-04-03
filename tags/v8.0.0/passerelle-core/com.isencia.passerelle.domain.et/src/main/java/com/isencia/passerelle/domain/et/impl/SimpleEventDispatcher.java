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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.domain.et.Event;
import com.isencia.passerelle.domain.et.EventDispatchReporter;
import com.isencia.passerelle.domain.et.EventDispatcher;
import com.isencia.passerelle.domain.et.EventError;
import com.isencia.passerelle.domain.et.EventHandler;
import com.isencia.passerelle.domain.et.EventRefusedException;

/**
 * A basic implementation of an event dispatcher, based on a BlockingQueue 
 * and a method for dispatching the oldest queued event.
 * The method must be invoked by some external component (e.g. the model director).
 * 
 * @author delerw
 */
public class SimpleEventDispatcher implements EventDispatcher, EventDispatchReporter {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(SimpleEventDispatcher.class);
  
  private String name;
  
  private BlockingQueue<Event> eventQ = new LinkedBlockingQueue<Event>();
  private List<Event> eventHistory = new LinkedList<Event>();
  private List<Event> unhandledEvents = new LinkedList<Event>();
  private List<EventError> eventErrors = new LinkedList<EventError>();
  
  private EventHandler<? extends Event> eventHandlers[];

  // state variables for managing the shutdown sequence
  private boolean active = true;
  private boolean forcedShutdown = false;

  public SimpleEventDispatcher(String name, EventHandler<? extends Event>... handlers) {
    this.name=name;
    eventHandlers=handlers;
  }
  
  public String getName() {
    return name;
  }

  public void initialize() {
    for (EventHandler<? extends Event> evtHandler : eventHandlers) {
      evtHandler.initialize();
    }
  }
  
  public void accept(Event e) throws EventRefusedException {
    try {
      eventQ.put(e);
    } catch (Exception e1) {
      throw new EventRefusedException(e, "Error accepting event", null, e1);
    }
  }

  public boolean dispatch(long timeOut) {
    boolean eventDispatched = false;
    Event event = null;
    try {
      event = eventQ.poll(timeOut, TimeUnit.MILLISECONDS);
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
            eventErrors.add(0,new EventError(event, e));
          }
        }
      }
    } catch (InterruptedException e) {
      LOGGER.error("Event dispatch was interrupted",e);
    } finally {
      if(!eventDispatched && event!=null) {
        unhandledEvents.add(0, event);
      }
    }
    return eventDispatched;
  }
  
  public List<Event> getEventHistory() {
    return eventHistory;
  }
  
  public List<Event> getUnhandledEvents() {
    return unhandledEvents;
  }
  
  public List<EventError> getEventErrors() {
    return eventErrors;
  }
  
  public void clearEvents() {
    eventHistory.clear();
    unhandledEvents.clear();
    eventErrors.clear();
  }

  public void shutdown() {
    active=false;
  }

  public List<Event> shutdownNow() {
    shutdown();
    List<Event> pendingEvents = new ArrayList<Event>(eventQ);
    return Collections.unmodifiableList(pendingEvents);
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    if(active)
      return false;
    // TODO do something here to block till all events have been processed
    return false;
  }
}
