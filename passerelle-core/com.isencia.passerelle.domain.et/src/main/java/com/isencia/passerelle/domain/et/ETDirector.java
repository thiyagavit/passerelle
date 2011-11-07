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

package com.isencia.passerelle.domain.et;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.domain.et.impl.ETReceiver;
import com.isencia.passerelle.domain.et.impl.SimpleEventDispatcher;

/**
 * @author delerw
 *
 */
public class ETDirector extends Director {
  
  private EventDispatcher dispatcher;
  private boolean notDone = true;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public ETDirector(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
  }
  
  @Override
  public void preinitialize() throws IllegalActionException {
    super.preinitialize();
    dispatcher=new SimpleEventDispatcher(getFullName());
  }
  @Override
  public void initialize() throws IllegalActionException {
    super.initialize();
    notDone = true;
  }
  @Override
  public boolean prefire() throws IllegalActionException {
    return super.prefire();
  }
  @Override
  public void fire() throws IllegalActionException {
    notDone = dispatcher.dispatch(1000);
  }
  @Override
  public boolean postfire() throws IllegalActionException {
    return notDone && super.postfire();
  }
  @Override
  public void wrapup() throws IllegalActionException {
    super.wrapup();
    dispatcher.shutdown();
  }
  
  public void reportEvents() {
    DateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss,SSS");
    List<Event> evtList = ((SimpleEventDispatcher)dispatcher).getEventHistory();
    if(!evtList.isEmpty()) {
      System.out.println("Event History");
      for (Event event : evtList) {
        System.out.println(((AbstractEvent)event).toString(dateFmt));
      }
    }
    evtList = ((SimpleEventDispatcher)dispatcher).getUnhandledEvents();
    if(!evtList.isEmpty()) {
      System.out.println("Unhandled Events");
      for (Event event : evtList) {
        System.out.println(((AbstractEvent)event).toString(dateFmt));
      }
    }
  }

  public void enqueueEvent(Event event) throws EventRefusedException {
    if(dispatcher!=null)
      dispatcher.accept(event);
  }

  @Override
  public void fireAt(Actor actor, Time time) throws IllegalActionException {
    // TODO add time handling
    try {
      enqueueEvent(new FireEvent(actor));
    } catch (EventRefusedException e) {
      throw new IllegalActionException(actor, e, "Error enqueing fire event");
    }
  }

  @Override
  public void fireAtCurrentTime(Actor actor) throws IllegalActionException {
    try {
      enqueueEvent(new FireEvent(actor));
    } catch (EventRefusedException e) {
      throw new IllegalActionException(actor, e, "Error enqueing fire event");
    }
  }

  @Override
  public Receiver newReceiver() {
    return new ETReceiver(this);
  }
}
