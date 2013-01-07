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
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.Status;
import com.isencia.passerelle.process.model.service.ServiceRegistry;
import com.isencia.passerelle.project.repository.api.RepositoryService;

/**
 * Actor to generate a simple Request that mainly can be used as trigger for basic diagnostic sequences.
 * 
 * @author delerw
 * 
 */
public class StartActor extends Actor {

  private static final long serialVersionUID = 1L;

  public static final String APPLICATION_PARAMETERS = "Application parameters";

  public Port trigger; // NOSONAR
  public Port output; // NOSONAR

  public StringParameter applicationParameters;

  // public StringParameter modelSourceNameParam;

  // public StringParameter requestParamsParameter;

  public StartActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    output = PortFactory.getInstance().createOutputPort(this);
    // modelSourceNameParam = new StringParameter(this, "model source name");
    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:orange;stroke:orange\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n" + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" "
        + "style=\"stroke-width:1.0;stroke:white\"/>\n" + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" "
        + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<circle cx=\"0\" cy=\"0\" r=\"10\"" + "style=\"fill:white;stroke-width:2.0\"/>\n" + "<line x1=\"-15\" y1=\"0\" x2=\"0\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"-3\" y1=\"-3\" x2=\"0\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"-3\" y1=\"3\" x2=\"0\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n"
        + "</svg>\n");

    applicationParameters = new StringParameter(this, APPLICATION_PARAMETERS);
    new TextStyle(applicationParameters, "paramsTextArea");

  }

  public static final String MOCK_REQUEST = "Mock Request";

  private NamedObj getFlow(NamedObj no) {
    if (no.getContainer() == null) {
      return no;
    }
    return getFlow(no.getContainer());
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      NamedObj flow = getFlow(this);
      Context context = null;
      Map<String, String> systemParameterMap = getParameterMap(flow, RepositoryService.SYSTEM_PARAMETERS);
      Map<String, String> applicationParameterMap = getParameterMap(flow, RepositoryService.APPLICATION_PARAMETERS);

      if (flow.getAttribute("context") != null) {
        Attribute attribute = flow.getAttribute("context");
        Parameter parameter = (Parameter) attribute;
        ObjectToken oToken = (ObjectToken) parameter.getToken();
        Object o = oToken.getValue();
        if (o instanceof Context) {
          context = (Context) o;
        }

        processContext(context, systemParameterMap);
        putRequestIdOnMDC(context);
      } else {

        String initiator = null;
        String jobID = null;
        String processType = null;
        String revisionID = null;
        String reference = null;
        String locale = null;
        if (systemParameterMap != null) {
          initiator = systemParameterMap.get(RepositoryService.USER_ID);
          jobID = systemParameterMap.get(RepositoryService.JOB_ID);
          processType = systemParameterMap.get("com.isencia.passerelle.edm.processtype");
          if (processType == null)
            processType = systemParameterMap.get(RepositoryService.FLOW_NAME);
          reference = systemParameterMap.get(RepositoryService.REFERENCE);
          locale = systemParameterMap.get(RepositoryService.LOCALE);
        }
        if (initiator == null) {
          initiator = "unknown";
        }
        Request req = ServiceRegistry.getInstance().getEntityManager().getRequest(jobID);

        if (req == null) {
          Case caze = null;
          Long refId = null;
          try {
            refId = Long.parseLong(reference);
          } catch (Exception e) {
            // ignore, just indicates an invalid or empty reference
          }
          if (refId == null) {
            caze = ServiceRegistry.getInstance().getEntityFactory().createCase(null);
            ServiceRegistry.getInstance().getEntityManager().persistCase(caze);
          } else {
            caze = ServiceRegistry.getInstance().getEntityManager().getCase(refId);
          }

          req = ServiceRegistry.getInstance().getEntityFactory().createRequest(caze, initiator, getCategory(), processType, jobID);

          for (Map.Entry<String, String> entry : systemParameterMap.entrySet()) {
            addSystemAttribute(req, entry);
          }

          for (Map.Entry<String, String> entry : applicationParameterMap.entrySet()) {
            addApplicationAttribute(req, entry);
          }
          ServiceRegistry.getInstance().getEntityManager().persistRequest(req);
        }

        context = req.getProcessingContext();
        try {
          new Parameter(flow, "context", new ObjectToken(context));
        } catch (Exception e) {

        }
        putRequestIdOnMDC(context);
      }
      try {
        ptolemy.data.expr.Parameter parameter = new ptolemy.data.expr.Parameter(toplevel(), "requestId");
        parameter.setExpression(context.getRequest().getId().toString());
      } catch (Exception e) {

      }
      notifyStarted(context);
      ManagedMessage message = createMessage(context, ManagedMessage.objectContentType);
      response.addOutputMessage(output, message);
    } catch (MessageException e) {
      throw new ProcessingException("Error creating output msg", this, e);
    } catch (Exception t) {
      throw new ProcessingException("Error generating request", this, t);
    } finally {
      // should terminate after one request generation
      requestFinish();
    }
  }

  protected void notifyStarted(Context context) {
    if (!Status.STARTED.equals(context.getStatus())) {
      context.setStatus(Status.STARTED);

    }
  }

  protected void addApplicationAttribute(Request req, Map.Entry<String, String> entry) {
    ServiceRegistry.getInstance().getEntityFactory().createAttribute(req, entry.getKey(), entry.getValue());
  }

  protected void addSystemAttribute(Request req, Map.Entry<String, String> entry) {
    ServiceRegistry.getInstance().getEntityFactory().createAttribute(req, entry.getKey(), entry.getValue());
  }

  protected void processContext(Context context, Map<String, String> systemParameterMap) {

  }

  private void putRequestIdOnMDC(Context context) {
    MDC.put("requestId", context.getRequest().getId().toString());
  }

  // / check if the processtype of the context is equal to the processtype defined in modelsource
  // public boolean checkProcessType(Context context, Map<String, String> systemParameterMap) {
  //
  //
  // String value = modelSourceNameParam.getExpression();
  // if (value == null || value.trim().equals("")) {
  // return true;
  // }
  // if (context != null && context.getRequest().getType().equals(value)) {
  // return true;
  // }
  // if (systemParameterMap == null) {
  // return true;
  // }
  // String processType = systemParameterMap.get("com.isencia.passerelle.edm.processtype");
  // if (processType != null && !value.equals(processType)) {
  // return false;
  // }
  //
  // return false;
  // }

  protected String getCategory() {
    return null;
  }

  private Map<String, String> getParameterMap(NamedObj flow, String type) throws IllegalActionException {
    Attribute attribute = flow.getAttribute(type);
    Parameter parameter = (Parameter) attribute;
    if (parameter == null) {
      return null;
    }
    ObjectToken oToken = (ObjectToken) parameter.getToken();
    Object o = oToken.getValue();
    Map parameterMap = null;
    if (o instanceof Map) {
      return (Map) o;
    }
    return null;

  }

}
