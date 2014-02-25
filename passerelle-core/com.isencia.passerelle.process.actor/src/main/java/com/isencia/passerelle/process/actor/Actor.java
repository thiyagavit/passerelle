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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageBuffer;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.message.MessageOutputContext;
import com.isencia.passerelle.message.MessageProvider;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.message.internal.SettableMessage;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.service.ContextRepository;
import com.isencia.passerelle.process.service.ServiceRegistry;

/**
 * Continuing on the track started with the "v3" and "v5" Actor APIs, the process-context-aware API offers further
 * enhancements in Actor development features, combined with runtime enhancements.
 * <p>
 * Similar to the v5 Actor API, a process-actor hides the complexity of push/pull port management, threading etc. But
 * whereas plain actors only care about the receipt of any arbitrary msg on a given port, a process-actor knows about
 * specific request processing contexts to which incoming message may be related.
 * </p>
 * <p>
 * In "streaming"-mode model runs, where actors may be receiving many consecutive messages on their input ports, during
 * one run, such process-context-aware actors must be able to handle mixed ordering of received messages. <br/>
 * Practically, this implies that actors with multiple data-input-ports must contain a kind of internal buffering for
 * received messages. Based on a definition of mandatory and optional inputs (using the default approach of
 * blocking/non-blocking a.k.a. pull/push port modes), message may need to be kept waiting a while until all required
 * messages have been received for a same context. <br/>
 * Only then should the <code>process(...)</code> method be invoked with a <code>ProcessRequest</code> containing all
 * the related messages.
 * </p>
 * 
 * @author erwin
 */

public abstract class Actor extends com.isencia.passerelle.actor.Actor implements ProcessActor, MessageBuffer {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(Actor.class);

  // Just a counter for the fire cycles.
  // We're using this to be able to show for each input msg on which fire cycle it arrived.
  private long iterationCount = 0;

  // A flag to indicate the special case of a source actor.
  // As these do not have any input ports, so the std algorithm
  // to automatically deduce that the actor can requestFinish() is not valid.
  private boolean isSource = true;

  // Collection of msg providers, i.e. typically receivers on input ports
  // that directly feed their received msgs into this MessageBuffer's queue.
  private Collection<Object> msgProviders = new HashSet<Object>();

  // Queue of messages that have been pushed to us, incl info on the input port on which
  // they have been received.
  private Queue<MessageInputContext> pushedMessages = new ConcurrentLinkedQueue<MessageInputContext>();
  // lock to manage blocking on empty pushedMessages queue and to synchronize concurrent access
  private ReentrantLock msgQLock = new ReentrantLock();
  private Condition msgQNonEmpty = msgQLock.newCondition();

  // These collections maintain current processing containers between prefire/fire/postfire.
  // ========================================================================================

  // TODO evaluate if these should not be managed centrally, e.g. attached to the <code>Director</code> or so, linked to
  // the event queue i.c.o. an <code>ETDirector</code> etc

  // This map contains all process requests for which some data has been received, but still not all required data. 
  // They are mapped to their context ID (in string format, as stored in the msg header).
  private Map<String, ProcessRequest> incompleteProcessRequests = new ConcurrentHashMap<String, ProcessRequest>();
  // This queue contains prepared response containers for the process requests
  // that have received all required inputs for their context and are waiting to be processed.
  // When the actor's processing resources have a free slot, they look in this queue for new work.
  private Queue<ProcessResponse> pendingProcessRequests = new LinkedBlockingQueue<ProcessResponse>();
  // This queue contains finished work, ready to be inspected for messages that must be sent out etc.
  private Queue<ProcessResponse> finishedProcessResponses = new LinkedBlockingQueue<ProcessResponse>();

  // Parameter to specify an optional buffer time between actor processing iterations.
  // This can be useful for streaming-mode executions, where actors may be able to optimize their work
  // when they can process many msgs/events in one shot, i.o. one-by-one.
  public Parameter bufferTimeParameter;

