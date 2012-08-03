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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.director.PasserelleDirector;
import com.isencia.passerelle.domain.et.FlowExecutionEvent.FlowExecutionEventType;
import com.isencia.passerelle.domain.et.impl.ETReceiver;
import com.isencia.passerelle.domain.et.impl.FireEventHandler;
import com.isencia.passerelle.domain.et.impl.FlowEventHandler;
import com.isencia.passerelle.domain.et.impl.SendEventHandler;
import com.isencia.passerelle.domain.et.impl.SimpleEventDispatcher;
import com.isencia.passerelle.domain.et.impl.ThreadPoolEventDispatcher;
import com.isencia.passerelle.ext.DirectorAdapter;

/**
 * @author delerw
 */
@SuppressWarnings("serial")
public class ETDirector extends Director implements EventDispatchReporter, PasserelleDirector {
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
  private Map<Object, Actor> busyTaskActors = new ConcurrentHashMap<Object, Actor>();
  // Map maintaining which actors are currently iterating, and for which triggering event
  private Map<Actor, Event> busyIteratingActors = new ConcurrentHashMap<Actor, Event>();

  public Parameter dispatchThreadsParameter;
  public Parameter dispatchTimeoutParameter;

  public Parameter dumpEventsParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public ETDirector(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    dispatchThreadsParameter = new Parameter(this, "Nr of dispatch threads", new IntToken(1));
    dispatchTimeoutParameter = new Parameter(this, "Dispatch timeout(ms)", new IntToken(1000));
    
    dumpEventsParameter = new Parameter(this, "Dump eventlog after run", BooleanToken.FALSE);
    new CheckBoxStyle(dumpEventsParameter, "check");
    
    _attachText(
        "_iconDescription",
        "<svg>\n"
          + "<polygon points=\"-20,0 -10,-18 10,-18 20,0 10,18 -10,18\" "
          + "style=\"fill:red;stroke:red\"/>\n"
          + "<line x1=\"-9.5\" y1=\"17\" x2=\"-19\" y2=\"0\" "
          + "style=\"stroke-width:1.0;stroke:white\"/>\n"
          + "<line x1=\"-19\" y1=\"0\" x2=\"-9.5\" y2=\"-17\" "
          + "style=\"stroke-width:1.0;stroke:white\"/>\n"
          + "<line x1=\"-9\" y1=\"-17\" x2=\"9\" y2=\"-17\" "
          + "style=\"stroke-width:1.0;stroke:white\"/>\n"
          + "<line x1=\"10\" y1=\"-17.5\" x2=\"20\" y2=\"0\" "
          + "style=\"stroke-width:1.0;stroke:black\"/>\n"
          + "<line x1=\"20\" y1=\"0\" x2=\"10\" y2=\"17.5\" "
          + "style=\"stroke-width:1.0;stroke:black\"/>\n"
          + "<line x1=\"10\" y1=\"17.5\" x2=\"-10\" y2=\"17.5\" "
          + "style=\"stroke-width:1.0;stroke:black\"/>\n"
          + "<line x1=\"11\" y1=\"-15\" x2=\"19\" y2=\"0\" "
          + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
          + "<line x1=\"19\" y1=\"0\" x2=\"11\" y2=\"16\" "
          + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
          + "<line x1=\"10\" y1=\"17\" x2=\"-9\" y2=\"17\" "
          + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
          + 

      // director stand
      "<line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"10\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"-6\" y1=\"10\" x2=\"6\" y2=\"10\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<polygon points=\"-8,0 -6,-8 8,-8 6,0\" "
        + "style=\"fill:lightgrey\"/>\n"
        + 

      //magic wand
      "<line x1=\"5\" y1=\"-15\" x2=\"15\" y2=\"-5\" "
        + "style=\"stroke-width:2.0;stroke:black\"/>\n"
        + "<line x1=\"5\" y1=\"-15\" x2=\"6\" y2=\"-14\" "
        + "style=\"stroke-width:2.0;stroke:white\"/>\n"
        + 
      // sparkles
      "<circle cx=\"12\" cy=\"-16\" r=\"1\""
        + "style=\"fill:black;stroke:white\"/>\n"
        + "<circle cx=\"16\" cy=\"-16\" r=\"1\""
        + "style=\"fill:black;stroke:white\"/>\n"
        + "<circle cx=\"14\" cy=\"-14\" r=\"1\""
        + "style=\"fill:black;stroke:white\"/>\n"
        + "</svg>\n");
  }

