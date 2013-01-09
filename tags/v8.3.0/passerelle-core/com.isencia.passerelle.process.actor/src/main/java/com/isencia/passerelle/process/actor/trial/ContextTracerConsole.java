/*
 * (c) Copyright 2002, Tuple NV Belgium
 * All Rights Reserved.
 */
package com.isencia.passerelle.process.actor.trial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.xml.MessageBuilder2;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * Dump a Context in an execution trace message
 * 
 * @author erwin
 * @version 1.0
 */
public class ContextTracerConsole extends Sink {
  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ContextTracerConsole.class);

  public Parameter chopLengthParam; // NOSONAR

  private int chopLength = 80;

  /**
   * @param container
   *          The container.
   * @param name
   *          The name of this actor.
   * @exception IllegalActionException
   *              If the entity cannot be contained by the proposed container.
   * @exception NameDuplicationException
   *              If the container already has an actor with this name.
   */
  public ContextTracerConsole(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);

    chopLengthParam = new Parameter(this, "Chop output at #chars", new IntToken(chopLength));
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if (attribute == chopLengthParam) {
      IntToken chopLengthToken = (IntToken) chopLengthParam.getToken();
      if (chopLengthToken != null) {
        chopLength = chopLengthToken.intValue();
        getLogger().debug("{} Chop length changed to {}", getFullName(), chopLength);
      }
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  protected void sendMessage(ManagedMessage message) throws ProcessingException {
    if (message != null) {
      String msgAsXML = MessageBuilder2.buildToXML(message);
      // if (isPassThrough()) {
      ExecutionTracerService.trace(this, msgAsXML);
      // } else {
      // String content=null;
      // try {
      // content = message.getBodyContentAsString();
      // if(chopLength<content.length()) {
      // content = content.substring(0,chopLength) + " !! CHOPPED !! ";
      // }
      // } catch (MessageException e) {
      // throw new ProcessingException(PasserelleException.Severity.NON_FATAL,"",message,e);
      // }
      // if (content != null) {
      // ExecutionTracerService.trace(this, content);
      // }
      // }
    }
  }

  public int getChopLength() {
    return chopLength;
  }
}