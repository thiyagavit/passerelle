/**
 * 
 */
package com.isencia.passerelle.process.model.impl.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.process.model.ResultItemFromRawBuilderRegistry;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.impl.ResultItemFromRawBuilderNOP;
import com.isencia.passerelle.process.model.impl.ResultItemFromRawBuilderRegistryImpl;
import com.isencia.passerelle.process.model.impl.factory.ProcessFactoryImpl;
import com.isencia.passerelle.process.service.ServiceRegistry;

/**
 * @author "puidir"
 * 
 */
public class Activator implements BundleActivator {

  private ServiceRegistration<ProcessFactory> factoryServiceRegistration;
  private ServiceRegistration<ResultItemFromRawBuilderRegistry> registryServiceRegistration;

  public void start(BundleContext bundleContext) throws Exception {
    ResultItemFromRawBuilderRegistryImpl rawBuilderRegistryImpl = new ResultItemFromRawBuilderRegistryImpl();
    rawBuilderRegistryImpl.registerBuilder(new ResultItemFromRawBuilderNOP());
    registryServiceRegistration = bundleContext.registerService(ResultItemFromRawBuilderRegistry.class, rawBuilderRegistryImpl, null);
    if (ServiceRegistry.getInstance().getProcessFactory() == null) {
      ProcessFactoryImpl entityFactory = new ProcessFactoryImpl();
      factoryServiceRegistration = bundleContext.registerService(ProcessFactory.class, entityFactory, null);
      ServiceRegistry.getInstance().setProcessFactory(entityFactory);
    }
  }

  public void stop(BundleContext bundleContext) throws Exception {
    if (factoryServiceRegistration != null) {
      ServiceRegistry.getInstance().setProcessFactory(null);
      factoryServiceRegistration.unregister();
    }
    if (registryServiceRegistration != null) {
      registryServiceRegistration.unregister();
    }
  }

}
