package com.isencia.passerelle.process.actor.flow;

import java.util.Map;

import org.slf4j.MDC;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.process.actor.Actor;
import com.isencia.passerelle.process.actor.ProcessRequest;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.project.repository.api.RepositoryService;

/**
 * Actor to generate a simple Request that mainly can be used as trigger for basic diagnostic sequences.
 * 
 * @author erwin
 * 
 */
public class StartActor extends Actor {

  private static final long serialVersionUID = 1L;

  public static final String APPLICATION_PARAMETERS = "Application parameters";
  public static final String MOCK_REQUEST = "Mock Request";

  public Port trigger; // NOSONAR
  public Port output; // NOSONAR

  public StringParameter applicationParameters;

  public StartActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);
    // modelSourceNameParam = new StringParameter(this,
    // "model source name");
    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:orange;stroke:orange\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<circle cx=\"0\" cy=\"0\" r=\"10\""
        + "style=\"fill:white;stroke-width:2.0\"/>\n" + "<line x1=\"-15\" y1=\"0\" x2=\"0\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"-3\" y1=\"-3\" x2=\"0\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"-3\" y1=\"3\" x2=\"0\" y2=\"0\" "
        + "style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
    applicationParameters = new StringParameter(this, APPLICATION_PARAMETERS);
    new TextStyle(applicationParameters, "paramsTextArea");
  }

  @Override
  protected ProcessManager getNonMessageBoundProcessManager() throws ProcessingException {
    try {
      NamedObj flow = toplevel();
      Parameter procMgrParameter = (Parameter) flow.getAttribute(ProcessManager.NAME_AS_ATTRIBUTE, Parameter.class);
      if (procMgrParameter != null) {
        Object o = ((ObjectToken) procMgrParameter.getToken()).getValue();
        if (o instanceof ProcessManager) {
          ProcessManager processManager = (ProcessManager) o;
          // for grouping request processing log messages
          MDC.put("requestId", processManager.getRequest().getId().toString());

          Map<String, String> systemParameterMap = getParameterMap(flow, RepositoryService.SYSTEM_PARAMETERS);
          preProcess(processManager, systemParameterMap);
          
          return processManager;
        }
      }
    } catch (Exception t) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error obtaining ProcessManager", this, t);
    }
    return super.getNonMessageBoundProcessManager();
  }

  @Override
  public void process(ProcessManager processManager, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      processManager.notifyStarted();
      ManagedMessage message = createOutputMessage(processManager.getRequest());
      response.addOutputMessage(output, message);
    } catch (Exception t) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error generating request", this, t);
    } finally {
      // should terminate after one request generation
      requestFinish();
    }
  }

  /**
   * Overridable method to apply extra logic on the processing context at start time.
   * 
   * @param processManager
   * @param systemParameterMap
   */
  protected void preProcess(ProcessManager processManager, Map<String, String> systemParameterMap) {
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> getParameterMap(NamedObj flow, String type) throws IllegalActionException {
    Attribute attribute = flow.getAttribute(type);
    Parameter parameter = (Parameter) attribute;
    if (parameter == null) {
      return null;
    }
    ObjectToken oToken = (ObjectToken) parameter.getToken();
    Object o = oToken.getValue();
    if (o instanceof Map) {
      return (Map<String, String>) o;
    }
    return null;
  }
}
