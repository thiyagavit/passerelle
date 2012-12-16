/**
 * 
 */
package com.isencia.passerelle.process.actor.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.dynaport.DynamicNamedOutputPortsActor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.message.internal.sequence.SequenceTrace;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.service.ServiceRegistry;

/**
 * <p>
 * An actor with configurable named output ports, that sends out copies of the
 * incoming Context on each outgoing port. The original Context is sent on the
 * default output port.
 * </p>
 * <p>
 * This is useful to ensure that parallel branches in a sequence don't see each
 * others intermediate results.
 * </p>
 * <p>
 * The <code>Fork</code> is typically used with the <code>Join</code>, to merge
 * the results of the parallel branches into one Context again.
 * </p>
 * 
 * @author delerw
 * 
 */
public class Fork extends DynamicNamedOutputPortsActor {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(Fork.class);

  // Header name to set the name of this actor in each outgoing msg
	// join actors must then search for this actor with that name.
	// This assumes that Fork and Join are within the same containing CompositeActor.
	public final static String FORK_ACTOR_HEADER_NAME = "__PSRL_FORK_ACTOR";

	public Port input;

	private Map<Long, SequenceTrace> msgSequences = new HashMap<Long, SequenceTrace>();
	private Map<Long, ManagedMessage> branchedContexts = new HashMap<Long, ManagedMessage>();

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public Fork(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		input = PortFactory.getInstance().createInputPort(this, null);
	}

	@Override
	protected Logger getLogger() {
	  return LOGGER;
	}
	
	@Override
	protected void process(ActorContext ctx, ProcessRequest procRequest, ProcessResponse procResponse) throws ProcessingException {
		MessageContainer message = (MessageContainer) procRequest.getMessage(input);
		if (message != null) {
			try {
				Context diagnosisContext = (Context) message.getBodyContent();
				Long scopeId = diagnosisContext.getRequest().getId();
				branchedContexts.put(scopeId, message);

        try {
          getAuditLogger().info("Forking msg with scope " + scopeId + " : " + getAuditTrailMessage(message, input));
        } catch (Exception e) {
          getLogger().error("Error logging audit trail",e);
        }

				List<Port> outputPorts = getOutputPorts();
				for (int i = 0; i < outputPorts.size(); ++i) {
					Context newOne = diagnosisContext.fork();
					// using internal Passerelle class for this complete copy + sequence
					// headers
					// TODO provide public API in MessageFactory to do the same
					MessageContainer outputMsg = message.copy();
					outputMsg.setSequenceID(diagnosisContext.getRequest().getId());
					outputMsg.setSequencePosition(new Long(i));
					outputMsg.setSequenceEnd(i == (outputPorts.size() - 1));
					outputMsg.addCauseID(message.getID());
					// enforce single Fork name
					outputMsg.removeHeader(FORK_ACTOR_HEADER_NAME);
					outputMsg.setHeader(FORK_ACTOR_HEADER_NAME, getName());
					outputMsg.setBodyContent(newOne, ManagedMessage.objectContentType);
					procResponse.addOutputMessage(outputPorts.get(i), outputMsg);
				}
			} catch (Exception e) {
				throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR,"Error generating forked messages", this, message, e);
			}
		}
	}
	
	public ManagedMessage mergeBranchedMessage(ManagedMessage branchedMsg) throws ProcessingException {
		ManagedMessage mergedMsg = null;
		
    Long scopeId = branchedMsg.getSequenceID();
		Context branchedCtxt = null;
    if(branchedContexts.get(scopeId)!=null) {
      try {
      	branchedCtxt = (Context) branchedContexts.get(scopeId).getBodyContent();
      } catch (MessageException e) {
        getLogger().error("Error getting context for scope "+scopeId,e);
      }
    }
    if(branchedCtxt!=null) {
      SequenceTrace seqTrace = msgSequences.get(scopeId);
  		if(seqTrace==null) {
  			seqTrace = new SequenceTrace(scopeId);
  			msgSequences.put(scopeId,seqTrace);
  		}
  		seqTrace.addMessage(branchedMsg);
			if(seqTrace.isComplete()) {
				try {
					getAuditLogger().debug("{} All branch messages received for scope {}", getFullName(), scopeId);
					mergedMsg = mergeFromMessages(seqTrace);
				} catch (Exception e) {
					throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error merging messages for scope "+scopeId, this, e);
				} finally {
					msgSequences.remove(seqTrace);
					seqTrace.clear();
					branchedContexts.remove(branchedCtxt);
				}
			}
    }
    return mergedMsg;
	}

  private ManagedMessage mergeFromMessages(SequenceTrace seqTrace) throws MessageException {
    ManagedMessage[] messages = seqTrace.getMessagesInSequence();
    MessageContainer scopeMsg = (MessageContainer) branchedContexts.get(seqTrace.getSequenceID());
    ManagedMessage msg = scopeMsg.copy();
    Context mergedCtxt = (Context) scopeMsg.getBodyContent();
    List<Context> branches = new ArrayList<Context>();
    for (ManagedMessage branchMsg : messages) {
    	Context branchedCtx = (Context)branchMsg.getBodyContent();
    	msg.addCauseID(branchedCtx.getId());
    	branches.add(branchedCtx);
    }
    mergedCtxt = ServiceRegistry.getInstance().getEntityManager().mergeWithBranchedContexts(mergedCtxt, branches);
    
    msg.setBodyContent(mergedCtxt, ManagedMessage.objectContentType);
    return msg;
  }

	@Override
	protected String getAuditTrailMessage(ManagedMessage message, Port port) {
		try {
      if (message.getBodyContent() instanceof Context) {
      	Context diagnosisContext = (Context) message.getBodyContent();
      	return port.getFullName() + " - msg for request " + diagnosisContext.getRequest().getId();
      } else {
      	return super.getAuditTrailMessage(message, port);
      }
    } catch (MessageException e) {
      getLogger().error("Error getting msg content", e);
      return super.getAuditTrailMessage(message, port);
    }
	}
}