  private ContextRepository contextRepository;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Actor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    bufferTimeParameter = new Parameter(this, "Buffer time (ms)", new IntToken(0));
    registerExpertParameter(bufferTimeParameter);
  }

  @Override
  protected String getExtendedInfo() {
    return "";
  }

  public Queue<MessageInputContext> getMessageQueue() {
    return pushedMessages;
  }

  /**
   * Process-aware actors that must support out-of-order multiple inputs for their processing contexts, require
   * non-blocking input ports. This is implemented via the <code>MessageBuffer</code> & <code>MessageProvider</code>
   * system.
   * <p>
   * Via this method call, <code>Port</code>s sniff around a bit on their <code>Actor</code> to check if it wants to act
   * as a <code>MessageBuffer</code> for the <code>Port</code>. So for process-aware actors, this call will return
   * <code>true</code> for all data input ports that belong to this actor instance.
   * </p>
   */
  public boolean acceptInputPort(Port p) {
    if (p == null || p.getContainer() != this) {
      return false;
    }
    if (p instanceof ControlPort || !p.isInput()) {
      return false;
    }
    return true;
  }

  /**
   * Registers a <code>MessageProvider</code>, so the actor knows there's someone out there that may feed messages to
   * it. This is important to allow the actor to determine when it can wrap-up.
   */
  public boolean registerMessageProvider(MessageProvider provider) {
    getLogger().debug("{} - Registered msgprovider {}", getFullName(), provider);
    return msgProviders.add(provider);
  }

  /**
   * Unregisters a <code>MessageProvider</code>, so the actor knows that it should not expect anything anymore from this
   * one. This is important to allow the actor to determine when it can wrap-up.
   */
  public boolean unregisterMessageProvider(MessageProvider provider) {
    getLogger().debug("{} - Unregistered msgprovider {}", getFullName(), provider);
    return msgProviders.remove(provider);
  }

  /**
   * This method is called each time a message is received on receivers of the actor's input ports, for the ports that
   * have been accepted via <code>acceptInputPort()</code>.
   * <p>
   * In this way the ports/receivers are able to push messages directly into the common msg queue of the actor, i.o.
   * forcing the actor to try to get its input messages from a number of <code>BlockingQueue</code>s, one for each
   * blocking input port.
   * </p>
   */
  public void offer(MessageInputContext ctxt) throws PasserelleException {
    getLogger().debug("{} - offer {}", getFullName(), ctxt);
    try {
      if (!msgQLock.tryLock(10, TimeUnit.SECONDS)) {
        // if we did not get the lock, something is getting overcharged, so refuse the task
        throw new Exception("Msg Queue lock overcharged for " + getFullName());
      }
      pushedMessages.offer(ctxt);
      msgQNonEmpty.signal();
    } catch (Exception e) {
      throw new PasserelleException(ErrorCode.MSG_DELIVERY_FAILURE, "Error storing received msg", this, ctxt.getMsg(), e);
    } finally {
      try {
        msgQLock.unlock();
      } catch (Exception e) {
      }
    }
  }

  @Override
  protected void doPreInitialize() throws InitializationException {
    super.doPreInitialize();
    // need to do this here, as in thread-based models
    // the doInitialize could cause race conditions with preceeding actors
    // that already started sending msgs!
    iterationCount = 1;
    pushedMessages.clear();
    incompleteProcessRequests.clear();
    pendingProcessRequests.clear();
    finishedProcessResponses.clear();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void doInitialize() throws InitializationException {
    super.doInitialize();

    List<Port> inputPortList = this.inputPortList();
    for (Port _p : inputPortList) {
      if (_p.isInput() && !(_p instanceof ControlPort)) {
        isSource = false;
        break;
      }
    }

    contextRepository = ServiceRegistry.getInstance().getContextRepository();
    if (contextRepository == null) {
      // TODO check if we can not find a way to run models ico missing ContextRepository
      // e.g. do as for plain v5 Actor : add all pushed msgs to a same ProcessRequest,
      // or still try to generate context IDs in some way and just group per ID or ...
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "ContextRepository service not found", this, null);
    }

    try {
      triggerFirstIteration();
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error triggering a fire iteration for source actor " + getFullName(), this, e);
    }
  }

  protected ContextRepository getContextRepository() {
    return contextRepository;
  }

  /**
   * Checks if any messages have been received since the previous iteration. If so, tries to aggregate them per
   * <code>Context</code>. If a <code>ProcessRequest</code> has a complete set of messages, it is stored in a
   * "pending-for-processing" queue.
   */
  @Override
  protected boolean doPreFire() throws ProcessingException {
    getLogger().trace("{} - doPreFire() - entry", getFullName());

    boolean readyToFire = super.doPreFire();
    if (!isSource) {
      if (hasPushedMessages() || (readyToFire && !msgProviders.isEmpty())) {
        try {
          int bufferTime = ((IntToken) bufferTimeParameter.getToken()).intValue();
          if (bufferTime > 0) {
            getLogger().debug("{} - doPreFire() - sleeping for buffer time {}", getFullName(), bufferTime);
            Thread.sleep(bufferTime);
          }
        } catch (Exception e) {
          getLogger().warn(getFullName() + " - Failed to enforce buffer time", e);
        }
        // we need to check all pushed msgs and group them according to their contexts
        aggregatePushedMessages();
      }
      readyToFire = !pendingProcessRequests.isEmpty();
      // when all ports are exhausted, and no messages have arrived in the meantime, we can stop this actor
      if (!readyToFire && areAllInputsFinished() && !hasPushedMessages()) {
        requestFinish();
      }
    } else {
      // For sources, we feed in an empty <code>ProcessRequest</code> anyway.
      // It's up to the <code>process()</code> implementation to define when the source must finish and wrap up.
      ProcessRequest currentProcessRequest = new ProcessRequest();
      currentProcessRequest.setIterationCount(iterationCount);
      pendingProcessRequests.add(new ProcessResponse(getActorContext(), currentProcessRequest));
    }
    getLogger().trace("{} - doPreFire() - exit : {}", getFullName(), readyToFire);
    return readyToFire;
  }

  /**
   * Pop a pending process request from the queue, if any, and process it.
   */
  @Override
  protected void doFire() throws ProcessingException {
    getLogger().trace("{} - doFire() - entry", getFullName());
    if (!pendingProcessRequests.isEmpty()) {
      ProcessResponse currentProcessResponse = pendingProcessRequests.poll();
      ActorContext ctxt = currentProcessResponse.getContext();
      ProcessRequest currentProcessRequest = currentProcessResponse.getRequest();
      getLogger().trace("{} - doFire() - processing request {}", getFullName(), currentProcessRequest);
      if (mustValidateIteration()) {
        try {
          getLogger().trace("{} - doFire() - validating iteration for request {}", getFullName(), currentProcessRequest);
          validateIteration(ctxt, currentProcessRequest);
          getAuditLogger().debug("ITERATION VALIDATED");
          getLogger().trace("{} - doFire() - validation done for request {}", getFullName(), currentProcessRequest);
        } catch (ValidationException e) {
          try {
            getErrorControlStrategy().handleIterationValidationException(this, e);
          } catch (IllegalActionException e1) {
            // interpret this is a FATAL error
            throw new ProcessingException(ErrorCode.ERROR_PROCESSING_FAILURE, "Error reporting iteration validation error", this, e);
          }
        }
      }
      try {
        getDirectorAdapter().notifyActorStartedTask(this, currentProcessRequest);
        notifyStartingFireProcessing();
        process(ctxt, currentProcessRequest, currentProcessResponse);
        if (ProcessingMode.SYNCHRONOUS.equals(getProcessingMode(ctxt, currentProcessRequest))) {
          processFinished(ctxt, currentProcessRequest, currentProcessResponse);
        }
      } finally {
        notifyFinishedFireProcessing();
      }
      getLogger().trace("{} - doFire() - obtained response {}", getFullName(), currentProcessResponse);
    }
    getLogger().trace("{} - doFire() - exit", getFullName());
  }

  @Override
  protected boolean doPostFire() throws ProcessingException {
    getLogger().trace("{} - doPostFire() - entry", getFullName());

    boolean result = super.doPostFire();
    if (!result) {
      // check if we don't have asynch work ongoing
      result = getDirectorAdapter().isActorBusy(this);
    }
    if (result) {
      iterationCount++;
      try {
        triggerNextIteration();
      } catch (IllegalActionException e) {
        throw new ProcessingException(ErrorCode.FLOW_EXECUTION_ERROR, "Error triggering a fire iteration for source actor " + getFullName(), this, e);
      }
    }
    getLogger().trace("{} - doPostFire() - exit : {}", getFullName(), result);
    return result;
  }

  /**
   * Overridable method that triggers a first iteration, from inside the actor initialization. Default implementation
   * calls <code>Director.fireAtCurrentTime(this)</code> when the actor is a source. (i.e. has no connected data input
   * ports)
   * 
   * @throws IllegalActionException
   */
  protected void triggerFirstIteration() throws IllegalActionException {
    if (isSource) {
      getDirector().fireAtCurrentTime(this);
    }
  }

  /**
   * Overridable method that triggers a next iteration, after each actor's previous iteration. Default implementation
   * calls <code>Director.fireAtCurrentTime(this)</code> when the actor is a source. (i.e. has no connected data input
   * ports)
   * 
   * @throws IllegalActionException
   */
  protected void triggerNextIteration() throws IllegalActionException {
    if (isSource) {
      getDirector().fireAtCurrentTime(this);
    }
  }

  /**
   * Check out all msgs pushed to this actor, and group them according to their context. If a ProcessRequest is found
   * with all required inputs filled in, add it to the pending queue.
   * 
   * @throws ProcessingException
   */
  protected void aggregatePushedMessages() throws ProcessingException {
    getLogger().trace("{} - checkPushedMessages() - entry", getFullName());
    int msgCtr = 0;
    try {
      if (!msgQLock.tryLock(10, TimeUnit.SECONDS)) {
        // if we did not get the lock, something is getting overcharged,
        // so refuse the task
        throw new ProcessingException(ErrorCode.RUNTIME_PERFORMANCE_INFO, "Msg Queue lock overcharged...", this, null);
      }
      ProcessRequest contextLessProcessRequest = new ProcessRequest();
      contextLessProcessRequest.setIterationCount(iterationCount);
      // Contrary to plain v5 Actors, we only want to handle one MessageInputContext per iteration.
      // So no while loop here!
      if (!pushedMessages.isEmpty()) {
        MessageInputContext msgInputCtxt = pushedMessages.poll();
        Iterator<ManagedMessage> msgIterator = msgInputCtxt.getMsgIterator();
        while (msgIterator.hasNext()) {
          ManagedMessage managedMessage = (ManagedMessage) msgIterator.next();
          String[] ctxtHdrs = ((MessageContainer) managedMessage).getHeader(ProcessRequest.HEADER_PROCESS_CONTEXT);
          if (ctxtHdrs != null && ctxtHdrs.length > 0) {
            for (String ctxtHdr : ctxtHdrs) {
              ProcessRequest processRequest = incompleteProcessRequests.get(ctxtHdr);
              if (processRequest == null) {
                Context context = contextRepository.getContext(ctxtHdr);
                if (context != null) {
                  processRequest = new ProcessRequest();
                  processRequest.setIterationCount(iterationCount);
                  incompleteProcessRequests.put(ctxtHdr, processRequest);
                } else {
                  processRequest = contextLessProcessRequest;
                }
              }
              processRequest.addInputMessage(msgInputCtxt.getPortIndex(), msgInputCtxt.getPortName(), managedMessage);
            }
          } else {
            contextLessProcessRequest.addInputMessage(msgInputCtxt.getPortIndex(), msgInputCtxt.getPortName(), managedMessage);
          }
          msgCtr++;
        }
      }
      // if the context-less process request has something to process, let's do it as well!
      if (contextLessProcessRequest.hasSomethingToProcess()) {
        pendingProcessRequests.offer(new ProcessResponse(getActorContext(), contextLessProcessRequest));
      }
      // now check for any completely-defined context-scoped process requests
      Collection<Entry<String, ProcessRequest>> transfers = new ArrayList<Entry<String, ProcessRequest>>();
      for (Entry<String, ProcessRequest> prEntry : incompleteProcessRequests.entrySet()) {
        if (prEntry.getValue().hasSomethingToProcess()) {
          transfers.add(prEntry);
        }
      }
      for (Entry<String, ProcessRequest> entry : transfers) {
        pendingProcessRequests.offer(new ProcessResponse(getActorContext(), entry.getValue()));
        incompleteProcessRequests.remove(entry.getKey());
      }
    } catch (InterruptedException e) {
      throw new ProcessingException(ErrorCode.RUNTIME_PERFORMANCE_INFO, "Msg Queue lock interrupted...", this, null);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error preparing process requests", this, e);
    } finally {
      try {
        msgQLock.unlock();
      } catch (Exception e) {
      }
      getLogger().trace("{} - checkPushedMessages() - exit - added {}", getFullName(), msgCtr);
    }
  }

  protected ActorContext getActorContext() {
    return new ActorContext();
  }

  /**
   * Does a check on the size of the <code>pushedMessages</code> queue, protected with a <code>msgLock</code>
   * 
   * @return true if this actor currently has msgs in its <code>pushedMessages</code> queue.
   * @throws ProcessingException
   *           if the access to the queue fails, e.g. when the lock is not available within a reasonable time
   */
  protected boolean hasPushedMessages() throws ProcessingException {
    getLogger().trace("{} - hasPushedMessages() - entry", getFullName());
    boolean result = false;
    try {
      if (!msgQLock.tryLock(1, TimeUnit.SECONDS)) {
        // if we did not get the lock, something is getting overcharged,
        // so refuse the task
        throw new ProcessingException(ErrorCode.RUNTIME_PERFORMANCE_INFO, "Msg Queue lock overcharged...", this, null);
      }
      result = !pushedMessages.isEmpty();
    } catch (InterruptedException e) {
      throw new ProcessingException(ErrorCode.RUNTIME_PERFORMANCE_INFO, "Msg Queue lock interrupted...", this, null);
    } finally {
      try {
        msgQLock.unlock();
      } catch (Exception e) {
      }
      getLogger().trace("{} - hasPushedMessages() - exit - {}", getFullName(), result);
    }
    return result;
  }

  /**
   * @return true when all input ports are exhausted
   */
  protected boolean areAllInputsFinished() {
    return msgProviders.isEmpty();
  }

  /**
   * Overridable method to indicate whether the given request will be processed synchronously or asynchronously. Default
   * implementation indicates synchronous processing for all requests.
   * <p>
   * Actors that have asynchronous processing, should combine returning <code>ProcessingMode.ASYNCHRONOUS</code> here,
   * with invoking <code>processFinished(ActorContext ctxt, ProcessRequest request, ProcessResponse response)</code>
   * when the work is done for a given request.
   * </p>
   * 
   * @param ctxt
   * @param request
   * @return whether the given request will be processed synchronously or asynchronously.
   */
  public ProcessingMode getProcessingMode(ActorContext ctxt, ProcessRequest request) {
    return ProcessingMode.SYNCHRONOUS;
  }

  /**
   * @param ctxt
   *          the context in which the request was processed
   * @param request
   *          the request that was processed
   * @param response
   *          contains the output messages that the actor should send, or a ProcessingException if some error was
   *          encountered during processing.
   */
  public void processFinished(ActorContext ctxt, ProcessRequest request, ProcessResponse response) {
    try {
      if (response.getException() == null) {
        // Mark the contexts as processed.
        // Not sure if this is still relevant for v5 actors,
        // as even PUSHed messages are assumed to be handled once, in the iteration when they are offered to process().
        Iterator<MessageInputContext> allInputContexts = request.getAllInputContexts();
        while (allInputContexts.hasNext()) {
          MessageInputContext msgInputCtxt = allInputContexts.next();
          msgInputCtxt.setProcessed(true);
        }

        // and now send out the results
        MessageOutputContext[] outputs = response.getOutputs();
        if (outputs != null) {
          for (MessageOutputContext output : outputs) {
            sendOutputMsg(output.getPort(), output.getMessage());
          }
        }
        outputs = response.getOutputsInSequence();
        if (outputs != null && outputs.length > 0) {
          Long seqID = MessageFactory.getInstance().createSequenceID();
          for (int i = 0; i < outputs.length; i++) {
            MessageOutputContext context = outputs[i];
            boolean isLastMsg = (i == (outputs.length - 1));
            try {
              ManagedMessage msgInSeq = MessageFactory.getInstance().createMessageCopyInSequence(context.getMessage(), seqID, new Long(i), isLastMsg);
              sendOutputMsg(context.getPort(), msgInSeq);
            } catch (MessageException e) {
              throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error creating output sequence msg for msg " + context.getMessage().getID(),
                  this, context.getMessage(), e);
            }
          }
        }
      } else {
        throw response.getException();
      }
    } catch (ProcessingException e) {
      try {
        getErrorControlStrategy().handleFireException(this, e);
      } catch (IllegalActionException e1) {
        getLogger().error("Error handling exception ", e);
      }
    } finally {
      getDirectorAdapter().notifyActorFinishedTask(this, response.getRequest());
    }
  }

  /**
   * <p>
   * Method that should be overridden for actors that need to be able to validate their state before processing a next
   * fire-iteration.
   * </p>
   * <p>
   * E.g. it can typically be used to validate dynamic parameter settings, and/or messages received on their input
   * ports.
   * </p>
   * 
   * @param ctxt
   * @param request
   *          contains all messages received on the actor's input ports for the current iteration.
   * @throws ValidationException
   */
  protected void validateIteration(ActorContext ctxt, ProcessRequest request) throws ValidationException {
  }

  /**
   * Overridable method to determine if an actor should do a validation of its state and incoming request for each
   * iteration. <br>
   * By default, checks on its Passerelle director what must be done. If no Passerelle director is used (but e.g. a
   * plain Ptolemy one), it returns false.
   * 
   * @see validateIteration()
   * @see doFire()
   * @return
   */
  protected boolean mustValidateIteration() {
    try {
      return getDirectorAdapter().mustValidateIteration();
    } catch (ClassCastException e) {
      return false;
    }
  }

  @Override
  public Object clone(Workspace workspace) throws CloneNotSupportedException {
    final Actor actor = (Actor) super.clone(workspace);
    actor.pushedMessages = new ConcurrentLinkedQueue<MessageInputContext>();
    actor.msgProviders = new HashSet<Object>();
    actor.incompleteProcessRequests = new ConcurrentHashMap<String, ProcessRequest>();
    actor.pendingProcessRequests = new LinkedBlockingQueue<ProcessResponse>();
    actor.finishedProcessResponses = new LinkedBlockingQueue<ProcessResponse>();
    actor.msgQLock = new ReentrantLock();
    actor.msgQNonEmpty = actor.msgQLock.newCondition();
    return actor;
  }

  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * @param context
   * @return
   * @throws MessageException
   */
  protected ManagedMessage createMessageForContext(Context context) throws MessageException {
    SettableMessage message = (SettableMessage) createMessage(context, ManagedMessage.objectContentType);
    if (context.getId() != null) {
      message.setHeader(ProcessRequest.HEADER_PROCESS_CONTEXT, context.getContextRepositoryID());
    }
    return message;
  }

  @Override
  public ManagedMessage createMessageFromCauses(ManagedMessage... causes) {
    SettableMessage message = (SettableMessage) super.createMessageFromCauses(causes);
    for (ManagedMessage causeMsg : causes) {
      // Normally we would only expect a msg to be related to one context,
      // but one never knows what may happen with complex/concurrent workflows
      // where work could maybe be optimized via sharing/grouping...
      String[] ctxtIDHdrs = ((SettableMessage) causeMsg).getHeader(ProcessRequest.HEADER_PROCESS_CONTEXT);
      if (ctxtIDHdrs != null) {
        for (String ctxtIDHdr : ctxtIDHdrs) {
          message.addHeader(ProcessRequest.HEADER_PROCESS_CONTEXT, ctxtIDHdr);
        }
      }
    }
    return message;
  }

  protected Context getRequiredContextForMessage(ManagedMessage message) throws ProcessingException {
    if (message == null) {
      throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "No message received", this, null);
    }
    String[] ctxtHdrs = ((MessageContainer) message).getHeader(ProcessRequest.HEADER_PROCESS_CONTEXT);
    if (ctxtHdrs == null || ctxtHdrs.length == 0) {
      try {
        if (message.getBodyContent() instanceof Context) {
          return (Context) message.getBodyContent();
        } else {
          throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "No context present in msg", this, message, null);
        }
      } catch (MessageException e) {
        throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error reading msg", this, message, null);
      }
    } else {
      Context context = contextRepository.getContext(ctxtHdrs[0]);
      if (context != null) {
        return context;
      } else {
        throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "No context present in msg", this, message, null);
      }
    }
  }

  @Override
  protected String getAuditTrailMessage(ManagedMessage message, Port port) {
    try {
      Context processContext = getRequiredContextForMessage(message);
      return port.getFullName() + " - msg for request " + processContext.getRequest().getId();
    } catch (ProcessingException e) {
      return super.getAuditTrailMessage(message, port);
    }
  }

}
