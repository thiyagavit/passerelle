/**
 * 
 */
package com.isencia.passerelle.process.actor.forkjoin;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * An <code>EvictedMessagesHandler</code> instance can be plugged in <code>MessageSequenceGenerator</code>s and other things maintaining message sets. 
 * The purpose is to be able to configure what must be done with message sets that have been evicted from the source's state storage,
 * before they had been completely processed, joined etc.
 * 
 * @author erwin
 */
public interface EvictedMessagesHandler {
  
  /**
   * @param initialMsg
   * @param otherMessages
   * 
   * @throws PasserelleException
   */
  void handleEvictedMessages(ManagedMessage initialMsg, ManagedMessage... otherMessages) throws PasserelleException;

}
