package com.isencia.passerelle.process.service.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.isencia.passerelle.process.service.proxy.ContextManagerProxy;
import com.isencia.passerelle.process.service.proxy.SchedulerRegistryProxy;

public class Activator implements BundleActivator {

	private static Activator instance;
	
	private ContextManagerProxy lifeCycleEntityManagerTracker;
	private SchedulerRegistryProxy schedulerRegistryProxyTracker;

	static Activator getInstance() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		lifeCycleEntityManagerTracker = new ContextManagerProxy(bundleContext);
		lifeCycleEntityManagerTracker.open();
		
		schedulerRegistryProxyTracker = new SchedulerRegistryProxy(bundleContext);
		schedulerRegistryProxyTracker.open();
		
		instance = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		instance = null;
		
		lifeCycleEntityManagerTracker.close();
		schedulerRegistryProxyTracker.close();
	}

}
