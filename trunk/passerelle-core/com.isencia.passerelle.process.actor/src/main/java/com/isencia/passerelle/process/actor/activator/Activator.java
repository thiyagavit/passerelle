package com.isencia.passerelle.process.actor.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.isencia.edm.service.result.reader.IResultReaderService;
import com.isencia.passerelle.process.actor.util.ServicesRegistry;
import com.isencia.passerelle.project.repository.api.RepositoryService;

public class Activator implements BundleActivator, ServiceTrackerCustomizer {

	// The shared instance
	private static Activator plugin;
	private ServiceTracker repoSvcTracker;
	private ServiceTracker entityFactorySvcTracker;
	private ServiceTracker taskManagerSvcTracker;
	private ServiceTracker resultReaderServiceTracker;

	private BundleContext bundleContext;

	public static Activator getDefault() {
		return plugin;
	}

	public void start(BundleContext context) {
		bundleContext = context;
		plugin = this;
		repoSvcTracker = new ServiceTracker(context, RepositoryService.class
				.getName(), this);
		repoSvcTracker.open();
		
		resultReaderServiceTracker = new ServiceTracker(context, IResultReaderService.class.getName(), this);
		resultReaderServiceTracker.open();
	}

	public void stop(BundleContext context) {
		plugin = null;
		repoSvcTracker.close();

		entityFactorySvcTracker.close();
		taskManagerSvcTracker.close();
		
		resultReaderServiceTracker.close();
		
		bundleContext = null;
	}

	public Object addingService(ServiceReference svcRef) {
		Object svc = bundleContext.getService(svcRef);
		if (svc instanceof RepositoryService) {
			ServicesRegistry.getInstance().setRepositoryService(
					(RepositoryService) svc);
		} else if (svc instanceof IResultReaderService) {
			ServicesRegistry.getInstance().setResultReaderService((IResultReaderService)svc);
		}
		return svc;
	}

	public void modifiedService(ServiceReference svcRef, Object svc) {
	}

	public void removedService(ServiceReference svcRef, Object svc) {
		if (svc instanceof RepositoryService) {
			ServicesRegistry.getInstance().setRepositoryService(
					(RepositoryService) null);
		} else if (svc instanceof IResultReaderService) {
			ServicesRegistry.getInstance().setResultReaderService(null);
		}
		bundleContext.ungetService(svcRef);
	}

	public static void initOutsideOSGi() {
		new Activator().start(null);
	}
}
