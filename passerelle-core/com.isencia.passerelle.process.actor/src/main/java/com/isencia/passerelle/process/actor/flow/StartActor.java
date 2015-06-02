package com.isencia.passerelle.process.actor.flow;

import java.util.Map;

import org.slf4j.MDC;

import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
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
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.Status;
import com.isencia.passerelle.process.service.ServiceRegistry;
import com.isencia.passerelle.project.repository.api.RepositoryService;

/**
 * Actor to generate a simple Request that mainly can be used as trigger for basic diagnostic sequences.
 * 
 * @author erwin
 * 
 */
public class StartActor extends Actor {

  private static final long serialVersionUID = 1L;

  public Port trigger; // NOSONAR
  public Port output; // NOSONAR

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
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      NamedObj flow = toplevel();
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
        context = constructNewRequestContextForFlow(flow, systemParameterMap, applicationParameterMap);
      }
      try {
        Parameter parameter = (Parameter) flow.getAttribute("requestId", Parameter.class);
        if (parameter == null) {
          parameter = new Parameter(flow, "requestId");
        }
        parameter.setExpression(context.getRequest().getId().toString());
      } catch (Exception e) {
        getLogger().warn(ErrorCode.ACTOR_EXECUTION_ERROR + " - Error setting requestId parameter", e);
      }
      notifyStarted(context);
      ManagedMessage message = createMessage(context, ManagedMessage.objectContentType);
      response.addOutputMessage(output, message);
    } catch (MessageException e) {
      throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error creating output msg", this, e);
    } catch (Exception t) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error generating request", this, t);
    } finally {
      // should terminate after one request generation
      requestFinish();
    }
  }

  protected Context constructNewRequestContextForFlow(NamedObj flow, Map<String, String> systemParameterMap, Map<String, String> applicationParameterMap) {
    Context context;
    String initiator = null;
    String jobID = null;
    String processType = null;
    String reference = null;
    if (systemParameterMap != null) {
      initiator = systemParameterMap.get(RepositoryService.USER_ID);
      jobID = systemParameterMap.get(RepositoryService.JOB_ID);
      processType = systemParameterMap.get(com.isencia.passerelle.process.common.util.Constants.REQUEST_TYPE_ATTRIBUTE);
      if (processType == null)
        processType = systemParameterMap.get(RepositoryService.FLOW_NAME);
      reference = systemParameterMap.get(RepositoryService.REFERENCE);
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
      req = ServiceRegistry.getInstance().getEntityFactory().createRequest(caze, initiator,toplevel().getName(), getCategory(), processType, jobID);
      if (systemParameterMap != null) {
        for (Map.Entry<String, String> entry : systemParameterMap.entrySet()) {
          addSystemAttribute(req, entry);
        }
      }
      if (applicationParameterMap != null) {
        for (Map.Entry<String, String> entry : applicationParameterMap.entrySet()) {
          addApplicationAttribute(req, entry);
        }
      }
      ServiceRegistry.getInstance().getEntityManager().persistRequest(req);
    }
    context = req.getProcessingContext();
    try {
      new Parameter(flow, "context", new ObjectToken(context));
    } catch (Exception e) {
      getLogger().warn(ErrorCode.ACTOR_EXECUTION_ERROR + " - Error setting context parameter", e);
    }
    putRequestIdOnMDC(context);
    return context;
  }

  protected void notifyStarted(Context context) {
    if (!Status.STARTED.equals(context.getStatus()) && !Status.RESTARTED.equals(context.getStatus())) {
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

  protected String getCategory() {
    return null;
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
      return (Map<String,String>) o;
    }
    return null;
  }
}
