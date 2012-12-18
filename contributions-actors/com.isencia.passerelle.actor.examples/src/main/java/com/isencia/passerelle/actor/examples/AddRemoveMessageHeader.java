/* Copyright 2011 - iSencia Belgium N

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

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;

/**
 * A transformer actor that adds or removes a header to a received message.
 */
@SuppressWarnings("serial")
public class AddRemoveMessageHeader extends Actor {

  private final static String MODE_ADD = "Add";
  private final static String MODE_REMOVE = "Remove";

  public Port input;
  public Port output;
  
  public StringParameter headerNameParameter;
  public StringParameter headerValueParameter;
  public StringParameter modeParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public AddRemoveMessageHeader(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, String.class);
    output = PortFactory.getInstance().createOutputPort(this);
    
    headerNameParameter = new StringParameter(this, "Header name");
    headerNameParameter.setExpression("MyHeader");
    headerValueParameter = new StringParameter(this, "Header value");
    modeParameter = new StringParameter(this, "Mode");
    // parameters can be configured with a list of options/choices
    // such parameters are rendered automatically with a combo-box
    modeParameter.addChoice(MODE_ADD);
    modeParameter.addChoice(MODE_REMOVE);
    // default value is Add
    modeParameter.setExpression(MODE_ADD);
  }

  /**
   * An illustration of validating the parameter settings.
   * E.g. for the mode parameter, we expect either Add or Remove,
   * but this constraint can not be enforced with 100% certainty in model files.
   * So we can check it again here.
   */
  @Override
  protected void validateInitialization() throws ValidationException {
    super.validateInitialization();
    
    String mode = modeParameter.getExpression();
    if(!MODE_REMOVE.equalsIgnoreCase(mode) && !MODE_ADD.equalsIgnoreCase(mode)) {
      throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Invalid mode "+mode, this, null);
    }
    
    String headerName = headerNameParameter.getExpression().trim();
    if(headerName.length()==0) {
      throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Undefined header name", this, null);
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage receivedMsg = request.getMessage(input);
    try {
      // Create a new outgoing msg, "caused by" the received input msg
      // and for the rest a complete copy of the received msg
      ManagedMessage outputMsg = MessageFactory.getInstance().createCausedCopyMessage(receivedMsg);
      
      String headerName = headerNameParameter.getExpression();
      String headerValue = headerValueParameter.getExpression();
      String mode = modeParameter.getExpression();
      
      if(MODE_ADD.equalsIgnoreCase(mode)) {
        outputMsg.addBodyHeader(headerName, headerValue);
      } else if(MODE_REMOVE.equalsIgnoreCase(mode)) {
        outputMsg.removeBodyHeader(headerName);
      } else {
        // should not happen, at least if the initialization validation is active.
        getLogger().warn("Invalid mode "+mode);
      }
      
      response.addOutputMessage(output, outputMsg);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Failed to add/remove a header", this, receivedMsg, e);
    }
  }
}
