package com.isencia.passerelle.process.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
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
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Status;

/**
 * An actor that marks an incoming request with the configured status (currently only supports a few).
 * 
 * @author puidir
 *
 */
public class RequestStatus extends Actor {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestStatus.class);
	
    private static final String STATUS_PARAM = "status";
    
    public Port output;
    public Port input;

    public Parameter statusParam;
    private Status status = Status.FINISHED;

    public RequestStatus(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		
        input = PortFactory.getInstance().createInputPort(this, ManagedMessage.class);
        output = PortFactory.getInstance().createOutputPort(this);

        statusParam = new StringParameter(this, STATUS_PARAM);
        statusParam.addChoice(Status.ERROR.name());
        statusParam.addChoice(Status.TIMEOUT.name());
        statusParam.addChoice(Status.FINISHED.name());
	}

	@Override
	public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == statusParam) {
            String paramStr = statusParam.getExpression();
            if (paramStr != null && paramStr.length() > 0) {
                status = Status.valueOf(paramStr);
            }
        }
        super.attributeChanged(attribute);
	}

	@Override
	protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {

		ManagedMessage message = request.getMessage(input);
		
		try {
			if ((message.getBodyContent() instanceof Context)) {
				Context flowContext = (Context)message.getBodyContent();
							
				if (status.equals(Status.FINISHED)) {
					ContextManagerProxy.notifyFinished(flowContext);
				} else if (status.equals(Status.ERROR)) {
					ContextManagerProxy.notifyError(flowContext, new Exception());
				} else if (status.equals(Status.TIMEOUT)) {
					ContextManagerProxy.notifyTimeOut(flowContext);
				}
				
			}
		} catch (MessageException ex) {
			LOGGER.error("Error while setting flow context status", ex);
		}
		
		response.addOutputMessage(output, message);
	}

}
