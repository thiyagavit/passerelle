package com.isencia.passerelle.process.actor.forkjoin;

import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * This is an implementation that can be set on an <code>Actor</code> (or other <code>NamedObj</code>) 
 * that is a <code>MessageSequenceGenerator</code>, and generates an exception for the <code>initialMsg</code> that is being evicted.
 * 
 * @author erwin
 *
 */
public class ErrorThrowingEvictedMessageHandler implements EvictedMessagesHandler {
  
  private NamedObj container;

  /**
   * 
   * @param container the <code>MessageSequenceGenerator</code> using this handler;
   * must be a model element, typically an <code>Actor</code>, for this type of handler.
   */
  public ErrorThrowingEvictedMessageHandler(NamedObj container) {
    this.container = container;
  }
  public void handleEvictedMessages(ManagedMessage initialMsg, ManagedMessage... otherMessages) throws PasserelleException {
    throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, "Message evicted from msg sequence src", container, initialMsg, null);
  }
}
