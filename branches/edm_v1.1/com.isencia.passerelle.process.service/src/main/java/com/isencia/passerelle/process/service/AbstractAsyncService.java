/**
 * 
 */
package com.isencia.passerelle.process.service;

import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.common.util.PreferenceUtils;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.scheduler.DefaultResourcePoolSelectionStrategy;
import com.isencia.passerelle.process.scheduler.ResourceSelectionStrategy;
import com.isencia.passerelle.process.scheduler.ResourceToken;
import com.isencia.passerelle.process.scheduler.TaskHandler;
import com.isencia.passerelle.process.scheduler.TaskRefusedException;
import com.isencia.passerelle.process.scheduler.TaskScheduler;
import com.isencia.passerelle.process.service.impl.DefaultContextHandler;
import com.isencia.passerelle.process.service.proxy.ContextManagerProxy;
import com.isencia.passerelle.process.service.proxy.SchedulerRegistryProxy;

/**
 * @author puidir
 *
 */
public abstract class AbstractAsyncService extends AbstractService {
	
  private static final String RESOURCE_POOL_NAME_OVERRIDE = "RESOURCE_NAME";
  
	private String defaultResourcePoolName;
	private ResourceSelectionStrategy resourcePoolSelectionStrategy = new DefaultResourcePoolSelectionStrategy();
	
	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractAsyncService.class);

	protected AbstractAsyncService(String defaultResourcePoolName) {
		this.defaultResourcePoolName = defaultResourcePoolName;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.edm.request.service.AbstractService#doConfigureService()
	 */
	@Override
	protected void doConfigureService() throws ServiceException {

	  // Preferences changed, may be an override of the resource name
	  getResourcePoolNameFromPreferences();
	}
	
	private void getResourcePoolNameFromPreferences() {
    // The default resource pool name can be overwritten through the preferences
    Preferences servicePrefs = PreferenceUtils.getBackendsConfigNode().node(getConfigurationNodeName());
    this.defaultResourcePoolName = servicePrefs.get(RESOURCE_POOL_NAME_OVERRIDE, defaultResourcePoolName);
	}
	
	protected Logger getLogger() {
		return LOGGER;
	}
	
	public void setResourcePoolSelectionStrategy(ResourceSelectionStrategy resourcePoolSelectionStrategy) {
		// We always want a strategy
		if (resourcePoolSelectionStrategy == null) {
			return;
		}
		
		this.resourcePoolSelectionStrategy = resourcePoolSelectionStrategy;
	}

	@Override
	protected final void doHandleTask(Context flowContext, Context taskContext) throws TaskRefusedException, ServiceException {

		TaskHandler handler = createContextHandler(taskContext);

		String resourcePoolName = resourcePoolSelectionStrategy.getResourcePoolName(
				(Task)taskContext.getRequest(), defaultResourcePoolName);
		
		TaskScheduler scheduler = SchedulerRegistryProxy.getScheduler(resourcePoolName);

		if (scheduler == null) {
			getLogger().warn(ErrorCode.SCHEDULER_WARNING + " - No TaskScheduler found for resourcePoolName " + resourcePoolName +
					". Defaulting to default scheduler");
			scheduler = SchedulerRegistryProxy.getDefaultScheduler();
		}
		
		scheduler.accept(taskContext, handler);
	}

	/**
	 * Overridable factory method for TaskHandlers for this service.
	 * By default, a task-independent DefaultContextHandler is created for
	 * each new task.
	 * This is ok, as the task is passed as argument in the TaskHandler.handletask() invocation.
	 * Service subclasses could create custom handlers for specific tasks,
	 * or reuse a common handler instance for all tasks, or apply any other
	 * desirable handler allocation strategy as needed.
	 * 
	 * @param taskContext : context of the actual task for which this handler is constructed.
	 * @return a new TaskHandler, to handle the asynchronous execution of the given Task
	 */
	protected TaskHandler createContextHandler(Context context) {
		return new DefaultContextHandler(this);
	}
	
	/**
	 * Normally only used by asynchronous request handlers.
	 * 
	 * Other service clients should use
	 * <code>handleTaskNow(LifeCycleEntity, ResourceToken...)</code> to force a
	 * synchronous entity handling by a service.
	 * 
	 * @param managedTask
	 */
	public void handleNow(ManagedContext managedContext) {
		try {
			handleNow(managedContext.getContext(), managedContext.getResourceTokens());
		} catch (Throwable e) { // NOSONAR
			ContextManagerProxy.notifyError(managedContext.getContext(), e);
		}
	}

	/**
	 * To be implemented by concrete asynchronous services.
	 * 
	 * @param entity the Task for which this service is executed
	 * @param resourceTokens an optional array of resourceTokens used by the handler
	 * @throws ServiceException 
	 */
	public abstract void handleNow(Context context, ResourceToken ... resourceTokens) throws ServiceException;
	
}
