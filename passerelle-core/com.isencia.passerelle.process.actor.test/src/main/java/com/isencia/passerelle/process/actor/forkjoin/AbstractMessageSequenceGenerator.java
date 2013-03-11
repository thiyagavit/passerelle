/**
 * 
 */
package com.isencia.passerelle.process.actor.forkjoin;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.message.internal.sequence.SequenceTrace;
import com.isencia.passerelle.process.actor.Actor;
import com.isencia.passerelle.process.model.Context;

/**
 * 
 * @author erwin
 */
public abstract class AbstractMessageSequenceGenerator extends Actor implements MessageSequenceGenerator {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMessageSequenceGenerator.class);

  private Map<Long, SequenceTrace> msgSequences = new HashMap<Long, SequenceTrace>();
  private Map<Long, ManagedMessage> sequenceScopeMessages = new HashMap<Long, ManagedMessage>();
  
  private AggregationStrategy aggregationStrategy;
  private EvictedMessagesHandler evictedMessagesHandler;
  private boolean stateful;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public AbstractMessageSequenceGenerator(CompositeEntity container, String name, boolean stateful) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    aggregationStrategy = new ContextAggregationStrategy();
    evictedMessagesHandler = new ErrorThrowingEvictedMessageHandler(this);
    this.stateful = stateful;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * A Fork is a stateful MessageSequenceGenerator
   */
  public boolean isStateful() {
    return stateful;
  }

  public boolean wasGeneratedHere(ManagedMessage seqMsg) {
    try {
      return sequenceScopeMessages.get(seqMsg.getSequenceID()) != null;
    } catch (Exception e) {
      return false;
    }
  }
  
  protected void registerSequenceScopeMessage(Long seqID, ManagedMessage message) {
    sequenceScopeMessages.put(seqID, message);
  }

  public ManagedMessage aggregateProcessedMessage(ManagedMessage branchedMsg) throws ProcessingException {
    Long scopeId = branchedMsg.getSequenceID();
    Context branchedCtxt = null;
    if (sequenceScopeMessages.get(scopeId) != null) {
      try {
        branchedCtxt = (Context) sequenceScopeMessages.get(scopeId).getBodyContent();
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
          getAuditLogger().debug("{} All sequence messages received for scope {}", getFullName(), scopeId);
          
          ManagedMessage[] messages = seqTrace.getMessagesInSequence();
          MessageContainer scopeMsg = (MessageContainer) sequenceScopeMessages.get(seqTrace.getSequenceID());

          mergedMsg = aggregationStrategy!=null ?
              aggregationStrategy.aggregateMessages(scopeMsg, messages) : scopeMsg;
              
        } catch (Exception e) {
          throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error merging messages for scope " + scopeId, this, e);
        } finally {
          msgSequences.remove(seqTrace);
          seqTrace.clear();
          sequenceScopeMessages.remove(branchedCtxt);
        }
      }
    }
    return mergedMsg;
  }

  public AggregationStrategy getAggregationStrategy() {
    return aggregationStrategy;
  }
  public void setAggregationStrategy(AggregationStrategy aggregationStrategy) {
    this.aggregationStrategy = aggregationStrategy;
  }
  public EvictedMessagesHandler getEvictedMessagesHandler() {
    return evictedMessagesHandler;
  }
  public void setEvictedMessagesHandler(EvictedMessagesHandler evictedMessagesHandler) {
    this.evictedMessagesHandler = evictedMessagesHandler;
  }
}
