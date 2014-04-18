package com.isencia.passerelle.process.service.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.isencia.passerelle.process.service.RequestProcessingService;
import com.isencia.passerelle.process.service.impl.DefaultRequestProcessingBroker;

public class Activator implements BundleActivator {

	private static Activator instance;
	private BundleContext bundleContext;
	
	private ServiceTracker<RequestProcessingService, RequestProcessingService> requestProcessingServiceTracker;
	
	static Activator getInstance() {
		return instance;
	}

	public void start(BundleContext bundleContext) throws Exception {
		instance = this;
		this.bundleContext = bundleContext;
		
		requestProcessingServiceTracker = new ServiceTracker<RequestProcessingService, RequestProcessingService>(
		    bundleContext, 
		    RequestProcessingService.class, 
		    new RequestProcessingServiceTrackerCustomizer());
		requestProcessingServiceTracker.open();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		if (requestProcessingServiceTracker != null) {
		  requestProcessingServiceTracker.close();
		  requestProcessingServiceTracker = null;
		}
	  
	  instance = null;
		bundleContext = null;
	}

	private class RequestProcessingServiceTrackerCustomizer implements ServiceTrackerCustomizer<RequestProcessingService, RequestProcessingService> {

    @Override
    public RequestProcessingService addingService(ServiceReference<RequestProcessingService> reference) {
      RequestProcessingService rps = bundleContext.getService(reference);
      DefaultRequestProcessingBroker.getInstance().registerService(rps);
      return rps;
    }

    @Override
    public void modifiedService(ServiceReference<RequestProcessingService> reference, RequestProcessingService service) {
    }

    @Override
    public void removedService(ServiceReference<RequestProcessingService> reference, RequestProcessingService service) {
      DefaultRequestProcessingBroker.getInstance().removeService(service);
      bundleContext.ungetService(reference);
    }
  }
}