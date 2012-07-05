/**
 * 
 */
package com.isencia.passerelle.process.actor.flow;

import java.util.Iterator;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
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
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.process.model.Context;

/**
 * A Join should be used in a combination with a preceding Fork. Where the Fork
 * is used to start multiple parallel branches, each with their own local copy
 * of the original Context, the Join is used to assemble and merge all results
 * from the parallel branches. The Join expects all parallel branches to be
 * connected to the single mergeInput port. The original context sent out via
 * the Fork's "std" output port, must be received on the scopeInput port. This
 * is not symmetric yet to the design of the Fork, which has separate output
 * ports per branch. Future will tell how this will evolve... When the results
 * of all parallel branches have been received, the merged context, i.e.
 * containing the union of all executed tasks and obtained results, is sent out
 * via the output port.
 * 
 * @author delerw
 */
public class Join extends Actor {

	private static final long serialVersionUID = 1L;
	
	public Port mergeInput;			// NOSONAR
	public Port output;				// NOSONAR

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public Join(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		mergeInput = PortFactory.getInstance().createInputPort(this, null);
		output = PortFactory.getInstance().createOutputPort(this);
	}

	@Override
	protected void process(ActorContext actorcontext,
			ProcessRequest procRequest, ProcessResponse procResponse)
			throws ProcessingException {
		Iterator<MessageInputContext> msgInputCtxtItr = procRequest
				.getAllInputContexts();
		while (msgInputCtxtItr.hasNext()) {
			MessageInputContext inputContext = (MessageInputContext) msgInputCtxtItr
					.next();
			if (!inputContext.isProcessed()) {
				if (mergeInput.getName().equals(inputContext.getPortName())) {
					ManagedMessage branchedMsg = procRequest
							.getMessage(mergeInput);
					ManagedMessage mergedMessage = mergeMessage(branchedMsg);
					if (mergedMessage != null) {
						procResponse.addOutputMessage(output, mergedMessage);
					}
				}
			}
		}
	}

	private ManagedMessage mergeMessage(ManagedMessage branchedMsg)
			throws ProcessingException {
		String[] forkNames = ((MessageContainer) branchedMsg)
				.getHeader(Fork.FORK_ACTOR_HEADER_NAME);
		// should be length 1
		if (forkNames.length == 1) {
			Entity fork = ((CompositeEntity) getContainer())
					.getEntity(forkNames[0]);
			if (fork != null) {
				return ((Fork) fork).mergeBranchedMessage(branchedMsg);
			}
		}
		return null;
	}

	@Override
	protected String getAuditTrailMessage(ManagedMessage message, Port port)
			throws Exception {
		if (message.getBodyContent() instanceof Context) {
			Context diagnosisContext = (Context) message.getBodyContent();
			return port.getFullName() + " - msg for request "
					+ diagnosisContext.getRequest().getId();
		} else {
			return super.getAuditTrailMessage(message, port);
		}
	}
}
