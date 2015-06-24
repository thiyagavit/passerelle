/*
 * (c) Copyright 2004, iSencia NV Belgium
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia NV, Belgium.  
 * Use is subject to license terms.
 */
package fr.soleil.passerelle.actor.script;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.IOUtils;
import org.python.core.Py;
import org.python.core.PySystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.dynaport.DynamicPortsActor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageInputContext;

import fr.soleil.passerelle.util.ExceptionUtil;

/**
 * A DynamicPortsActor that feeds each incoming message, from one of the dynamically configured input ports, to a common script. The script can create an
 * arbitrary number of output messages, each targeted to one of the dynamically configured output ports. Since Passerelle v4.1.1 this actor is based on the new
 * Actor API implemented in {@link be.isencia.passerelle.actor.v3.Actor}. Remark that when executing Jython scripts within an OSGi environment (such as the
 * Passerelle Manager), all imported Java packages must be set explicitly on the Jython engine before the script is executed. For the moment, this scripting
 * actor only adds the be.isencia.passerelle.message package where the Passerelle messaging classes are located (e.g. MessageFlowElement and MessageFactory
 * etc). To support Jython scripts that need to import other Java packages, a mechanism must be devised to get that working within OSGi. E.g. parse the script
 * to find all used imports, or add an actor parameter where the user must manually declare all imports etc.
 * 
 * @author erwin
 */
@SuppressWarnings("serial")
public class DynamicPortScriptConverter extends DynamicPortsActor {

  private static final String JAVASCRIPT = "javascript";
  private static final String JYTHON = "jython";
  // Static variables/initializers 
  public static final String PATH_PARAM = "Path";
  public static final String LANGUAGE_PARAM = "Language";
  public static final String CONTAINERNAME_PARAM = "Container name";

  private static Logger logger = LoggerFactory.getLogger(DynamicPortScriptConverter.class);

  // Instance variables 
  // bsf managers
  protected List<BSFManager> bsfManagers = new ArrayList<BSFManager>();

  protected String script = null;

  public Parameter languageParam = null;

  protected String language = "";

  public Parameter containerNameParam = null;

  protected String containerName = "";

  public FileParameter scriptPathParam = null;

  protected String scriptPath = null;

  // Constructors 

  /**
   * Construct an actor in the specified container with the specified name.
   * 
   * @param container The container.
   * @param name The name of this actor within the container.
   * @exception IllegalActionException If the actor cannot be contained by the proposed container.
   * @exception NameDuplicationException If the name coincides with an actor already in the container.
   */
  public DynamicPortScriptConverter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    // Create the parameters
    scriptPathParam = new FileParameter(this, PATH_PARAM);
    languageParam = new StringParameter(this, LANGUAGE_PARAM);
    languageParam.setExpression(JYTHON);
    languageParam.addChoice(JYTHON);
    languageParam.addChoice(JAVASCRIPT);

