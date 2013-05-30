package com.isencia.passerelle.runtime.process.impl.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPortEvent;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;

public class PortBreakpointListener implements DebugListener {
  private final static Logger LOGGER = LoggerFactory.getLogger(PortBreakpointListener.class);
  private static final int BREAKPOINT_EVENT_TYPE_INPUT_PORT = IOPortEvent.GET_END;
  private static final int BREAKPOINT_EVENT_TYPE_OUTPUT_PORT = IOPortEvent.SEND;

  @Override
  public void event(DebugEvent event) {
    if (event instanceof IOPortEvent) {
      IOPortEvent pe = (IOPortEvent) event;
      if ((BREAKPOINT_EVENT_TYPE_INPUT_PORT == pe.getEventType()) 
          || (BREAKPOINT_EVENT_TYPE_OUTPUT_PORT == pe.getEventType())) {
        Port p = pe.getPort();
        LOGGER.info("Suspend on breakpoint {}", p.getFullName());
        ((CompositeActor)p.toplevel()).getManager().pauseOnBreakpoint(p.getFullName());
      }
    }
  }

  @Override
  public void message(String message) {
    // ignore in this listener
  }
}
