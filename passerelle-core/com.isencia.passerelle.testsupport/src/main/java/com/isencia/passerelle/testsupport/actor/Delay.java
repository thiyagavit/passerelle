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
package com.isencia.passerelle.testsupport.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * @author erwin
 */
public class Delay extends Actor {
  private final static Logger LOGGER = LoggerFactory.getLogger(Delay.class);

  public Parameter timeParameter = null;
  public Port input;
  public Port output;

  /**
   * Construct an actor with the given container and name.
   * 
   * @param container The container.
   * @param name The name of this actor.
   * @exception IllegalActionException If the actor cannot be contained by the proposed container.
   * @exception NameDuplicationException If the container already has an actor with this name.
   */
  public Delay(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);

    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);

    timeParameter = new Parameter(this, "time(s)", new IntToken(1));
    timeParameter.setTypeEquals(BaseType.INT);
    registerConfigurableParameter(timeParameter);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    // The code below is for the case where we configure the input port as PUSH.
    // Then it is possible that multiple msgs are received in one iteration.
    // When the input port is in PULL mode, there will always only be one received msg.
    ManagedMessage msg = request.getMessage(input);
    int time = Integer.parseInt(timeParameter.getExpression());
    try {
      if (time > 0) {
        for (int i = 0; i < time; ++i) {
          Thread.sleep(1000);
          if (isFinishRequested()) {
            break;
          }
        }
      }
    } catch (InterruptedException e) {
      // do nothing, means someone wants us to stop
    } catch (Exception e) {
      throw new ProcessingException("[PASS-EX-1111] - Error in delay processing", this, e);
    }

    response.addOutputMessage(output, msg);
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }
}
