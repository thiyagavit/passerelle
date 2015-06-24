package fr.soleil.passerelle.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.Manager;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.ext.FiringEventListener;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

@SuppressWarnings("serial")
public class ContextEventListener extends Attribute implements FiringEventListener {

  private static TangoAttribute attribute;

  private final static String CONTEXT_NAME = "Context name";
  private final static String CONTEXT_STRATEGY_NAME = "Context strategy";
  public static boolean contextAlive = false;
  private static String deviceName;
  private final static Logger logger = LoggerFactory.getLogger(ContextEventListener.class);

  // private static boolean stopping = false;

  public static String getDeviceName() {
    return deviceName;
  }

  public static void setDeviceName(final String deviceName) {
    ContextEventListener.deviceName = deviceName;
  }

  public ContextEventListener(final Director container, final String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    
    DirectorUtils.getAdapter(container,null).registerFiringEventListener(this);

    if (container.getAttribute(CONTEXT_NAME) == null) {
      new StringParameter(container, CONTEXT_NAME);
    }

    if (container.getAttribute(CONTEXT_STRATEGY_NAME) == null) {
      final Parameter param = new StringParameter(container, CONTEXT_STRATEGY_NAME);
      param.addChoice("ignore");
      param.addChoice("stop");
      param.addChoice("pause");
      param.setExpression("pause");
    }
  }

  private String getStringFromPtolemyAttribute(final FiringEvent event, final String attributeName) {
    final Attribute attr = event.getDirector().getAttribute(attributeName);
    StringToken st = null;
    String result = null;
    if (attr != null) {
      try {
        st = (StringToken) ((Parameter) attr).getToken();
      } catch (final IllegalActionException e1) {
        e1.printStackTrace();
      }
      result = st.stringValue();
    }
    return result;
  }

  private void initDeviceConnection(final String contextName) {
    try {
      // setter on device name added to be able to perform tests on a
      // specific BeamlineStatus
      if (deviceName == null) {
    	  deviceName = TangoAccess.getFirstDeviceExportedForClass("BeamlineStatus");
      }
      try {
        attribute = new TangoAttribute(deviceName + "/" + contextName);
        contextAlive = true;
      } catch (final DevFailed df) {
        attribute = new TangoAttribute(deviceName + "/contextValidity");
        contextAlive = true;
      }
    } catch (final DevFailed e2) {
      // Director dir =
      // (com.isencia.passerelle.domain.cap.Director)e.getDirector();
      // ExecutionTracerService.trace(dir,
      // TangoUtil.getDevFailedString(e2, dir));
      // System.err.println(TangoUtil.getDevFailedString(e2,
      // (Actor)null));
      contextAlive = false;
    }
  }

  public void onEvent(final FiringEvent event) {
    if (event.getType().equals(FiringEvent.BEFORE_FIRE)) {
      final String contextName = getStringFromPtolemyAttribute(event, CONTEXT_NAME);
      final String contextStrategy = getStringFromPtolemyAttribute(event, CONTEXT_STRATEGY_NAME);

      if (contextStrategy.compareTo("pause") == 0) {
        pause(event, contextName);
      } else if (contextStrategy.compareTo("stop") == 0) {
        stop(event, contextName);
      }// else ignore -> do nothing
    }
  }

  /**
   * check context to know if it has to pause. Synchronized because onEvent is called by several threads
   * 
   * @param e
   * @param contextName
   */
  private synchronized void pause(final FiringEvent e, final String contextName) {
    boolean contextValidity = false;
    boolean firtTurn = true;
    final CompositeActor model = (CompositeActor) e.getDirector().getContainer();
    if (model.getManager().getState() != Manager.WRAPPING_UP) {
      // if
      // prevent
      // from
      // doing
      // first
      // dowhile
      do {
        initDeviceConnection(contextName);
        if (contextAlive) {
          try {
            contextValidity = attribute.read(Boolean.class);
          } catch (final DevFailed e2) {
            logger.error(TangoToPasserelleUtil.getDevFailedString(e2, (Actor) null));
          }
        } else {
          if (firtTurn) {
            ExecutionTracerService.trace(e.getDirector(), "ERROR - context device is not alive");
          }
        }
        if (!contextValidity) {
          // System.out.println("Context invalid, waiting");
          if (firtTurn) {
            ExecutionTracerService.trace(e.getDirector(), "WAITING - CONTEXT INVALID");
            firtTurn = false;
          }
          try {
            Thread.sleep(2000);
          } catch (final InterruptedException e1) {
            System.out.println("sleep interrupted");
            break;
          }
        }
      } while (!contextValidity && model.getManager().getState() != Manager.WRAPPING_UP);
    }
  }

  private synchronized void stop(final FiringEvent e, final String contextName) {
    boolean contextValidity = false;
    final CompositeActor model = (CompositeActor) e.getDirector().getContainer();
    if (model.getManager().getState() != Manager.WRAPPING_UP) {
      initDeviceConnection(contextName);
      if (contextAlive) {
        try {
          contextValidity = attribute.read(Boolean.class);
        } catch (final DevFailed e2) {
          // System.err.println(TangoUtil.getDevFailedString(e2,
          // (Actor)null));
        }
      } else {
        ExecutionTracerService.trace(e.getDirector(), "ERROR - context device is not alive");
      }

      if (!contextValidity) {
        // stopping = true;
        ExecutionTracerService.trace(e.getDirector(), "STOPPING - CONTEXT INVALID");
        // CompositeActor model = (CompositeActor) ((Director)
        // getContainer()).getContainer();
        try {
          model.getManager().stop();
          Thread.sleep(1000);
        } catch (final Exception ex) {
          ex.printStackTrace();
          // ignore
        }
      }
    }
  }
}
