/**
 * 
 */
package com.isencia.passerelle.process.service;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.common.util.PreferenceUtils;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.impl.ContextImpl;
import com.isencia.passerelle.process.model.service.ServiceRegistry;
import com.isencia.passerelle.process.scheduler.TaskRefusedException;
import com.isencia.passerelle.process.service.proxy.ContextManagerProxy;


/**
 * @author puidir
 * 
 */
public abstract class AbstractService implements Service {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);

  protected IPreferenceChangeListener configChangeListener;

  protected IEclipsePreferences configPrefNode;

  // Left out for now: WorkerSpecification (for alarming and monitoring)

  public void setup() throws ServiceException {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("setUp() - entry");
    }

    configPrefNode = (IEclipsePreferences) PreferenceUtils.getBackendsConfigNode().node(getConfigurationNodeName());
    configureService();

    // We only want to configure once on startup + whenever the preferences
    // for this service changed.
    if (configChangeListener == null) {
      configChangeListener = new ChangeListener();
      configPrefNode.addPreferenceChangeListener(configChangeListener);
    }

    if (getLogger().isDebugEnabled()) {
      getLogger().debug("setUp() - exit");
    }
  }

  // TODO: this is never called (would be considered a leak if the services
  // came and went, but they simply stay up all the time)
  public void wrapup() {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("wrapUp() - entry");
    }

    // Stop listening to preference changes
    if (configChangeListener != null && configPrefNode != null) {
      configPrefNode.removePreferenceChangeListener(configChangeListener);
      configChangeListener = null;
      configPrefNode = null;
    }

    if (getLogger().isDebugEnabled()) {
      getLogger().debug("wrapUp() - exit");
    }
  }

  private class ChangeListener implements IEclipsePreferences.IPreferenceChangeListener {

    public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {

      String key = event.getKey();
      if (key == null) {
        return;
      }

      String newValue = (String) event.getNewValue();
      if (newValue == null) {
        return;
      }

      if (getLogger().isInfoEnabled()) {
        getLogger().info("Configuration changed - " + key + ": " + newValue);
      }

      try {
        configureService();
      } catch (ServiceException e) {
        // should not happen, but one never knows
        // we assume that when this does happen, the original service
        // cfg remains operational!
        getLogger().error(
            ErrorCode.INVALID_CONFIGURATION + " - Error reconfiguring service " + getName()
                + " - Maintaining old config!", e);
      }
    }
  }

  final protected synchronized void configureService() throws ServiceException {
    // TODO: dare uses a WorkerSpecification here ... not sure what to do
    // with it

    try {
      doConfigureService();
    } catch (ServiceException ex) {
      // TODO: dare used to restore the old workerspecification here
      throw ex;
    }
  }

  protected abstract void doConfigureService() throws ServiceException;

  /**
   * Allow implementations to plug in their own logger
   */
  protected Logger getLogger() {
    return LOGGER;
  }

  public Context handleTask(Class<? extends Task> taskClass, Context flowContext, Map<String, String> taskAttributes,
      Map<String, Serializable> taskContextEntries, String initiator, String type) throws ServiceException {
    Object source = taskContextEntries.get("source");
    taskContextEntries.remove("source");
    getLogger().trace("handleTask() - entry - context " + flowContext.getId());

    // Set up configuration if it was not yet done
    if (configPrefNode == null) {
      setup();
    }

    Context taskContext = null;

    try {
      Context context =  ServiceRegistry.getInstance().getEntityManager().getContext(flowContext);

      taskContext = (Context) ContextManagerProxy.createTask(taskClass, context, taskAttributes,
          taskContextEntries, initiator, type);

      // skipped setting worker for monitoring / alarming here

      if (getLogger().isDebugEnabled()) {
    	  getLogger().debug(
    	          "Created task context " + taskContext.getId() + ":" + getName() + " for flowContext " + flowContext.getId());
        }
      ((ContextImpl)flowContext).addTask((Task)taskContext.getRequest());

      doHandleTask(flowContext, taskContext);

      return taskContext;

    } catch (ServiceException ex) {
      notifyException(source,flowContext, taskContext, ex);
      throw ex;
    } catch (TaskRefusedException ex) {
      notifyException(source,flowContext, taskContext, ex);
      throw new ServiceException(ErrorCode.REQUEST_LIFECYCLE_REQUEST_REFUSED, "FlowContext " + flowContext.getId()
          + " refused - typically indicates a system overload", ex);
    } catch (Exception ex) {
      notifyException(source,flowContext, taskContext, ex);
      throw new ServiceException(ErrorCode.SYSTEM_ERROR, "FlowContext " + flowContext.getId() + " processing failed",
          ex);
    } finally {
      getLogger().trace("handleTask() - exit - " + taskContext);
    }
  }

  /**
   * If there is a task context, the exception is notified on the task context. Otherwise, it is notified on the flow
   * context, which is assumed to be always available
   * 
   * @param flowContext
   * @param taskContext
   * @param t
   */
  private void notifyException(Object source,Context flowContext, Context taskContext, Throwable t) {
    if (taskContext != null) {
      ContextManagerProxy.notifyError(taskContext, t);
    } else {
      ContextManagerProxy.notifyError(flowContext, t);
    }
  }

  protected abstract void doHandleTask(Context flowContext, Context taskContext) throws TaskRefusedException,
      ServiceException;
}
