package com.isencia.passerelle.process.service.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static Activator instance;
	
	static Activator getInstance() {
		return instance;
	}

	public void start(BundleContext bundleContext) throws Exception {
		instance = this;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		instance = null;
	}

}
