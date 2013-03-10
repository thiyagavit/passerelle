/**
 * 
 */
package com.isencia.passerelle.process.actor.forkjoin;

import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

/**
 * A <code>JoinStrategy</code> instance can be plugged in <code>MessageSequenceSource</code>s and other things handling
 * message sets. The purpose is to be able to configure any desired kind of joining/merging/folding logic on message sets, 
 * and to obtain a single result message.
 * 
 * @author erwin
 */
public interface JoinStrategy {
  
  /**
   * 
   * @param initialMsg
   * @param otherMessages
   * @return the result of joining initialMsg with all otherMessages
   * 
   * @throws MessageException
   */
  ManagedMessage joinMessages(ManagedMessage initialMsg, ManagedMessage... otherMessages) throws MessageException;

}
