/* Copyright 2014 - iSencia Belgium NV

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
package org.passerelle.python.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import uk.ac.diamond.python.service.PythonService;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.dynaport.InputPortConfigurationExtender;
import com.isencia.passerelle.actor.dynaport.OutputPortConfigurationExtender;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * A basic Python actor, binding input and output variables from a python script
 * to the actor's input&output ports.
 * <p>
 * To use this actor you need to set one or more input ports, e.g. 2 ports "input1,input2" and output ports, e.g. "output1,output2".
 * The script can then refer to the input variables and can create result/output variables with the corresponding port names.
 * The actor will send the individual produced outputs on the respectively named ports.
 * The complete result map wil be sent out on the actor's default output port as well.
 * <br/>
 * Below is a simple example that would work for the above ports configuration :<br/>
 * <pre>
 * def run(input1, input2, **kwargs):
 *     print "hello world : %s and %s" % (input1 , input2)
 *     return {"output1": "isn't this nice", "output2": "imagine this"} 
 * </pre>
 * </p>
 * <p>
 * The actor uses the DAWN RPC-based {@link PythonService} to run the script on a separate "true" Python interpreter process.
 * I.e. no Jython or so.<br/>
 * A new python process is launched each time, for each individual script execution. 
 * </p>
 * @author erwindl
 *
 */
public class PythonActor extends Actor {
  private static final long serialVersionUID = -2069802354336022650L;
  private final static Logger LOGGER = LoggerFactory.getLogger(PythonActor.class);
  private final static String PYTHONINTERPRETER_DEFAULT = "python";

  public Port output;
  
  public InputPortConfigurationExtender inputPortCfgExtender;
  public OutputPortConfigurationExtender outputPortCfgExtender;

  public FileParameter scriptPathParameter;
  public FileParameter pythonInterpreterParam;

  public PythonActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);
    
    inputPortCfgExtender = new InputPortConfigurationExtender(this, "Input ports");
    outputPortCfgExtender = new OutputPortConfigurationExtender(this, "Output ports");

    scriptPathParameter = new FileParameter(this, "Script path");
    pythonInterpreterParam = new FileParameter(this, "Python path");
    registerExpertParameter(pythonInterpreterParam);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
        List<ManagedMessage> inputMsgs = new ArrayList<ManagedMessage>();
        Map<String, Object> inputs = new HashMap<String, Object>();
        List<Port> inputPorts = inputPortCfgExtender.getInputPorts();
        for(Port input : inputPorts) {
          ManagedMessage message = request.getMessage(input);
          inputs.put(input.getName(), message.getBodyContentAsString());
          inputMsgs.add(message);
        }
        final Map<String, ? extends Object> result = getTransformedMessage(inputs);
        if (result != null) {
          ManagedMessage resultMsg = createMessageFromCauses(inputMsgs.toArray(new ManagedMessage[inputMsgs.size()]));
          resultMsg.setBodyContent(result, ManagedMessage.objectContentType);
          sendOutputMsg(output, resultMsg);
          
          List<Port> outputPorts = outputPortCfgExtender.getOutputPorts();
          for (Port output : outputPorts) {
            Object resObj = result.get(output.getName());
            if(resObj!=null) {
              resultMsg = createMessageFromCauses(inputMsgs.toArray(new ManagedMessage[inputMsgs.size()]));
              resultMsg.setBodyContent(resObj, ManagedMessage.objectContentType);
              sendOutputMsg(output, resultMsg);
            }
          }
        }
    } catch (ProcessingException pe) {
      throw pe;
    } catch (Exception ne) {
      throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Cannot handle input msgs", this, null, ne);
    }
  }

  private Map<String, ? extends Object> getTransformedMessage(Map<String, Object> data) throws ProcessingException {
    PythonService service = null;
    try {
      String pythonInterpreter = pythonInterpreterParam.stringValue();
      if(pythonInterpreter==null || pythonInterpreter.trim().length()==0) {
        pythonInterpreter = PYTHONINTERPRETER_DEFAULT;
      }
      service = PythonService.openConnection(pythonInterpreter);
      String scriptPath = scriptPathParameter.asFile().getAbsolutePath();

      final Map<String, ? extends Object> result;
      try {
        result = service.runScript(scriptPath, data);
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error from python script", this, e);
      }
      return result;
    } catch (ProcessingException e) {
      throw e;
    } catch (Throwable e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error preparing for python script", this, e);
    } finally {
      if (service != null)
        service.stop();
    }
  }

  public Logger getLogger() {
    return LOGGER;
  }
}
