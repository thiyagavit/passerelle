/* Copyright 2013 - Synchrotron Soleil

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
package fr.soleil.passerelle.cdma.actor;

import org.cdma.interfaces.IArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.isencia.passerelle.message.MessageFactory;

/**
 * Base class for actors that receive a CDMA IArray, do some modifications/transformations on it, and send out the transformed/modified IArray.
 * 
 * @author delerw
 */
public abstract class CDMAArrayTransformer extends Actor {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(CDMAArrayTransformer.class);
  
  public Port input;
  // the port where a transformed output is sent, if there is one
  // some transformers may not process all received messages one-by-one, so may only generate transformed outputs irregularly
  public Port output;
  // the port where the received msg is just forwarded untransformed
  public Port forward;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public CDMAArrayTransformer(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, IArray.class);
    output = PortFactory.getInstance().createOutputPort(this);
    forward = PortFactory.getInstance().createOutputPort(this, "forward");
  }

  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public final void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);
    try {
      IArray rcvdArray = (IArray) msg.getBodyContent();
      IArray trfdArray = transformArray(rcvdArray);
      if (trfdArray != null) {
        ManagedMessage copyMessage = MessageFactory.getInstance().createCausedCopyMessage(msg);
        copyMessage.setBodyContent(trfdArray, ManagedMessage.objectContentType);
        response.addOutputMessage(output, copyMessage);
      }
      response.addOutputMessage(forward, msg);
    } catch (ProcessingException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, e);
    }
  }

  /**
   * This is where the real work on the received IArray must be done. The resulting array must be returned. <br/>
   * This result can be either a new array instance, or the received one with modified values, transformed shape etc.
   * 
   * @param rcvdArray
   * @return the new/modified/transformed array
   * @throws ProcessingException
   */
  protected abstract IArray transformArray(IArray rcvdArray) throws ProcessingException;
}