    containerNameParam = new StringParameter(this, CONTAINERNAME_PARAM);
    containerNameParam.setExpression("container");

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<circle cx=\"0\" cy=\"0\" r=\"10\""
        + "style=\"fill:white;stroke-width:2.0\"/>\n" + "<line x1=\"-15\" y1=\"0\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"12\" y1=\"-3\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"12\" y1=\"3\" x2=\"15\" y2=\"0\" "
        + "style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
  }

  // Methods 

  /**
   * @param attribute The attribute that changed.
   * @exception IllegalActionException
   */
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " attributeChanged()- entry - attribute :" + attribute);
    }

    // Define actions to be taken when a parameter/attribute has been changed

    if (attribute == scriptPathParam) {
      try {
        scriptPath = scriptPathParam.asURL().getPath();
        logger.debug("Path changed to : " + scriptPath);
      } catch (Exception e) {
        // ignore
      }
    } else if (attribute == languageParam) {
      StringToken languageToken = (StringToken) languageParam.getToken();
      if ((languageToken != null) && (languageToken.stringValue().length() > 0)) {
        language = languageToken.stringValue();
        logger.debug("Language changed to : " + language);
      }
    } else if (attribute == containerNameParam) {
      StringToken containerNameToken = (StringToken) containerNameParam.getToken();
      if ((containerNameToken != null) && (containerNameToken.stringValue().length() > 0)) {
        containerName = containerNameToken.stringValue();
        logger.debug("Container name changed to : " + containerName);
      }
    } else {
      super.attributeChanged(attribute);
    }

    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " attributeChanged()- exit");
    }
  }

  public void doInitialize() throws InitializationException {
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doInitialize() - entry");
    }

    if (!isMockMode()) {
      // Read the script into the "script"-variable
      try {
        Reader scriptReader = new FileReader(scriptPath);
        script = IOUtils.getStringFromReader(scriptReader);
        scriptReader.close();
      } catch (IOException e) {
          ExceptionUtil.throwInitializationException("Could not read script file", this,e);
      }

      initializeScriptingEngines();

      // get Jython initialised correctly in OSGi as well
      // then we need to manually specify all packages
      // imported by the jython script for some reason
      if (JYTHON.equals(language)) {
        Py.initPython();
        PySystemState.add_package("com.isencia.passerelle.message");
        PySystemState.add_package("fr.soleil.passerelle.actor.script");
      }
    }

    // Beware: contrary to initialization conventions, where the superclass
    // initialization is done BEFORE the subclass, here it is necessary
    // to initialize the subclass first, as the baseclass initialization
    // launches new threads that could bypass the current initialization thread,
    // call methods that have been implemented/overriden in the subclass
    // and thus break stuff in here...
    super.doInitialize();

    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doInitialize() - exit");
    }
  }

  /**
   * Overridable method to initialize a number of scripting engines. By default, we create a scripting engine per input port, as this actor will invoke the
   * script concurrently for each separate received input msg.
   */
  protected void initializeScriptingEngines() {
    for (int i = 0; i < getNrInputPorts(); ++i) {
      BSFManager mgr = new BSFManager();
      mgr.setClassLoader(this.getClass().getClassLoader());
      bsfManagers.add(mgr);
    }
  }

  protected String getExtendedInfo() {
    return scriptPath;
  }

  public void doWrapUp() throws TerminationException {
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doWrapUp() - entry");
    }
    for (Iterator<BSFManager> iter = bsfManagers.iterator(); iter.hasNext();) {
      BSFManager element = iter.next();
      element.terminate();
    }
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doWrapUp() - exit");
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " process() - entry - request : " + request);
    }
    try {
      Iterator<MessageInputContext> allInputContexts = request.getAllInputContexts();
      while (allInputContexts.hasNext()) {
        MessageInputContext messageInputContext = (MessageInputContext) allInputContexts.next();
        if (!messageInputContext.isProcessed()) {
          int portIndex = getPortIndex(messageInputContext);
          ManagedMessage msg = messageInputContext.getMsg();
          MessageFlowElement input = new MessageFlowElement(new MessageFlowElement.MessageAndPort(portIndex, msg), getNrOutputPorts());
          BSFManager mgr = bsfManagers.get(portIndex);
          mgr.declareBean(containerName, input, input.getClass());
          mgr.exec(language, scriptPath, -1, -1, script);
          // now send out results to all specified outputs
          MessageFlowElement.MessageAndPort[] outputSpecs = input.getAllOutputSpecs();
          for (int i = 0; i < outputSpecs.length; i++) {
            MessageFlowElement.MessageAndPort msgAndPort = outputSpecs[i];
            Port outputPort = (Port) getOutputPorts().get(msgAndPort.portNr);
            response.addOutputMessage(outputPort, msgAndPort.message);
          }
        }
      }
    } catch (BSFException e) {
        ExceptionUtil.throwProcessingException(e.getMessage(), scriptPath, e);
    }
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " process() - exit");
    }
  }

  // TODO : remove this once Soleil upgrades to Passerelle 8.2.
  // the method is present on the DynamicPortsActor in there, as well.
  protected int getPortIndex(final MessageInputContext messageInputContext) {
    String portName = messageInputContext.getPortName();
    String portIndexStr = portName.substring("input".length());
    int portIndex = Integer.parseInt(portIndexStr);
    return portIndex;
  }

}