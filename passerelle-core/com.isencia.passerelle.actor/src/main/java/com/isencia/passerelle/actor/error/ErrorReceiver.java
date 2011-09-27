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

package com.isencia.passerelle.actor.error;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * Registers itself as an ErrorCollector, and then sends out each received error
 * as an ErrorMessage on its output port.
 * 
 * @author erwin
 */
public class ErrorReceiver extends Actor implements ErrorCollector {

  private final static Logger logger = LoggerFactory.getLogger(ErrorReceiver.class);

  private BlockingQueue<PasserelleException> errors = new LinkedBlockingQueue<PasserelleException>();

  public Port output;

  public ErrorReceiver(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);

        _attachText("_iconDescription", 
                "<svg>\n" + 
                "<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:red;stroke:red\"/>\n" + 
                "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" style=\"stroke-width:1.0;stroke:white\"/>\n" + 
                "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" style=\"stroke-width:1.0;stroke:white\"/>\n" + 
                "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n" + 
                "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n" + 
                "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n" + 
                "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n" + 
                "<circle cx=\"0\" cy=\"0\" r=\"10\" style=\"fill:white;stroke-width:2.0\"/>\n" + 
                "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n" + 
                "<line x1=\"-3\" y1=\"-3\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n" + 
                "<line x1=\"3\" y1=\"-3\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n" + 
                "</svg>\n");
	}

	@Override
	protected void doInitialize() throws InitializationException {
		super.doInitialize();
		
    try {
      ((Director) getDirector()).addErrorCollector(this);
    } catch (ClassCastException e) {
      // means the actor is used without a Passerelle Director
      // just log this. Only consequence is that we'll never receive
      // any error messages via acceptError
      logger.info(getInfo() + " - used without Passerelle Director!!");
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    // ErrorReceiver has no data input ports,
    // so it's like a Source in the days of the original Actor API.
    // The BlockingQueue (errors) is our data feed.
    try {
      PasserelleException e = errors.poll(1, TimeUnit.SECONDS);
      if (e != null) {
        ManagedMessage msg = createErrorMessage(e);
        response.addOutputMessage(0, output, msg);
        drainErrorsQueueTo(response);
      }
    } catch (InterruptedException e) {
      // should not happen,
      // or if it does only when terminating the model execution
      // and with an empty queue, so we can just finish then
      requestFinish();
    }
  }

  private void drainErrorsQueueTo(ProcessResponse response) throws ProcessingException {
    while (!errors.isEmpty()) {
      PasserelleException e = errors.poll();
      if (e != null) {
        ManagedMessage msg = createErrorMessage(e);
        if (response != null) {
          response.addOutputMessage(0, output, msg);
        } else {
          sendOutputMsg(output, msg);
        }
      } else {
        break;
      }
    }
  }

  public void acceptError(PasserelleException e) {

    try {
      errors.put(e);
    } catch (InterruptedException e1) {
      // should not happen,
      // or if it does only when terminating the model execution
      logger.error("Receipt interrupted for ", e);
    }
  }

  @Override
  protected void doWrapUp() throws TerminationException {
    try {
      ((Director) getDirector()).removeErrorCollector(this);
    } catch (ClassCastException e) {
    }

    try {
      drainErrorsQueueTo(null);
    } catch (Exception e) {
      throw new TerminationException(getInfo() + " - doWrapUp() generated exception " + e, errors, e);
    }

    super.doWrapUp();
  }
}
