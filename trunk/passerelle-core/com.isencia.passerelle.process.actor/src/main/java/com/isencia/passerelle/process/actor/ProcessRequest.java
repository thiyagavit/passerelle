/* Copyright 2013 - iSencia Belgium NV

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.internal.SettableMessage;
import com.isencia.passerelle.process.model.Context;

/**
 * ProcessRequest is a generic container for request data delivered to an actor in the process domain. 
 * It contains (inputport,message) pairs, related to a same processing <code>Context</code>.
 * <p>
 * A process actor needs to be able to collect input messages belonging to a same process context before
 * executing its <code>process(...)</code> method.
 * </p>
 * 
 * @author erwin
 */
public class ProcessRequest {
  public static final String HEADER_PROCESS_CONTEXT = "__PSRL_PROC_CTXT_ID";
  private final static Collection<ManagedMessage> EMPTY_MSG_COLLECTION = new ArrayList<ManagedMessage>();

  private Context processContext;
  private long iterationCount = 0;
  private Map<String, MessageInputContext> inputContexts = new HashMap<String, MessageInputContext>();

  /**
	 *
	 */
  public ProcessRequest() {
  }

  /**
   * @param processContext
   */
  public ProcessRequest(Context processContext) {
    this.processContext = processContext;
  }

  /**
   * @return Returns the iterationCount.
   */
  public long getIterationCount() {
    return iterationCount;
  }

  /**
   * @param iterationCount
   *          The iterationCount to set.
   */
  public void setIterationCount(long iterationCount) {
    this.iterationCount = iterationCount;
  }

  public boolean addInputMessage(int inputIndex, String inputName, ManagedMessage inputMsg) {
    if (isMessageInContext(inputMsg)) {
      MessageInputContext presentCtxt = inputContexts.get(inputName);
      if (presentCtxt == null) {
        inputContexts.put(inputName, new MessageInputContext(inputIndex, inputName, inputMsg));
      } else {
        presentCtxt.addMsg(inputMsg);
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean addInputContext(MessageInputContext msgCtxt) {
    if (msgCtxt != null) {
      Iterator<ManagedMessage> msgIterator = msgCtxt.getMsgIterator();
      while (msgIterator.hasNext()) {
        ManagedMessage inputMsg = (ManagedMessage) msgIterator.next();
        if (isMessageInContext(inputMsg)) {
          MessageInputContext presentCtxt = inputContexts.get(msgCtxt.getPortName());
          if (presentCtxt == null) {
            inputContexts.put(msgCtxt.getPortName(), msgCtxt);
          } else {
            presentCtxt.addMsg(msgCtxt.getMsg());
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns the most recently received message on the given Port. For PULL ports, only one message can be read from an input port per iteration, and any other
   * buffered messages will be offered one-by-one in consecutive actor iterations. For PUSH ports, multiple messages can be pushed into each actor iteration.
   * This method only returns the most recent one.
   * 
   * @param inputPort
   * @return the most recently received message on the given Port, or null if no message arrived on the port.
   */
  public ManagedMessage getMessage(Port inputPort) {
    if (inputPort != null)
      return getMessage(inputPort.getName());
    else
      return null;
  }

  /**
   * Returns the most recently received message on the given Port. For PULL ports, only one message can be read from an input port per iteration, and any other
   * buffered messages will be offered one-by-one in consecutive actor iterations. For PUSH ports, multiple messages can be pushed into each actor iteration.
   * This method only returns the most recent one.
   * 
   * @param inputName
   * @return the most recently received message on the given Port, or null if no message arrived on the port.
   */
  public ManagedMessage getMessage(String inputName) {
    if (inputName != null) {
      MessageInputContext ctxt = inputContexts.get(inputName);
      return ctxt != null ? ctxt.getMsg() : null;
    } else
      return null;
  }

  /**
   * Returns an iterator on <code>ManagedMessages</code> received on the given input port. If no messages were received on the given port, an "empty" iterator
   * is returned. If the inputPort is null, null is returned.
   * 
   * @param inputPort
   * @return an iterator on <code>ManagedMessages</code> received on the given input port, or null if the input parameter is null.
   * @since Passerelle v8.0
   */
  public Iterator<ManagedMessage> getAllMessages(Port inputPort) {
    if (inputPort != null)
      return getAllMessages(inputPort.getName());
    else
      return null;
  }

  /**
   * Returns an iterator on <code>ManagedMessages</code> received on the given input port. If no messages were received on the given port, an "empty" iterator
   * is returned. If the inputName is null, null is returned.
   * 
   * @param inputName
   * @return an iterator on <code>ManagedMessages</code> received on the given input port, or null if the input parameter is null.
   * @since Passerelle v8.0
   */
  public Iterator<ManagedMessage> getAllMessages(String inputName) {
    if (inputName != null) {
      MessageInputContext ctxt = inputContexts.get(inputName);
      return ctxt != null ? ctxt.getMsgIterator() : EMPTY_MSG_COLLECTION.iterator();
    } else
      return null;
  }

  /**
   * @since Passerelle v4.1.1
   * @return all received input contexts
   */
  public Iterator<MessageInputContext> getAllInputContexts() {
    return inputContexts.values().iterator();
  }

  /**
   * @return an indication whether this request contains at least one MessageInputContext
   */
  public boolean isEmpty() {
    return inputContexts.isEmpty();
  }

  /**
   * @return an indication whether this request contains unprocessed MessageInputContexts
   */
  public boolean hasSomethingToProcess() {
    boolean result = false;
    Collection<MessageInputContext> inpContexts = inputContexts.values();
    for (MessageInputContext messageInputContext : inpContexts) {
      if (result = !messageInputContext.isProcessed())
        break;
    }
    return result;
  }

  public String toString() {
    StringBuffer bfr = new StringBuffer();
    Collection<MessageInputContext> c = inputContexts.values();
    MessageInputContext[] inputs = c.toArray(new MessageInputContext[inputContexts.size()]);
    bfr.append("\n\tInput msgs:");
    for (int i = 0; i < inputs.length; i++) {
      MessageInputContext context = inputs[i];
      if (context != null) {
        bfr.append("\n\t\t" + context.getPortName() + ": msgID="
            + ((context.getMsg() != null && context.getMsg().getID() != null) ? context.getMsg().getID().toString() : "null"));
      }
    }
    return bfr.toString();
  }

  /**
   * @param inputMsg
   * @return true if this <code>ProcessRequest</code> is non-context-specific, or the inputMsg is non-context-specific, or the inputMsg has the required context
   *         id in its headers.
   */
  public boolean isMessageInContext(ManagedMessage inputMsg) {
    if (inputMsg == null) {
      return false;
    } else {
      String[] ctxtIDHdrs = ((SettableMessage) inputMsg).getHeader(HEADER_PROCESS_CONTEXT);
      boolean itsOk = (processContext == null) || (processContext.getId() == null) || (ctxtIDHdrs.length == 0);
      if (!itsOk) {
        for (String ctxtIdHdr : ctxtIDHdrs) {
          if (itsOk = (processContext.getId().equals(ctxtIdHdr))) {
            break;
          }
        }
      }
      return itsOk;
    }
  }

}
