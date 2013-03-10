/**
 * 
 */
package com.isencia.passerelle.process.actor.forkjoin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.dynaport.OutputPortConfigurationExtender;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.message.internal.sequence.SequenceTrace;
import com.isencia.passerelle.process.actor.Actor;
import com.isencia.passerelle.process.actor.ActorContext;
import com.isencia.passerelle.process.actor.ProcessRequest;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.model.Context;

/**
 * <p>
 * An actor with configurable named output ports, that sends out copies of the incoming Context on each outgoing port. The original Context is sent on the
 * default output port.
 * </p>
 * <p>
 * This is useful to ensure that parallel branches in a sequence don't see each others intermediate results.
 * </p>
 * <p>
 * The <code>Fork</code> is typically used with the <code>Join</code>, to merge the results of the parallel branches into one Context again.
 * </p>
 * 
 * @author delerw
 */
public class Fork extends Actor implements MessageSequenceSource {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(Fork.class);

  public Port input;
  public OutputPortConfigurationExtender outputPortCfgExt;

  private Map<Long, SequenceTrace> msgSequences = new HashMap<Long, SequenceTrace>();
  private Map<Long, ManagedMessage> branchedContexts = new HashMap<Long, ManagedMessage>();
  
  private JoinStrategy joinStrategy;
  private EvictedMessagesHandler evictedMessagesHandler;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Fork(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    outputPortCfgExt = new OutputPortConfigurationExtender(this, "output port configurer");
    joinStrategy = new ContextJoinStrategy();
    evictedMessagesHandler = new ErrorThrowingEvictedMessageHandler(this);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * A Fork is a stateful MessageSequenceSource
   */
  public boolean isStateful() {
    return true;
  }

  public void process(ActorContext ctx, ProcessRequest procRequest, ProcessResponse procResponse) throws ProcessingException {
    MessageContainer message = (MessageContainer) procRequest.getMessage(input);
    if (message != null) {
      try {
        Context processContext = getRequiredContextForMessage(message);
        Long scopeId = processContext.getRequest().getId();
        branchedContexts.put(scopeId, message);

        try {
          getAuditLogger().info("Forking msg with scope " + scopeId + " : " + getAuditTrailMessage(message, input));
        } catch (Exception e) {
          getLogger().error("Error logging audit trail", e);
        }

        List<Port> outputPorts = outputPortCfgExt.getOutputPorts();
        for (int i = 0; i < outputPorts.size(); ++i) {
          Context newOne = processContext.fork();
          MessageContainer outputMsg = (MessageContainer) MessageFactory.getInstance().createMessageCloneInSequence(
              message,
              processContext.getRequest().getId(),  // sequence ID
              new Long(i),                          // sequence position
              (i == (outputPorts.size() - 1)));     // end of sequence?
          // enforce single Fork name
          outputMsg.setHeader(HEADER_SEQ_SRC, getName());
          outputMsg.setBodyContent(newOne, ManagedMessage.objectContentType);
          procResponse.addOutputMessage(outputPorts.get(i), outputMsg);
        }
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error generating forked messages", this, message, e);
      }
    }
  }

  public boolean fromThisSource(ManagedMessage seqMsg) {
    try {
      return branchedContexts.get(seqMsg.getSequenceID()) != null;
    } catch (Exception e) {
      return false;
    }
  }

  public ManagedMessage joinProcessedMessageInSequence(ManagedMessage branchedMsg) throws ProcessingException {
    Long scopeId = branchedMsg.getSequenceID();
    Context branchedCtxt = null;
    if (branchedContexts.get(scopeId) != null) {
      try {
        branchedCtxt = (Context) branchedContexts.get(scopeId).getBodyContent();
      } catch (MessageException e) {
        getLogger().error("Error getting context for scope " + scopeId, e);
      }
    }
    ManagedMessage mergedMsg = null;
    if (branchedCtxt != null) {
      SequenceTrace seqTrace = msgSequences.get(scopeId);
      if (seqTrace == null) {
        seqTrace = new SequenceTrace(scopeId);
        msgSequences.put(scopeId, seqTrace);
      }
      seqTrace.addMessage(branchedMsg);
      if (seqTrace.isComplete()) {
        try {
          getAuditLogger().debug("{} All branch messages received for scope {}", getFullName(), scopeId);
          
          ManagedMessage[] messages = seqTrace.getMessagesInSequence();
          MessageContainer scopeMsg = (MessageContainer) branchedContexts.get(seqTrace.getSequenceID());

          mergedMsg = joinStrategy!=null ?
              joinStrategy.joinMessages(scopeMsg, messages) : scopeMsg;
              
        } catch (Exception e) {
          throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error merging messages for scope " + scopeId, this, e);
        } finally {
          msgSequences.remove(seqTrace);
          seqTrace.clear();
          branchedContexts.remove(branchedCtxt);
        }
      }
    }
    return mergedMsg;
  }

  public JoinStrategy getJoinStrategy() {
    return joinStrategy;
  }
  public void setJoinStrategy(JoinStrategy joinStrategy) {
    this.joinStrategy = joinStrategy;
  }
  public EvictedMessagesHandler getEvictedMessagesHandler() {
    return evictedMessagesHandler;
  }
  public void setEvictedMessagesHandler(EvictedMessagesHandler evictedMessagesHandler) {
    this.evictedMessagesHandler = evictedMessagesHandler;
  }
}
