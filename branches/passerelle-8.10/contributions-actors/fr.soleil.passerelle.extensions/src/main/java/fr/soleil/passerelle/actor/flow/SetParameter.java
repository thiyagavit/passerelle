/* Copyright 2014 - Synchrotron Soleil, iSencia Belgium NV

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
package fr.soleil.passerelle.actor.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeRequest;
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
 * A basic actor that reads an input message and sets its body as the value of a selected parameter. The parameter can be anywhere in the model, e.g. as a
 * (sub)model parameter or as an actor parameter.
 * <p>
 * The name of the parameter-to-be-modified must be manually specified. I.e. there is no automated lookup (yet) of all parameters present in the actor's parent
 * model.
 * </p>
 * 
 * @author delerw
 */
@SuppressWarnings("serial")
public class SetParameter extends Actor {
  private final static Logger LOGGER = LoggerFactory.getLogger(SetParameter.class);

  public Port input;
  public Port output;

  public StringParameter paramNameParameter;
  // if this parameter is set to true, the model will be marked as "dirty" in the HMI after the parameter change so the user can save the changed moml file
  public Parameter persistChangeParameter;
  
  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public SetParameter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);

    paramNameParameter = new StringParameter(this, "Parameter to modify");
    persistChangeParameter = new Parameter(this, "Save changed parameter", BooleanToken.FALSE);
    new CheckBoxStyle(persistChangeParameter, "chckbox");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);
    try {
      final String paramName = paramNameParameter.stringValue();
      boolean persistChange = ((BooleanToken)persistChangeParameter.getToken()).booleanValue();
      final String msgValue = msg.getBodyContentAsString();
      ChangeRequest chgReq = new ChangeRequest(this, "SetParameter property change request") {
        protected void _execute() throws IllegalActionException {
          setParamValue(paramName, msgValue);
        }
      };
      chgReq.setPersistent(persistChange);
      requestChange(chgReq);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, msg, e);
    } finally {
      response.addOutputMessage(output, msg);
    }
  }

  private void setParamValue(String paramName, String msgValue) throws IllegalActionException {
    Variable param = (Variable) toplevel().getAttribute(paramName, Variable.class);
    if (param != null) {
      getLogger().debug("Setting parameter {} to {} ", paramName, msgValue);
      param.setToken(msgValue);
    } else {
      // TODO : check if logging a warning is OK; or should it be a ProcessingException?
      getLogger().warn("Parameter {} not found", paramName);
    }
  }
}
