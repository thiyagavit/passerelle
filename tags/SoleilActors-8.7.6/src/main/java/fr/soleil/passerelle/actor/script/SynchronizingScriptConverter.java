package fr.soleil.passerelle.actor.script;

import java.util.Iterator;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageInputContext;

import fr.soleil.passerelle.util.ExceptionUtil;

/**
 * An actor that synchronizes the messages on all input ports, and then offers them to a script. It can have a configurable nr of input and output ports. The
 * input ports are all single-channel.
 * 
 * @author erwin.de.ley@isencia.be
 */
public class SynchronizingScriptConverter extends DynamicPortScriptConverter {
    private static final long serialVersionUID = 2264919642605026024L;

private static Logger logger = LoggerFactory.getLogger(SynchronizingScriptConverter.class);

  private BSFManager scriptManager = new BSFManager();

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public SynchronizingScriptConverter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    if (hasEqualNumberOfInputAndOutputPorts()) {
      // remove separate parameter for nr of output ports
      if (numberOfOutputs != null) {
        numberOfOutputs.setContainer(null);
        numberOfOutputs = null;
      }
      if (numberOfInputs != null) {
        numberOfInputs.setName("Nr of ports");
      }
    }
    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"15\" "
        + "style=\"stroke-width:3.0\"/>\n" +

        "<line x1=\"-15\" y1=\"0\" x2=\"-1\" y2=\"0\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"-5\" y1=\"-3\" x2=\"-1\" y2=\"0\" "
        + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"-5\" y1=\"3\" x2=\"-1\" y2=\"0\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
        + "<line x1=\"-15\" y1=\"-10\" x2=\"-1\" y2=\"-10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
        + "<line x1=\"-5\" y1=\"-13\" x2=\"-1\" y2=\"-10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"-5\" y1=\"-7\" x2=\"-1\" y2=\"-10\" "
        + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"-15\" y1=\"10\" x2=\"-1\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
        + "<line x1=\"-5\" y1=\"7\" x2=\"-1\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"-5\" y1=\"13\" x2=\"-1\" y2=\"10\" "
        + "style=\"stroke-width:1.0;stroke:red\"/>\n" +

        "<line x1=\"1\" y1=\"0\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"-3\" x2=\"15\" y2=\"0\" "
        + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"3\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
        + "<line x1=\"1\" y1=\"-10\" x2=\"15\" y2=\"-10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"-13\" x2=\"15\" y2=\"-10\" "
        + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"-7\" x2=\"15\" y2=\"-10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
        + "<line x1=\"1\" y1=\"10\" x2=\"15\" y2=\"10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"7\" x2=\"15\" y2=\"10\" "
        + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"13\" x2=\"15\" y2=\"10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
        + "</svg>\n");

  }

  @Override
  public void doInitialize() throws InitializationException {
    super.doInitialize();
    if (!isMockMode()) {
      for (final Port port : this.getInputPorts()) {
        port.setExpectedMessageContentType(String.class);
      }
      // small soleil code to limit usage of scripts
      if (script.contains("import fr.esrf.Tango") || script.contains("PyTango")) {
          ExceptionUtil.throwInitializationException("Tango is not allowed", this);
      }
    }
  }

  /**
   * Overridable method to decide whether there should be 1 or 2 parameters to set the nr of input/output ports. <br/>
   * If an equal nr of input & output ports is OK (default case), only 1 parameter is shown.
   * 
   * @return flag indicating whether this Synch... implementation uses an equal nr of input and output ports.<br/>
   *         Remark that this method is invoked in the constructor, so its implementation should not depend on the instance being completely constructed!<br/>
   *         It should just contain a hard-coded true or false!!
   */
  protected boolean hasEqualNumberOfInputAndOutputPorts() {
    return false;
  }

  @Override
  protected String getExtendedInfo() {
    return "";
  }

  @Override
  protected void initializeScriptingEngines() {
    scriptManager = new BSFManager();
    scriptManager.setClassLoader(this.getClass().getClassLoader());
    bsfManagers.add(scriptManager);
  }

  @Override
  protected void changeNumberOfPorts(int newPortCount, int currPortCount, PortType portType) throws IllegalActionException, IllegalArgumentException {
    if (hasEqualNumberOfInputAndOutputPorts()) {
      // Since we only enabled the input port count cfg parameter,
      // each change in the nr of input ports will pass here once.
      // And then the code below ensures that the output port count remains
      // the same as the input port count.
      super.changeNumberOfPorts(newPortCount, currPortCount, PortType.INPUT);
      super.changeNumberOfPorts(newPortCount, currPortCount, PortType.OUTPUT);
      nrOutputPorts = newPortCount;
      nrInputPorts = newPortCount;
    } else {
      super.changeNumberOfPorts(newPortCount, currPortCount, portType);
    }
  }

  /**
   * Return PULL mode for all input ports, so the fire loop blocks till all ports have received an input msg.
   */
  @Override
  protected PortMode getPortModeForNewInputPort(String portName) {
    return PortMode.PULL;
  }

  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " process() - entry - request : " + request);
    }

    // build composite container to transport all input messages into the script
    MultiMessageFlowElement messages = new MultiMessageFlowElement(getNrInputPorts(), getNrOutputPorts());
    Iterator<MessageInputContext> allInputContexts = request.getAllInputContexts();
    while (allInputContexts.hasNext()) {
      MessageInputContext messageInputContext = allInputContexts.next();
      int portIndex = getPortIndex(messageInputContext);
      messages.addInputSpec(portIndex, messageInputContext.getMsg());
    }

    if (isMockMode()) {
      int i = 0;
      try {
        for (Port port : getOutputPorts()) {
          sendOutputMsg(port, createMessage(new Integer(i).toString(), ManagedMessage.objectContentType));
          i++;
        }
      } catch (MessageException e) {
          ExceptionUtil.throwProcessingException(getName() + " - OUTPUT PORT ERROR - Error creating output message",this,e);
      }
    } else {
      processMessages(messages);

      // now send out results to all specified outputs
      MultiMessageFlowElement.MessageAndPort[] outputSpecs = messages.getAllOutputSpecs();
      for (int i = 0; i < outputSpecs.length; i++) {
        MultiMessageFlowElement.MessageAndPort msgAndPort = outputSpecs[i];
        Port outputPort = getOutputPorts().get(msgAndPort.portNr);
        try {
          sendOutputMsg(outputPort, msgAndPort.message);
        } catch (IllegalArgumentException e) {
            ExceptionUtil.throwProcessingException(getName() + " - process() generated exception " + e,msgAndPort.message,e);
        }
      }
    }

    if (logger.isTraceEnabled())
      logger.trace(getName() + " doFire() - exit");
  }

  private void processMessages(MultiMessageFlowElement messages) throws ProcessingException {
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " processMessage() - entry - input : " + messages);
    }
    if (messages != null) {
      try {
        scriptManager.declareBean(containerName, messages, messages.getClass());
        scriptManager.exec(language, scriptPath, -1, -1, script);
      } catch (BSFException e) {
          ExceptionUtil.throwProcessingException(e.getMessage(),scriptPath,e);
      }
    }
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " processMessage() - exit");
    }
  }
}
