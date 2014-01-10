package com.isencia.passerelle.process.actor;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.FlowUtils;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.ext.impl.DefaultActorErrorControlStrategy;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.common.util.ContextUtils;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.service.ServiceRegistry;
import com.isencia.passerelle.util.ExecutionTracerService;

@SuppressWarnings("serial")
public abstract class TaskBasedActor extends Actor {
  private final static Logger LOGGER = LoggerFactory.getLogger(TaskBasedActor.class);

  private static final String ERROR_STRATEGY = "Error Strategy";
  private static final String ERROR_VIA_ERROR_PORT = "Error via error port";
  private static final String CONTINUE_VIA_ERROR_PORT = "Continue via error port";
  private static final String CONTINUE_VIA_OUTPUT_PORT = "Continue via output port";

  public Port output; // NOSONAR
  public Port input; // NOSONAR
  // by default the actor name is set as task/result type
  public StringParameter taskTypeParam; // NOSONAR
  public StringParameter resultTypeParam;
  public StringParameter errorStrategyParameter;// NOSONAR

  private Set<ContextProcessingCallback> pendingListeners = Collections.synchronizedSet(new HashSet<ContextProcessingCallback>());

  public TaskBasedActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);

    taskTypeParam = new StringParameter(this, AttributeNames.TASK_TYPE);
    taskTypeParam.setExpression(name);
    resultTypeParam = new StringParameter(this, AttributeNames.RESULT_TYPE);
    resultTypeParam.setExpression(name);
    
    // TODO: the default should come from the DirectorAdapter
    errorStrategyParameter = new StringParameter(this, ERROR_STRATEGY);
    errorStrategyParameter.addChoice(CONTINUE_VIA_OUTPUT_PORT);
    errorStrategyParameter.addChoice(CONTINUE_VIA_ERROR_PORT);
    errorStrategyParameter.addChoice(ERROR_VIA_ERROR_PORT);
    errorStrategyParameter.setExpression(ERROR_VIA_ERROR_PORT);
    
  }

  @Override
  public final ProcessingMode getProcessingMode(ActorContext ctxt, ProcessRequest request) {
    return ProcessingMode.ASYNCHRONOUS;
  }

  @Override
  public Object clone(Workspace workspace) throws CloneNotSupportedException {
    final TaskBasedActor actor = (TaskBasedActor) super.clone(workspace);
    actor.pendingListeners = Collections.synchronizedSet(new HashSet<ContextProcessingCallback>());
    return actor;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    pendingListeners.clear();

    if (errorStrategyParameter != null) {
      if (CONTINUE_VIA_OUTPUT_PORT.equals(errorStrategyParameter.getExpression())) {
        // Continue with the message on the output port
        setErrorControlStrategy(new ContinueOnOutputControlStrategy());
      } else if (CONTINUE_VIA_ERROR_PORT.equals(errorStrategyParameter.getExpression())) {
        // Continue with the message on the error port
        setErrorControlStrategy(new ContinueOnErrorControlStrategy());
      } else if (ERROR_VIA_ERROR_PORT.equals(errorStrategyParameter.getExpression())) {
        setErrorControlStrategy(new DefaultActorErrorControlStrategy());
      }
    } else {
      // Send error on error port
      setErrorControlStrategy(new DefaultActorErrorControlStrategy());
    }
  }

  /**
   * Allow ErrorControlStrategy to write to the output port
   */
  @Override
  protected void sendOutputMsg(Port port, ManagedMessage message) throws ProcessingException {
    super.sendOutputMsg(port, message);
  }

  @Override
  public final void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage message = request.getMessage(input);
    if (message != null) {
      Context processContext = null;
      Context taskContext = null;
      try {
        if (message.getBodyContent() instanceof Context) {
          processContext = (Context) message.getBodyContent();
        } else {
          throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "No context present in msg", this, message, null);
        }
        if (mustProcess(message)) {
          String requestId = Long.toString(processContext.getRequest().getId());
          String referenceId = Long.toString(processContext.getRequest().getCase().getId());

          Map<String, String> taskAttributes = new HashMap<String, String>();
          taskAttributes.put(AttributeNames.CREATOR_ATTRIBUTE, getFullName());
          taskAttributes.put(AttributeNames.REF_ID, referenceId);
          taskAttributes.put(AttributeNames.REQUEST_ID, requestId);
          // allow subclasses to add their own attributes, mostly based on data in the received processContext
          addActorSpecificTaskAttributes(processContext, taskAttributes);

          // allow subclasses to add task context entries
          Map<String, Serializable> taskContextEntries = new HashMap<String, Serializable>();
          addActorSpecificTaskContextEntries(processContext, taskContextEntries);

          taskContext = createTask(processContext, taskAttributes, taskContextEntries);
          // Remark that we don't return a changed taskContext or so
          // any changes (e.g. new events,results,status,... should be done in the passed taskContext instance, if needed
          // for this actor it's not relevant as it doesn't work with the context in the remainder of this method.
          // any following processing is done in the TaskContextListener.
          // But we need to check if this works OK with the strange JPA/eclipselink behaviour in some configurations
          process(taskContext);
          postProcess(message, taskContext, response);
        } else {
          response.addOutputMessage(output, message);
          processFinished(ctxt, request, response);
        }
      } catch (PasserelleException ex) {
        ExecutionTracerService.trace(this, ex.getMessage());
        // copy the ex info but ensure that the message is added
        // as an exception coming from the depths of task processing may not know about the received message
        ServiceRegistry.getInstance().getContextManager().notifyError(taskContext, ex);
        response.setException(new ProcessingException(ex.getErrorCode(), ex.getSimpleMessage(), this, message, ex.getCause()));
        processFinished(ctxt, request, response);
      } catch (Throwable t) {
        ExecutionTracerService.trace(this, t.getMessage());
        response.setException(new ProcessingException(ErrorCode.TASK_ERROR, "Error processing task", this, message, t));
        ServiceRegistry.getInstance().getContextManager().notifyError(taskContext, t);
        processFinished(ctxt, request, response);
      }
    } else {
      // should not happen, but one never knows, e.g. when a requestFinish msg arrived or so...
      getLogger().warn("Actor " + this.getFullName() + " received empty message in process()");
      processFinished(ctxt, request, response);
    }
  }

  /**
   * Override this in specific cases where default postProcessing is not OK. Default is to register a TaskContextListener that will send the processing Context
   * onwards when the Task is done.
   * 
   * @param message
   * @param taskContext
   * @param response
   * @throws Exception
   */
  protected void postProcess(ManagedMessage message, Context taskContext, ProcessResponse response) throws Exception {
    TaskContextListener listener = new TaskContextListener(message, response);
    pendingListeners.add(listener);
    ServiceRegistry.getInstance().getContextManager().subscribe(taskContext, listener);
  }

  /**
   * Should perform the actual processing of the task. For most simple/fast cases, this can be done in a synchronous fashion. For complex/long-running
   * processing, the usage of a ServiceBasedActor is advisable.
   * 
   * @param taskContext
   *          the context of the new task that must be processed
   * @return the updated context of the started/executed task
   * @throws ProcessingException
   */
  protected abstract void process(Context taskContext) throws ProcessingException;

  /**
   * Override this method to define the logic for potentially skipping the processing of a received message.
   * <p>
   * Sample cases could be : check for mock mode, filter on certain request elements etc
   * </p>
   * 
   * @param message
   * @return
   */
  protected boolean mustProcess(ManagedMessage message) {
    return true;
  }

  /**
   * Returns a new Task (or at least its Context) with the configured resultType as taskType.
   * 
   * @param parentContext
   * @param taskAttributes
   * @param taskContextEntries
   * @return the context for a new task created within the parent process context
   * @throws Exception
   */
  protected Context createTask(Context parentContext, Map<String, String> taskAttributes, Map<String, Serializable> taskContextEntries) throws Exception {
    String taskType = taskTypeParam.stringValue();
    String initiator = getTaskInitiator();
    return ServiceRegistry.getInstance().getContextManager().createTask(getTaskClass(parentContext), parentContext, taskAttributes, taskContextEntries, initiator, taskType);
  }

  /**
   * Defines the initiator for a new Task started by this actor.
   * <p>
   * Default implementation uses an URI-syntax as follows :
   * <br/><code>actor:/<flow name>.[<subflow name>...].<actor name></code>
   * <br/>i.e. the actor's full name (without leading .) is used as URI path
   * </p>
   * @return
   * @throws Exception
   */
  protected String getTaskInitiator() throws Exception {
    return new URI("actor",null,"/"+FlowUtils.getOriginalFullName(this).substring(1),null,null).toString();
  }

  /**
   * @param parentContext
   * @return the java class of the Task implementation entity. Default is com.isencia.passerelle.process.model.impl.TaskImpl.
   */
  protected Class<? extends Task> getTaskClass(Context parentContext) {
    return ServiceRegistry.getInstance().getContextManager().getDefaultTaskClass();
  }

  @Override
  protected boolean doPostFire() throws ProcessingException {
    boolean result = super.doPostFire();
    if (!result) {
      synchronized (pendingListeners) {
        while (!pendingListeners.isEmpty()) {
          try {
            pendingListeners.wait(1000);
          } catch (InterruptedException e) {
            break;
          }
        }
      }
    }
    return result;
  }

  /**
   * Method to configure the attributes for the task that the actor wants to get executed. The actor implementation should add entries in the taskAttributes map
   * as needed for its type of task. Attribute data is typically obtained either from the received processContext and/or from the actor's parameters.
   * 
   * @param processContext
   * @param taskAttributes
   * @throws ProcessingException
   */
  protected void addActorSpecificTaskAttributes(final Context processContext, Map<String, String> taskAttributes) throws ProcessingException {
    try {
      storeContextItemValueInMap(taskAttributes, processContext, AttributeNames.RESULT_TYPE, resultTypeParam);
    } catch (IllegalActionException e) {
      throw new ProcessingException(ErrorCode.TASK_ERROR, "Unable to obtain task attributes", this, e);
    }
  }

  /**
   * Method to allow actor implementations to pass specific context entries into the task that will be created and executed. Similar to
   * <code>addActorSpecificTaskAttributes</code> but :
   * <ul>
   * <li>context entries can contain any serializable object i.o. just strings</li>
   * <li>context entries are typically not persisted, but only valid in memory during the process execution!</li>
   * </ul>
   * 
   * @param processContext
   * @param taskContextEntries
   */
  protected void addActorSpecificTaskContextEntries(final Context processContext, Map<String, Serializable> taskContextEntries) {
    // Default does nothing
  }

  /**
   * @param context
   * @param itemName
   * @param defaultValue
   * @return the value of the context item with the given itemName, or the defaultValue if no such item was found.
   */
  protected final String getContextItemValue(Context context, String itemName, String defaultValue) {
    if (context == null || itemName == null) {
      return null;
    } else {
      String itemValue = context.lookupValue(itemName);
      return itemValue != null ? itemValue : defaultValue;
    }
  }

  /**
   * Stores the value of the context item with the given itemName, or the defaultValue, in the given map, iff a non-null value is found.
   * 
   * @param map
   * @param context
   * @param itemName
   * @param defaultValue
   */
  protected final void storeContextItemValueInMap(Map<String, String> map, Context context, String itemName, String defaultValue) {
    String itemValue = getContextItemValue(context, itemName, defaultValue);
    if (itemValue != null) {
      map.put(itemName, itemValue);
    }
  }

  /**
   * Stores the value of the context item with the given lookupItemName, or the defaultValue, in the given map, with as name attrName, iff a non-null value is
   * found.
   * 
   * @param map
   * @param context
   * @param attrName
   *          the name that will be given to the resulting attribute, stored in the map
   * @param lookupItemName
   *          the name used to lookup the data item in the given context.
   * @param defaultValue
   */
  protected final void storeContextItemValueInMap(Map<String, String> map, Context context, String attrName, String lookupItemName, String defaultValue) {
    String itemValue = getContextItemValue(context, lookupItemName, defaultValue);
    if (itemValue != null) {
      map.put(attrName, itemValue);
    }
  }

  /**
   * Retrieves the value of a context item with the given itemName. If this is not found, it uses the value of the actorParameter as default value.
   * <p>
   * The actorParameter's value may contain a placeHolder (syntax #[some_name]), in which case another context item is looked up, this time with the
   * <i>some_name</i> from the placeHolder.
   * </p>
   * 
   * @param map
   * @param context
   * @param itemName
   * @param actorParameter
   * @throws IllegalActionException
   */
  protected final void storeContextItemValueInMap(Map<String, String> map, Context context, String itemName, Variable actorParameter)
      throws IllegalActionException {
    String defaultValue = null;
    if (actorParameter instanceof StringParameter) {
      defaultValue = ((StringParameter) actorParameter).stringValue();
    } else if (actorParameter != null && actorParameter.getToken() != null) {
      defaultValue = actorParameter.getToken().toString();
    }
    defaultValue = ContextUtils.lookupValueForPlaceHolder(context, defaultValue);
    String itemValue = getContextItemValue(context, itemName, defaultValue);
    if (itemValue != null) {
      map.put(itemName, itemValue);
    }
  }

  /**
   * Look up the value of a context item with one of the given itemNames (first one that is found).
   * 
   * This can be used to look for parameters where the name of the key varies.
   * 
   * @param context
   *          The context of the request or task to look in
   * @param itemNames
   *          The item names to search for
   * @return First value found or null
   */
  private String lookupValueForFirstKeyFound(final Context context, String... itemNames) {
    String value = null;
    for (String itemName : itemNames) {
      value = context.lookupValue(itemName);
      if (value != null) {
        break;
      }
    }
    return value;
  }

  /**
   * Retrieves the value of a context item with one of the given itemNames (first one that is found).
   * 
   * This can be used to look for parameters where the name of the key varies. E.g. a LineNumber is sometimes known as
   * an NA, a CLE or a DN. You can look up this parameter by searching with a number of keys with:
   * storeContextItemValueInMap(flowCtx, taskAttrs, "my_attr_key", null, new String[]{"LINENUMBER", "NA",
   * "CLE", "DN"});
   * 
   * @param context Context of the request to search in
   * @param map Map to store the value in
   * @param attrName
   *          the name that will be given to the resulting attribute, stored in the map
   * @param defaultValue
   *          Default value to use when none of the keys yield a value
   * @param itemNames
   *          Item names to search for
   */
  protected final void storeContextItemValueInMap(Map<String, String> map, Context context, String attrName, String defaultValue, String... itemNames) {

    if (itemNames != null) {
      String value = lookupValueForFirstKeyFound(context, itemNames);

      if (value != null) {
        map.put(attrName, value);
      } else if (defaultValue != null) {
        map.put(attrName, defaultValue);
      }
    }
  }

  @Override
  protected final String getAuditTrailMessage(ManagedMessage message, Port port) {
    try {
      if (message.getBodyContent() instanceof Context) {
        Context processContext = (Context) message.getBodyContent();
        return port.getFullName() + " - msg for request " + processContext.getRequest().getId();
      } else {
        return super.getAuditTrailMessage(message, port);
      }
    } catch (Exception e) {
      // TODO do something in case of exception
      return null;
    }
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  private final class TaskContextListener implements ContextProcessingCallback {

    private ManagedMessage message;
    private ProcessResponse processResponse;
    private boolean consumed;

    public TaskContextListener(ManagedMessage message, ProcessResponse processResponse) {
      this.message = message;
      this.processResponse = processResponse;
    }

    private void removeMeAsListener() {
      synchronized (pendingListeners) {
        pendingListeners.remove(this);
        pendingListeners.notifyAll();
      }
    }

    public synchronized void contextStarted(ContextEvent event) {
      // nothing for the moment
    }

    public synchronized void contextInterrupted(ContextEvent event) {
      removeMeAsListener();
    }

    public void contextWasCancelled(ContextEvent event) {
      removeMeAsListener();
    }

    public synchronized void contextError(ContextEvent event, Throwable error) {
      if (!isConsumed()) {
        Task task = (Task) event.getContext().getRequest();
        Request parentrequest = task.getParentContext().getRequest();
        final String errorMsg = "Error executing task " + task.getType() + " with task ID " + task.getId() + " for request " + parentrequest.getId();
        ProcessingException exception = new ProcessingException(ErrorCode.TASK_ERROR, errorMsg, TaskBasedActor.this, message, error);
        try {
          refreshTaskInContext(task, message);
          getErrorControlStrategy().handleFireException(TaskBasedActor.this, exception);
          setConsumed(true);
        } catch (Exception e) {
          // this line serves to get the constructed PasserelleException in the log file
          getLogger().error("Failed to send error msg, so dumping its stacktrace here ", exception);
          // and this one to also get the IllegalActionException in there
          getLogger().error("Failed to send error msg because of ", e);
        } finally {
          removeMeAsListener();
        }
      }
    }

    public synchronized void contextFinished(ContextEvent event) {
      if (!isConsumed()) {
        Task task = (Task) event.getContext().getRequest();
        Request parentrequest = task.getParentContext().getRequest();
        try {
          refreshTaskInContext(task, message);
          processResponse.addOutputMessage(output, message);
          setConsumed(true);
        } catch (Exception e) {
          getLogger().error("Failed to send result msg for task " + task.getId() + " for request " + parentrequest.getId(), e);
          contextError(event, e);
        } finally {
          removeMeAsListener();
        }
      }
    }

    @Override
    public void contextPendingCompletion(ContextEvent event) {
      contextFinished(event);
    }

    public void contextTimeOut(ContextEvent event) {
      if (!isConsumed()) {
        Task task = (Task) event.getContext().getRequest();
        Request parentrequest = task.getParentContext().getRequest();
        final String errorMsg = "Timeout invoking task " + task.getType() + " with task ID " + task.getId() + " for request " + parentrequest.getId();
        ProcessingException exception = new ProcessingException(ErrorCode.TASK_TIMEOUT, errorMsg, TaskBasedActor.this, null);
        try {
          refreshTaskInContext(task, message);

          ExecutionTracerService.trace(TaskBasedActor.this, exception);
          getErrorControlStrategy().handleFireException(TaskBasedActor.this, exception);
          setConsumed(true);
        } catch (Exception e) {
          ExecutionTracerService.trace(TaskBasedActor.this, exception);
          // this line serves to get the constructed PasserelleException in the log file
          getLogger().error("Failed to send timeout error msg, so dumping its stacktrace here ", exception);
          // and this one to also get the IllegalActionException in there
          getLogger().error("Failed to send timeout error msg because of ", e);
        } finally {
          removeMeAsListener();
        }
      }
    }

    public boolean isConsumed() {
      return consumed;
    }

    public void setConsumed(boolean consumed) {
      this.consumed = consumed;
      if (consumed) {
        processFinished(processResponse.getContext(), processResponse.getRequest(), processResponse);
      }
    }

    private void refreshTaskInContext(Task task, ManagedMessage message) throws MessageException {
      Context processContext = (Context) message.getBodyContent();
      // Retrieve the task from db, bypassing the cache
      task = ServiceRegistry.getInstance().getEntityManager().getTask(task.getId(), true);
      // Re-attach on the process context
      processContext.reattachTask(task);
    }
  }
}
