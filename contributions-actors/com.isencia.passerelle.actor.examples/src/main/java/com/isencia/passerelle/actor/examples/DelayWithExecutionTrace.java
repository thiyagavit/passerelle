/**
 * 
 */
package com.isencia.passerelle.actor.examples;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
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
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * @author erwin
 *
 */
public class DelayWithExecutionTrace extends Actor {
	
	public Parameter timeParameter = null;
	public Port input;
	public Port output;

	/**
	 * Construct an actor with the given container and name.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this actor.
	 * @exception IllegalActionException
	 *                If the actor cannot be contained by the proposed
	 *                container.
	 * @exception NameDuplicationException
	 *                If the container already has an actor with this name.
	 */
	public DelayWithExecutionTrace(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		
		input = PortFactory.getInstance().createInputPort(this, null);
		output = PortFactory.getInstance().createOutputPort(this);
		
		timeParameter = new Parameter(this, "time(s)", new IntToken(1));
		timeParameter.setTypeEquals(BaseType.INT);
        registerConfigurableParameter(timeParameter);
	}

	@Override
	protected void process(ActorContext ctxt, ProcessRequest request,
			ProcessResponse response) throws ProcessingException {

		ManagedMessage msg = request.getMessage(input);
		int time = Integer.parseInt(timeParameter.getExpression());
		try {
			if(time>0) {
				ExecutionTracerService.trace(this, "Going to sleep for msg "+msg.getBodyContentAsString());
				boolean interrupted = false;
				int interruptedTime = 0;
				for(int i=0;i<time;++i) {
					Thread.sleep(1000);
					if(isFinishRequested()) {
						interrupted = true;
						interruptedTime = i;
						break;
					}
				}
				if(!interrupted)
					ExecutionTracerService.trace(this, "Waking up again for msg "+msg.getBodyContentAsString());
				else
					ExecutionTracerService.trace(this, "Delay interrupted after "+interruptedTime+"(s) for msg "+msg.getBodyContentAsString());
			}
		} catch(InterruptedException e) {
            // do nothing, means someone wants us to stop
        } catch (Exception e) {
			throw new ProcessingException("",this,e);
		}
		
		try {
			response.addOutputMessage(output, msg);
		} catch (IllegalArgumentException e) {
			throw new ProcessingException(getInfo() + " - doFire() generated exception "+e,msg,e);
		}
	}
}
