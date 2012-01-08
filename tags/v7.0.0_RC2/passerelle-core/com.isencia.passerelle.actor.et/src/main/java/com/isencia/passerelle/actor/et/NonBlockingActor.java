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

package com.isencia.passerelle.actor.et;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageBuffer;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.message.MessageOutputContext;

/**
 * This is a base class for actors to be used in the experimental "event-driven" domain for Passerelle. The goal is to have the director maintain a
 * "msg-transport-event"-queue. Each msg sent to an input port is queued. The Director thread reads the top-event and sends the msg to the destination port.
 * Then it checks whether the related actor is ready to fire, by calling its prefire(). When prefire() returns true, the actor's fire() method is invoked,
 * followed by postfire(). When prefire() returns false, the director "forgets" about that actor and sends the next msg from its event queue, probably to
 * another actor and tries this one's prefire(). Etc.
 * 
 * @author delerw
 */
public abstract class NonBlockingActor extends Actor implements MessageBuffer {

  private final static Logger LOGGER = LoggerFactory.getLogger(NonBlockingActor.class);

  // Just a counter for the fire cycles.
  // We're using this to be able to show for each input msg on which fire cycle it arrived.
  private long iterationCount = 1;

  // A flag to indicate the special case of a source actor.
  // As these do not have any input ports, so the std algorithm
  // to automatically deduce that the actor can requestFinish() is not valid
  private boolean isSource = true;

  // Collection of msg providers, i.e. typically receivers on input ports
  // that directly feed their received msgs into this MessageBuffer's queue.
  private Collection<Object> msgProviders = new HashSet<Object>();

  // used to track the blocking/PULL inputs that have been exhausted
  protected Map<Port, Boolean> blockingInputFinishRequests = new HashMap<Port, Boolean>();

  // Queue of messages that have been pushed to us, incl info on the input port
  // on which they have been received.
  private Queue<MessageInputContext> pushedMessages = new ConcurrentLinkedQueue<MessageInputContext>();
  // lock to manage blocking on empty pushedMessages queue
  private ReentrantLock msgQLock = new ReentrantLock();
  private Condition msgQNonEmpty = msgQLock.newCondition();

