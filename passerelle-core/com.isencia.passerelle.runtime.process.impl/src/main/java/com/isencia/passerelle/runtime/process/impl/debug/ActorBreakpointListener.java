package com.isencia.passerelle.runtime.process.impl.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.FiringEvent.FiringEventType;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;

public class ActorBreakpointListener implements DebugListener {
  private final static Logger LOGGER = LoggerFactory.getLogger(ActorBreakpointListener.class);
  private final static FiringEventType BREAKPOINT_EVENT_TYPE = FiringEvent.BEFORE_FIRE;

  @Override
  public void event(DebugEvent event) {
    if (event instanceof FiringEvent) {
      FiringEvent fe = (FiringEvent) event;
      if (BREAKPOINT_EVENT_TYPE.equals(fe.getType())) {
        Actor a = fe.getActor();
        LOGGER.info("Suspend on breakpoint {}", a.getFullName());
        a.getManager().pauseOnBreakpoint(a.getFullName());
      }
    }
  }

  @Override
  public void message(String message) {
    // ignore in this listener
  }
}
