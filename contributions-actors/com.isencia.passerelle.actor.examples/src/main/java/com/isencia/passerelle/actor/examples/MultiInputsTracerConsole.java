/* Copyright 2012 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.actor.examples;

import java.util.Iterator;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.dynaport.InputPortSetterBuilder;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * @author erwin
 */
public class MultiInputsTracerConsole extends Actor {
  private static final long serialVersionUID = 1L;
  private static final String INPUT_PORTNAMES = "Input port names (comma-separated)";

  public Parameter chopLengthParam;
  public InputPortSetterBuilder inputPortBldr;
  public StringParameter inputPortNamesParameter;

  private long counter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public MultiInputsTracerConsole(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    chopLengthParam = new Parameter(this, "Chop output at #chars", new IntToken(80));
//    input = PortFactory.getInstance().createInputPort(this, null);
    
    inputPortBldr = new InputPortSetterBuilder(this, "input port configurer");
    inputPortNamesParameter = new StringParameter(this, INPUT_PORTNAMES);

  }

  @Override
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    if (attribute == inputPortNamesParameter) {
      String inputPortNames = inputPortNamesParameter.getExpression();
      inputPortBldr.setInputPortNames(inputPortNames.split(","));
    } else {
      super.attributeChanged(attribute);
    }
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();

    counter = 0;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    StringBuilder contentBldr = new StringBuilder("Iteration " + request.getIterationCount() + ":");
    String content = null;
    int chopLength = 80;

    try {
      IntToken chopLengthToken = (IntToken) chopLengthParam.getToken();
      if (chopLengthToken != null) {
        chopLength = chopLengthToken.intValue();
      }
    } catch (IllegalActionException e) {
      // ignore
    }

    Iterator<MessageInputContext> contexts = request.getAllInputContexts();
    while (contexts.hasNext()) {
      MessageInputContext context = (MessageInputContext) contexts.next();
      Iterator<ManagedMessage> msgIterator = context.getMsgIterator();
      while (msgIterator.hasNext()) {
        ManagedMessage msg = (ManagedMessage) msgIterator.next();
        contentBldr.append(context.getPortName() + ":" + msg.getID());
        counter++;
      }
    }
    content = contentBldr.toString();

    if (chopLength < content.length()) {
      content = content.substring(0, chopLength) + " !! CHOPPED !! ";
    }
    if (content != null) {
      ExecutionTracerService.trace(this, content);
    }
    ExecutionTracerService.trace(this, "Received msg# " + counter);
  }

  @Override
  protected void doWrapUp() throws TerminationException {
    ExecutionTracerService.trace(this, "Received total msg# " + counter);
    super.doWrapUp();
  }

}
