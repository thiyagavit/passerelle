package com.isencia.passerelle.process.actor.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator {

	// The shared instance
	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}

	public void start(BundleContext context) {
		plugin = this;

	}

	public void stop(BundleContext context) {
		plugin = null;
	}

	public static void initOutsideOSGi() {
		new Activator().start(null);
	}

}
