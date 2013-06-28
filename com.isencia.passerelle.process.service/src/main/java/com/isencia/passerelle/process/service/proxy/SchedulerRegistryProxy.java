/**
 * 
 */
package com.isencia.passerelle.process.service.proxy;

import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.process.scheduler.TaskScheduler;
import com.isencia.passerelle.process.scheduler.TaskSchedulerRegistry;

/**
 * @author puidir
 *
 */
public class SchedulerRegistryProxy extends ServiceTracker {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerRegistryProxy.class);
	
	private static TaskSchedulerRegistry delegate;
	
	/**
	 * @param context
	 * @param filter
	 * @param customizer
	 */
	public SchedulerRegistryProxy(BundleContext context) {
		super(context, TaskSchedulerRegistry.class.getName(), null);
	}

	public static TaskScheduler getScheduler(String resourcePoolName) {
		return delegate.getScheduler(resourcePoolName);
	}
	
	public static TaskScheduler getDefaultScheduler() {
		return delegate.getDefaultScheduler();
	}

	public static Set<String> getRegisteredSchedulerNames() {
		return delegate.getRegisteredSchedulerNames();
	}

	@Override
	public Object addingService(ServiceReference reference) {

		LOGGER.trace("addingService() - entry - LifeCycleEntitySchedulerRegistry from bundle " + reference.getBundle().getSymbolicName());
		
		delegate = (TaskSchedulerRegistry)super.addingService(reference);
		
		LOGGER.trace("addingService() - exit");
		
		return delegate;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {

		LOGGER.trace("removedService() - entry - LifeCycleEntitySchedulerRegistry from bundle " + reference.getBundle().getSymbolicName());
		
		super.removedService(reference, service);
		delegate = null;
		
		LOGGER.trace("removedService() - exit");
		
	}
		
}
