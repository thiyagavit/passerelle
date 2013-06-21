/**
 * 
 */
package com.isencia.passerelle.process.model.impl.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.model.impl.ContextManagerProxy;
import com.isencia.passerelle.process.model.impl.factory.EntityFactoryImpl;
import com.isencia.passerelle.process.model.service.ServiceRegistry;

/**
 * @author "puidir"
 * 
 */
public class Activator implements BundleActivator {

  private ServiceRegistration factoryServiceRegistration;
  private ContextManagerProxy lifeCycleEntityManagerTracker;

  /*
   * (non-Javadoc)
   * 
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext bundleContext) throws Exception {
    lifeCycleEntityManagerTracker = new ContextManagerProxy(bundleContext);
    lifeCycleEntityManagerTracker.open();
    if (ServiceRegistry.getInstance().getEntityFactory() == null) {
      EntityFactoryImpl entityFactory = new EntityFactoryImpl();
      factoryServiceRegistration = bundleContext.registerService(EntityFactory.class.getName(), entityFactory, null);
      ServiceRegistry.getInstance().setEntityFactory(entityFactory);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext bundleContext) throws Exception {
    lifeCycleEntityManagerTracker.close();
    if (factoryServiceRegistration != null) {
      ServiceRegistry.getInstance().setEntityFactory(null);
      factoryServiceRegistration.unregister();
    }
  }

}
