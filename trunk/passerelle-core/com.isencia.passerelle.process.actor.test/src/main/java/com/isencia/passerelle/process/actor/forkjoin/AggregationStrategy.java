/**
 * 
 */
package com.isencia.passerelle.process.actor.forkjoin;

import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

/**
 * A <code>AggregationStrategy</code> instance can be plugged in <code>MessageSequenceGenerator</code>s and other things handling
 * message sets. The purpose is to be able to configure any desired kind of aggregation/merging/folding logic on message sets, 
 * and to obtain a single result message.
 * 
 * @author erwin
 */
public interface AggregationStrategy {
  
  /**
   * 
   * @param initialMsg
   * @param otherMessages
   * @return the result of aggregating initialMsg with all otherMessages
   * 
   * @throws MessageException
   */
  ManagedMessage aggregateMessages(ManagedMessage initialMsg, ManagedMessage... otherMessages) throws MessageException;

}
