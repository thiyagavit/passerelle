package com.isencia.passerelle.runtime.ws.rest.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.ws.rest.RestTest;

public class Activator implements BundleActivator {

	private static BundleContext context;
  private ServiceRegistration<?> flowReposSvcReg;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		flowReposSvcReg = context.registerService(RestTest.class.getName(), new RestTest(), null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
	  flowReposSvcReg.unregister();
		Activator.context = null;
	}
}
