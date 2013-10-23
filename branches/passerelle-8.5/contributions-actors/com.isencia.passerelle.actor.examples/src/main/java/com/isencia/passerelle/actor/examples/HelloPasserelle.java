/* Copyright 2011 - iSencia Belgium NV

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

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * A first hello world actor, transforming a received text msg and sending it onwards.
 *
 */
@SuppressWarnings("serial")
public class HelloPasserelle extends Actor {
  
  public Port input;
  public Port output;
  
  public Parameter changedTextParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public HelloPasserelle(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    // In case of a single input port, this short-cut factory method can be used.
    // It creates a blocking port with name "input".
    // The String.class as second parameter guarantees that the received messages will contain a String,
    // or will be transformed into a String by Passerelle's automated type conversion chain.
    input = PortFactory.getInstance().createInputPort(this, String.class);
    // In case of a single output port, this short-cut factory method can be used.
    // It creates a port with name "output".
    output = PortFactory.getInstance().createOutputPort(this);
    // A Parameter gets a name (2nd parameter below), which is also used to automatically generate configuration forms.
    // Different specific Parameter classes correspond to different specific form widgets (e.g. text field, combobox, file chooser etc.)
    changedTextParameter = new StringParameter(this, "Changed text");
    // Set the default value.
    changedTextParameter.setExpression("Hello Passerelle");
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    // Get the message received on the input port, from the request.
    ManagedMessage receivedMsg = request.getMessage(input);
    try {
      // We know the message will contain a String in its body.
      String receivedText = receivedMsg.getBodyContentAsString();
      // Get the configured value for the changed text.
      String changedText = changedTextParameter.getExpression();
      // Create a new outgoing msg, "caused by" the received input msg
      ManagedMessage outputMsg = createMessageFromCauses(receivedMsg);
      outputMsg.setBodyContentPlainText("Changed ["+receivedText+"] to ["+changedText+"]");
      // Set the outgoing msg to be sent on the output port
      response.addOutputMessage(output, outputMsg);
    } catch (Exception e) {
      // When something failed, throw a ProcessingException which will be handled as needed
      // by Passerelle's default error handling mechanisms.
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Failed to transform the received text", this, receivedMsg, e);
    }
  }
}
