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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.message.internal.ErrorMessageContainer;
import com.isencia.passerelle.message.internal.ErrorMessageListContainer;

/**
 * An ErrorCollector actor collects all error msgs received during a model run,
 * and when the model closes down it sends them out via its output port (during
 * its wrapup()).
 * <p>
 * Error msgs can be received via the
 * {@link com.isencia.passerelle.ext.ErrorCollector} interface mechanism and/or
 * via the input port. This actor assumes that msgs arriving at its input port
 * are {@link ErrorMessageContainer}s, or if not it creates a default error msg
 * referring to the received msg.
 * </p>
 * 
 * @author erwin
 */
public class ErrorCollector extends Actor implements com.isencia.passerelle.ext.ErrorCollector {
  final private static Logger logger = LoggerFactory.getLogger(ErrorCollector.class);

  /**
   * The input port. This base class imposes no type constraints except that the
   * type of the input cannot be greater than the type of the output.
   */
  public Port input;
  private PortHandler inputHandler = null;

  /**
   * The output port. By default, the type of this output is constrained to be
   * at least that of the input.
   */
  public Port output;

  private List errorsReceived = new ArrayList();

  /**
   * @param container
   * @param name
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public ErrorCollector(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, "input", null);
    output = PortFactory.getInstance().createOutputPort(this, "output");
  }

  protected void doInitialize() throws InitializationException {
    if (logger.isTraceEnabled()) {
      logger.trace(getInfo());
    }

    inputHandler = new PortHandler(input);
    if (input.getWidth() > 0) {
      inputHandler.start();
    }

    errorsReceived.clear();
    try {
      ((Director) getDirector()).addErrorCollector(this);
    } catch (ClassCastException e) {
      // means the actor is used without a Passerelle Director
      // just log this. Only consequence is that we'll never receive
      // any error messages via acceptError
      logger.info(getInfo() + " - used without Passerelle Director!!");
    }

    if (logger.isTraceEnabled()) {
      logger.trace(getInfo() + " - exit ");
    }
  }

  protected void doFire() throws ProcessingException {
    if (logger.isTraceEnabled()) logger.trace(getInfo());

    isFiring = false;
    Token token = inputHandler.getToken();
    isFiring = true;

    if (token != null) {
      ManagedMessage message = null;
      try {
        message = MessageHelper.getMessageFromToken(token);
      } catch (PasserelleException e) {
        throw new ProcessingException("Error reading message from token", token, e);
      }
      if (logger.isDebugEnabled()) {
        logger.debug(getInfo() + " - Transformer received message :" + message);
      }
      doFire(message);
    } else {
      // should mean that no messages will arrive on input port
      // but we've got to keep this actor alive till it's explicitly stopped
      // TODO : think about a way to have it stop automatically when it's the
      // only active one left in the model.
      performWait(-1);
    }

    if (logger.isTraceEnabled()) logger.trace(getInfo() + " - exit ");
  }

  protected void doFire(ManagedMessage message) throws ProcessingException {
    if (logger.isTraceEnabled()) {
      logger.trace(getInfo() + " - received " + message);
    }
    if (message != null) {
      if (logger.isDebugEnabled()) {
        logger.debug(getInfo() + " - adding error message " + message);
      }
      if (message instanceof ErrorMessageContainer) {
        errorsReceived.add(message);
      } else {
        errorsReceived.add(createErrorMessage(new ProcessingException("misc error rcv " + new Date().toString(), message, null)));
      }
    }
    if (logger.isTraceEnabled()) {
      logger.trace(getInfo() + " - exit ");
    }
  }

  protected void doWrapUp() throws TerminationException {
    if (logger.isTraceEnabled()) {
      logger.trace(getInfo());
    }

    try {
      // TODO : check timing and concurrency issues if we don't want to loose
      // error messages
      try {
        ((Director) getDirector()).removeErrorCollector(this);
      } catch (ClassCastException e) {
        // means the actor is used without a Passerelle Director
        // just log this. Only consequence is that we'll never receive
        // any error messages via acceptError
        logger.info(getInfo() + " - used without Passerelle Director!!");
      }

      if (errorsReceived.size() > 0) {
        ErrorMessageListContainer errMsgList = new ErrorMessageListContainer();
        for (Iterator errItr = errorsReceived.iterator(); errItr.hasNext();) {
          ErrorMessageContainer dataMsg = (ErrorMessageContainer) errItr.next();
          errMsgList.addErrorMessage(dataMsg);
        }
        try {
          sendOutputMsg(output, errMsgList);
        } catch (Exception e) {
          throw new TerminationException(getInfo() + " - doWrapUp() generated exception " + e, errMsgList, e);
        }

        if (getAuditLogger().isInfoEnabled()) {
          getAuditLogger().info("Sent error message with " + errorsReceived.size() + " entries");
        }

      }
    } finally {
      super.doWrapUp();
    }

    if (logger.isTraceEnabled()) {
      logger.trace(getInfo() + " - exit ");
    }
  }

  protected String getAuditTrailMessage(ManagedMessage message, Port port) {
    // specific audit logging done in wrapup()
    return null;
  }

  protected String getExtendedInfo() {
    return "";
  }

  public void acceptError(PasserelleException e) {
    if (logger.isTraceEnabled()) {
      logger.trace(getInfo() + " - received :" + e);
    }
    if (e != null) {
      if (logger.isDebugEnabled()) {
        logger.debug(getInfo() + " - adding error " + e);
      }
      errorsReceived.add(createErrorMessage(e));
    }
    if (logger.isTraceEnabled()) {
      logger.trace(getInfo() + " - exit ");
    }
  }

  private synchronized void performWait(int time) {
    if (logger.isTraceEnabled()) logger.trace(getInfo() + " :" + time);
    try {
      if (time == -1)
        wait();
      else
        wait(time);
    } catch (InterruptedException e) {
      requestFinish();
    }
    if (logger.isTraceEnabled()) logger.trace(getInfo() + " - exit ");
  }

  private synchronized void performNotify() {
    if (logger.isTraceEnabled()) logger.trace(getInfo());
    notify();
    if (logger.isTraceEnabled()) logger.trace(getInfo() + " - exit ");
  }

  protected void doStopFire() {
    performNotify();
  }

  protected void doStop() {
    performNotify();
  }
}
