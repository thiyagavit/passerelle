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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.domain.et.impl.ETReceiver;
import com.isencia.passerelle.domain.et.impl.FireEventHandler;
import com.isencia.passerelle.domain.et.impl.SendEventHandler;
import com.isencia.passerelle.domain.et.impl.SimpleEventDispatcher;

/**
 * @author delerw
 *
 */
@SuppressWarnings("serial")
public class ETDirector extends Director implements EventDispatchReporter {
  private final static Logger LOGGER = LoggerFactory.getLogger(ETDirector.class);
  
  // not sure yet if this is a good idea or not,
  // to split-out a separate event dispatcher.
  // maybe easier to put the eventQ and behaviour directly in this director
  // splitting it out may allow to e.g. plugin different impls,
  // like one using OSGi event bus etc...
  private EventDispatcher dispatcher;
  private EventDispatchReporter dispatchReporter;
  
  private boolean notDone = true;
  private Set<Actor> inactiveActors = new HashSet<Actor>();
  
  // Need some collection to maintain info about busy tasks
  // i.e. for slow actions done by actors.
  // Seems interesting to store a tuple {actor, taskHandle, startTime}.
  // Probably through a dedicated Event type?
  // In models with loops or request-trains (e.g. DARE), a same actor could
  // be handling several tasks concurrently.
  // The taskHandle could be used to link to any domain-specific task entity.
  // The startTime could be used for internal CEP, timeout mgmt etc. 
  private Map<Object, Actor> busyTaskActors = new HashMap<Object, Actor>();

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public ETDirector(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
  }
  @SuppressWarnings("unchecked")
  @Override
  public void preinitialize() throws IllegalActionException {
    super.preinitialize();
    dispatcher=new SimpleEventDispatcher(getFullName(),
        new SendEventHandler(this),
        new FireEventHandler(this));
    dispatchReporter = (EventDispatchReporter) dispatcher;
    notDone = true;
    inactiveActors.clear();
  }
  @Override
  public void initialize() throws IllegalActionException {
    super.initialize();
  }
  @Override
  public boolean prefire() throws IllegalActionException {
    return super.prefire();
  }
  @Override
  public synchronized void fire() throws IllegalActionException {
    // In this prototype, we use the model execution thread,
    // which is doing the director iterations, 
    // to dispatch the oldest pending event.
    // In future "advanced" impls we will also investigate
    // event-dispatching via thread pools. 
    // This will add inherent support for (limited) actor execution concurrency.
    notDone = dispatcher.dispatch(1000) || (busyTaskActors.size()>0);
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
  
  public void enqueueEvent(Event event) throws EventRefusedException {
    if(dispatcher!=null)
      dispatcher.accept(event);
  }
  
  public synchronized void notifyActorStartedTask(Actor actor, Object task) {
    busyTaskActors.put(task, actor);
    // TODO : could be interesting to generate events for this?
//    enqueueEvent(new TaskStartedEvent(task, actor));
  }

  /**
   * 
   * @param actor
   * @param task
   * @throws IllegalStateException when the given task is not registered
   * as busy for the given actor.
   */
  public synchronized void notifyActorFinishedTask(Actor actor, Object task) throws IllegalStateException {
    if(actor==busyTaskActors.get(task)) {
      busyTaskActors.remove(task);
    } else {
      throw new IllegalArgumentException("Task "+task+"not found for actor "+actor.getFullName());
    }
    // TODO : could be interesting to generate events for this?
//    enqueueEvent(new TaskFinishedEvent(task, actor));
  }
  
  public void notifyActorInactive(Actor actor) {
    LOGGER.debug("Marking actor {} as inactive.", actor.getName());
    inactiveActors.add(actor);
  }
  
  public boolean isActorInactive(Actor actor) {
    return inactiveActors.contains(actor);
  }
  public List<Event> getEventHistory() {
    return dispatchReporter.getEventHistory();
  }
  public List<Event> getUnhandledEvents() {
    return dispatchReporter.getUnhandledEvents();
  }
  public List<EventError> getEventErrors() {
    return dispatchReporter.getEventErrors();
  }
  public void clearEvents() {
    dispatchReporter.clearEvents();
  }
}