  private ProcessRequest currentProcessRequest;
  private ProcessResponse currentProcessResponse;
  
  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  protected NonBlockingActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
  }

  protected Logger getLogger() {
    return LOGGER;
  }
  @Override
  protected String getExtendedInfo() {
    return "";
  }

  public Queue<MessageInputContext> getMessageQueue() {
    return pushedMessages;
  }

  public boolean acceptInputPort(Port p) {
    if (p == null || p.getContainer() != this) {
      return false;
    }
    if (p instanceof ControlPort || !p.isInput()) {
      return false;
    }
    return PortMode.PUSH.equals(p.getMode());
  }

  public boolean registerMessageProvider(Object provider) {
    getLogger().debug("{} - Registered msgprovider {}", getFullName(), provider);
    return msgProviders.add(provider);
  }

  public boolean unregisterMessageProvider(Object provider) {
    getLogger().debug("{} - Unregistered msgprovider {}", getFullName(), provider);
    return msgProviders.remove(provider);
  }

  public void offer(MessageInputContext ctxt) throws PasserelleException {
    try {
      if (!msgQLock.tryLock(10, TimeUnit.SECONDS)) {
        // if we did not get the lock, something is getting overcharged, so refuse the task
        throw new Exception("Msg Queue lock overcharged for "+getFullName());
      }
      pushedMessages.offer(ctxt);
      msgQNonEmpty.signal();
    } catch (Exception e) {
      throw new PasserelleException("Error storing received msg", ctxt.getMsg(), e);
    } finally {
      try {
        msgQLock.unlock();
      } catch (Exception e) {
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void doInitialize() throws InitializationException {
    getLogger().trace("{} - doInitialize() - entry", getFullName());
    super.doInitialize();

    blockingInputFinishRequests.clear();

    iterationCount = 0;
    currentProcessRequest = new ProcessRequest();
    currentProcessRequest.setIterationCount(++iterationCount);
    currentProcessResponse = null;

    List<Port> inputPortList = this.inputPortList();
    for (Port _p : inputPortList) {
      if (_p.isInput() && !(_p instanceof ControlPort)) {
        isSource = false;
        if (PortMode.PULL.equals(_p.getMode())) {
          blockingInputFinishRequests.put(_p, Boolean.FALSE);
        }
      }
    }
    
    if(isSource) {
      try {
        getDirector().fireAtCurrentTime(this);
      } catch (IllegalActionException e) {
        throw new InitializationException("Error triggering a fire iteration for source actor "+getFullName(), this, e);
      }
    }

    getLogger().trace("{} - doInitialize() - exit", getFullName());
  }

  /**
   * Overridable method to allow custom collecting and addition of received pushed input messages. TODO : check which is the right behaviour on the base class
   * of the following : By default, this just reads whatever's received (possibly nothing). But alternatively, this could e.g. block till at least one pushed
   * msg was received etc.
   * 
   * @param req
   * @throws ProcessingException
   */
  protected void addPushedMessages(ProcessRequest req) throws ProcessingException {
    getLogger().trace("{} - addPushedMessages() - entry", getFullName());
    try {
      if (!msgQLock.tryLock(10, TimeUnit.SECONDS)) {
        // if we did not get the lock, something is getting overcharged, so refuse the task
        throw new ProcessingException("Msg Queue lock overcharged...", this, null);
      }
      while (!isFinishRequested() && !areAllInputsFinished() && pushedMessages.isEmpty()) {
        msgQNonEmpty.await(100, TimeUnit.MILLISECONDS);
      }

      while (!pushedMessages.isEmpty()) {
        req.addInputContext(pushedMessages.poll());
      }
      ;
    } catch (InterruptedException e) {
      throw new ProcessingException("Msg Queue lock interrupted...", this, null);
    } finally {
      try {
        msgQLock.unlock();
      } catch (Exception e) {
      }
      getLogger().trace("{} - addPushedMessages() - exit", getFullName());
    }
  }

  /**
   * @return true when all input ports are exhausted
   */
  protected boolean areAllInputsFinished() {
    boolean result = true;
    Collection<Boolean> portFinishIndicators = blockingInputFinishRequests.values();
    for (Boolean portFinishedIndicator : portFinishIndicators) {
      result = result && portFinishedIndicator;
    }
    return result && msgProviders.isEmpty();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean doPreFire() throws ProcessingException {
    getLogger().trace("{} - doPreFire() - entry", getFullName());

    boolean readyToFire = super.doPreFire();

    if (!isSource) {
      List<Port> inputPortList = this.inputPortList();
      for (Port _p : inputPortList) {
        if (_p.isInput() && !(_p instanceof ControlPort)) {
          // check if the process request is complete,
          // i.e. for all PULL(==blocking) inputs we need to have received an input msg
          if (PortMode.PULL.equals(_p.getMode())) {
            ManagedMessage msg = null;
            // If a port is exhausted, we just pass a null msg to the request.
            // If not, we try to read another msg from it.
            // A null msg indicates that the port is exhausted.
            if (!blockingInputFinishRequests.get(_p).booleanValue()) {
              // For the moment, we only read at most one msg per PULL input port.
              // Like for the v5.Actor, msg streams should be handled via PUSH ports.
              if (currentProcessRequest.getMessage(_p) == null) {
                boolean portHasToken = false;
                for (int i = 0; i < _p.getWidth(); ++i) {
                  try {
                    if (_p.hasToken(i)) {
                      portHasToken = true;
                      Token t = _p.get(i);
                      msg = MessageHelper.getMessageFromToken(t);
                      break;
                    }
                  } catch (Exception e) {
                    throw new ProcessingException("Error getting messages from input ports", this, e);
                  }
                }
                if (!portHasToken) {
                  readyToFire = false;
                  break;
                }
              }
            }
            currentProcessRequest.addInputMessage(0, _p.getName(), msg);
          }
        }
      }

      if (readyToFire) {
        if (!msgProviders.isEmpty() || !pushedMessages.isEmpty()) {
          // we've got at least one PUSH port that registered a msg provider
          // so we need to include all pushed msgs in the request as well
          addPushedMessages(currentProcessRequest);
        }

        // when all ports are exhausted, we can stop this actor
        if (areAllInputsFinished()) {
          requestFinish();
        }

        readyToFire = currentProcessRequest.hasSomethingToProcess();
      }
    }

    getLogger().trace("{} - doPreFire() - exit : {}", getFullName(), readyToFire);
    return readyToFire;
  }

  @Override
  protected void doFire() throws ProcessingException {
    if (isSource || currentProcessRequest.hasSomethingToProcess()) {
      getLogger().trace("{} - doFire() - processing request {}", getFullName(), currentProcessRequest);

      ActorContext ctxt = new ActorContext();
      currentProcessResponse = new ProcessResponse(currentProcessRequest);
      process(ctxt, currentProcessRequest, currentProcessResponse);

      getLogger().trace("{} - doFire() - obtained response {}", getFullName(), currentProcessResponse);
    }
  }

  @Override
  protected boolean doPostFire() throws ProcessingException {
    getLogger().trace("{} - doPostFire() - entry", getFullName());

    if (currentProcessResponse != null) {
      // send outputs
      // Mark the contexts as processed.
      // Not sure if this is still relevant for v5 actors, as even PUSHed messages are assumed to be handled once, 
      // in the iteration when they are offered to process().
      Iterator<MessageInputContext> allInputContexts = currentProcessRequest.getAllInputContexts();
      while (allInputContexts.hasNext()) {
        MessageInputContext msgInputCtxt = allInputContexts.next();
        msgInputCtxt.setProcessed(true);
      }

      // and now send out the results
      MessageOutputContext[] outputs = currentProcessResponse.getOutputs();
      if (outputs != null) {
        for (MessageOutputContext output : outputs) {
          sendOutputMsg(output.getPort(), output.getMessage());
        }
      }
      outputs = currentProcessResponse.getOutputsInSequence();
      if (outputs != null && outputs.length > 0) {
        Long seqID = MessageFactory.getInstance().createSequenceID();
        for (int i = 0; i < outputs.length; i++) {
          MessageOutputContext context = outputs[i];
          boolean isLastMsg = (i == (outputs.length - 1));
          try {
            ManagedMessage msgInSeq = MessageFactory.getInstance().createMessageCopyInSequence(context.getMessage(), seqID, new Long(i), isLastMsg);
            sendOutputMsg(context.getPort(), msgInSeq);
          } catch (MessageException e) {
            throw new ProcessingException("Error creating output sequence msg for msg " + context.getMessage().getID(), context.getMessage(), e);
          }
        }
      }
    }

    currentProcessResponse = null;
    
    boolean result = super.doPostFire();
    if (result) {
      // create new proc req for next iteration
      iterationCount++;
      currentProcessRequest = new ProcessRequest();
      currentProcessRequest.setIterationCount(iterationCount);
    }

    getLogger().trace("{} - doPostFire() - exit : {}", getFullName(), result);
    return result;
  }

  /**
   * @param ctxt
   * @param request
   * @param response
   * @throws ProcessingException
   */
  protected abstract void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException;

  @Override
  public Object clone(Workspace workspace) throws CloneNotSupportedException {
    final NonBlockingActor actor = (NonBlockingActor) super.clone(workspace);
    actor.blockingInputFinishRequests = new HashMap<Port, Boolean>();
    actor.pushedMessages = new ConcurrentLinkedQueue<MessageInputContext>();
    actor.msgProviders = new HashSet<Object>();
    return actor;
  }
}
