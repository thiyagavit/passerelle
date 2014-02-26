package com.isencia.passerelle.process.service.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.process.service.ContextRepository;
import com.isencia.passerelle.process.service.ServiceRegistry;
import com.isencia.passerelle.process.service.impl.ContextRepositoryImpl;

public class Activator implements BundleActivator {

	private static Activator instance;
  private ServiceRegistration<?> svcReg;
	
	static Activator getInstance() {
		return instance;
	}

	public void start(BundleContext bundleContext) throws Exception {
		instance = this;
		
		ContextRepository contextRepository = ContextRepositoryImpl.getInstance();
		svcReg = bundleContext.registerService(ContextRepository.class.getName(), contextRepository, null);
    ServiceRegistry.getInstance().setContextRepository(contextRepository);
	}

	public void stop(BundleContext bundleContext) throws Exception {
	  svcReg.unregister();
    ServiceRegistry.getInstance().setContextRepository(null);
		instance = null;
	}

}
