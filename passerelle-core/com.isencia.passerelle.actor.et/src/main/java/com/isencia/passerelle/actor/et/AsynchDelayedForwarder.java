package com.isencia.passerelle.actor.et;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;

public class AsynchDelayedForwarder extends NonBlockingActor {
  
  public Port input;
  public Port output;

  public AsynchDelayedForwarder(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage receivedMsg = request.getMessage(input);
    // Create a new outgoing msg, "caused by" the received input msg
    // and for the rest a complete copy of the received msg
    try {
      ManagedMessage outputMsg = MessageFactory.getInstance().createCausedCopyMessage(receivedMsg);
      new Thread(new DelayedSender(outputMsg)).start();
    } catch (MessageException e) {
      throw new ProcessingException("Failed to create & send output msg", receivedMsg, e);
    }
  }
  
  private class DelayedSender implements Runnable {
    
    private ManagedMessage outputMsg;

    
    public DelayedSender(ManagedMessage outputMsg) {
      this.outputMsg = outputMsg;
    }


    @Override
    public void run() {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }

      try {
        sendOutputMsg(output, outputMsg);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
  }
}