  @SuppressWarnings("unchecked")
  @Override
  public void preinitialize() throws IllegalActionException {
    super.preinitialize();

    int threadCount = ((IntToken) dispatchThreadsParameter.getToken()).intValue();
    
    List<EventHandler> eventHandlers = new ArrayList<EventHandler>();
    eventHandlers.add(new SendEventHandler(this));
    eventHandlers.add(new FireEventHandler(this));
    eventHandlers.add(new FlowEventHandler(this));
    // add any extra configured handlers
    eventHandlers.addAll(attributeList(EventHandler.class));
    
    if (threadCount > 1) {
      dispatcher = new ThreadPoolEventDispatcher(getFullName(), threadCount, eventHandlers.toArray(new EventHandler[eventHandlers.size()]));
    } else {
      dispatcher = new SimpleEventDispatcher(getFullName(), eventHandlers.toArray(new EventHandler[eventHandlers.size()]));
    }
    dispatcher.initialize();
    dispatchReporter = (EventDispatchReporter) dispatcher;
    notDone = true;
    inactiveActors.clear();
    busyTaskActors.clear();
  }

  @Override
  public void initialize() throws IllegalActionException {
    try {
      enqueueEvent(new FlowExecutionEvent((CompositeActor) getContainer(), FlowExecutionEventType.START));
    } catch (EventRefusedException e) {
      LOGGER.warn("Internal status error - refused flow start event",e);
    }
    super.initialize();
  }

  @Override
  public boolean prefire() throws IllegalActionException {
    return super.prefire();
  }

  @Override
  public synchronized void fire() throws IllegalActionException {
    try {
      int timeout = ((IntToken) dispatchTimeoutParameter.getToken()).intValue();
      notDone = dispatcher.dispatch(timeout) || (busyTaskActors.size() > 0);
    } catch (Exception e) {
      throw new IllegalActionException(this, e, "Error during dispatching of events");
    }
  }

  @Override
  public boolean postfire() throws IllegalActionException {
    return notDone && super.postfire();
  }

  @Override
  public void wrapup() throws IllegalActionException {
    try {
      enqueueEvent(new FlowExecutionEvent((CompositeActor) getContainer(), FlowExecutionEventType.FINISH));
      // TODO a bit annoying this explicit dispatch call, but as fire-loop is done we need to repeat it one last time here; isn't there a better way???
      dispatcher.dispatch(10);
    } catch (EventRefusedException e) {
      LOGGER.warn("Internal status error - refused flow finish event",e);
    } catch (InterruptedException e) {
      LOGGER.warn("Internal status error - interrupted flow finish event",e);
    }
    super.wrapup();
    
    if(((BooleanToken)dumpEventsParameter.getToken()).booleanValue()) {
      StringBuilder eventLog = new StringBuilder();
      for (Event event : getEventHistory()) {
        eventLog.append(event+"\n");
      }
      LOGGER.info("{} - event log :\n{}",getFullName(),eventLog.toString());
    }

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
    if (dispatcher != null)
      dispatcher.accept(event);
  }

  public void notifyActorIteratingForEvent(Actor actor, Event event) {
    Event _e = busyIteratingActors.get(actor);
    if (_e == null) {
      busyIteratingActors.put(actor, event);
    } else if (_e == event) {
      // do nothing, already registered
    } else {
      throw new IllegalArgumentException("Actor " + actor.getFullName() + " iterating other event " + _e + " i.o. given event " + event);
    }
  }

  public void notifyActorDoneIteratingForEvent(Actor actor, Event event) {
    Event _e = busyIteratingActors.get(actor);
    if (event == _e) {
      busyIteratingActors.remove(actor);
    } else {
      throw new IllegalArgumentException("Actor " + actor.getFullName() + " iterating other event " + _e + " i.o. given event " + event);
    }
  }

  public boolean isActorIterating(Actor actor) {
    return busyIteratingActors.get(actor) != null;
  }

  public void notifyActorStartedTask(Actor actor, Object task) {
    busyTaskActors.put(task, actor);
    // TODO : could be interesting to generate events for this?
    // enqueueEvent(new TaskStartedEvent(task, actor));
  }

  /**
   * @param actor
   * @param task
   * @throws IllegalStateException when the given task is not registered as busy for the given actor.
   */
  public void notifyActorFinishedTask(Actor actor, Object task) throws IllegalStateException {
    if (actor == busyTaskActors.get(task)) {
      busyTaskActors.remove(task);
    } else {
      throw new IllegalArgumentException("Task " + task + "not found for actor " + actor.getFullName());
    }
    // TODO : could be interesting to generate events for this?
    // enqueueEvent(new TaskFinishedEvent(task, actor));
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

  public DirectorAdapter getAdapter(String adapterName) throws IllegalActionException {
    return DirectorUtils.getAdapter(this, adapterName);
  }
}
