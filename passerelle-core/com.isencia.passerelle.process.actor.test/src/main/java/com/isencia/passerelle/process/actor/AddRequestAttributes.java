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
package com.isencia.passerelle.process.actor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.service.ServiceRegistry;

/**
 * @author puidir
 */
public class AddRequestAttributes extends Actor {

  private static final long serialVersionUID = 1L;

  private Map<String, String> params = new HashMap<String, String>();

  public Port input;
  public Port output;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public AddRequestAttributes(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);
  }

  public void addParameter(String key, String value) {
    params.put(key, value);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage message = request.getMessage(input);
    Context requestContext;
    try {
      requestContext = (Context) message.getBodyContent();
    } catch (MessageException ex) {
      throw new ProcessingException(ErrorCode.REQUEST_CONTENTS_ERROR,"Error reading msg contents", this, message, ex);
    }
    if (requestContext != null) {
      EntityFactory entityFactory = ServiceRegistry.getInstance().getEntityFactory();
      for (Entry<String, String> entry : params.entrySet()) {
        entityFactory.createAttribute(requestContext.getRequest(), entry.getKey(), entry.getValue());
      }
      ManagedMessage outputMessage;
      try {
        outputMessage = createMessage(requestContext, ManagedMessage.objectContentType);
      } catch (MessageException ex) {
        throw new ProcessingException(ErrorCode.REQUEST_CONTENTS_ERROR,"Error constructing output msg", this, message, ex);
      }
      sendOutputMsg(output, outputMessage);
    } else {
      sendOutputMsg(output, message);
    }
  }
}
