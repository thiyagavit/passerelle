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

import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.message.ManagedMessage;

import fr.soleil.passerelle.actor.TransformerV5;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;

/**
 * A basic actor that reads an input message and sets its body as the value of a selected
 * parameter. The parameter can be anywhere in the model, e.g. as a (sub)model parameter or
 * as an actor parameter.
 * <p>
 * The name of the parameter-to-be-modified must be manually specified. 
 * I.e. there is no automated lookup (yet) of all parameters present in the actor's parent model.
 * </p>
 * @author delerw
 */

@SuppressWarnings("serial")
public class SetParameter extends TransformerV5 {
  private final static Logger LOGGER = LoggerFactory.getLogger(SetParameter.class);

  public StringParameter paramNameParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public SetParameter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    paramNameParameter = new StringParameter(this, "Parameter to modify");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  } 
  
  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);
    try {
      String paramName = paramNameParameter.stringValue();
      Variable param = (Variable) toplevel().getAttribute(paramName, Variable.class);
      if(param!=null) {
        String msgValue = msg.getBodyContentAsString();
        getLogger().debug("Setting parameter {} to {} ",paramName, msgValue);
        param.setToken(msgValue);
      } else {
        // TODO : check if logging a warning is OK; or should it be a ProcessingException?
        getLogger().warn("Parameter {} not found", paramName);
        throw new ProcessingExceptionWithLog(this,Severity.FATAL, "Parameter" + paramName + " not found ", this, null);
      }
    } catch (Exception e) {
      //throw new ProcessingExceptionWit(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, msg, e);
        
      throw new ProcessingExceptionWithLog(this,Severity.FATAL, "", this, e);
    } finally {
      response.addOutputMessage(output, msg);
    }
  }

}
