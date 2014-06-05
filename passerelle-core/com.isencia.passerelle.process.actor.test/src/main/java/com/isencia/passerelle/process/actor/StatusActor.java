package com.isencia.passerelle.process.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Status;
import com.isencia.passerelle.process.service.ServiceRegistry;

/**
 * An actor that marks an incoming request with the configured status.
 * 
 * @author erwin
 */
public class StatusActor extends Actor {

  private final static Logger LOGGER = LoggerFactory.getLogger(StatusActor.class);
  private static final long serialVersionUID = 1L;

  public Port output;
  public Port input;

  public Parameter statusParam;

  public StatusActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, ManagedMessage.class);
    output = PortFactory.getInstance().createOutputPort(this);

    statusParam = new StringParameter(this, "status");
    statusParam.setExpression(Status.FINISHED.name());
    for (Status s : Status.values()) {
      statusParam.addChoice(s.name());
    }
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage message = request.getMessage(input);
    try {
      Context context = (Context) message.getBodyContent();
      Status status = Status.valueOf(statusParam.getExpression());
      context.setStatus(status);
      getLogger().debug("Request {} set to status {}", context.getRequest().getId(), status);
      ServiceRegistry.getInstance().getProcessPersistenceService().updateStatus(context.getRequest());
      message.setBodyContent(context, ManagedMessage.objectContentType);
    } catch (MessageException e) {
      throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "", this, message, e);
    }
    response.addOutputMessage(output, message);
  }
}