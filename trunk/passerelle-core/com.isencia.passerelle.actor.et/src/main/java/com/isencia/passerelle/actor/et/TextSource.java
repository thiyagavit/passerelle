package com.isencia.passerelle.actor.et;

import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
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

public class TextSource extends NonBlockingActor {
  
  public Port output;
  public StringParameter textParameter;

  public TextSource(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);
    textParameter = new StringParameter(this,"value");
    textParameter.setExpression("Hello");
    registerConfigurableParameter(textParameter);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      String tokenMessage = ((StringToken) textParameter.getToken()).stringValue();
      ManagedMessage outputMsg = createMessage();
      outputMsg.setBodyContentPlainText(tokenMessage);
      
      response.addOutputMessage(output, outputMsg);
    } catch (Exception e) {
      throw new ProcessingException("Error creating output msg", this, e);
    } finally {
      requestFinish();
    }
  }
}
