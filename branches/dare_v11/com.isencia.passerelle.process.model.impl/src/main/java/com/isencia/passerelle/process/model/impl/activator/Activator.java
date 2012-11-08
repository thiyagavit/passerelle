/**
 * 
 */
package com.isencia.passerelle.process.model.impl.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.model.impl.factory.EntityFactoryImpl;
import com.isencia.passerelle.process.model.service.ServiceRegistry;

/**
 * @author "puidir"
 *
 */
public class Activator implements BundleActivator {

  private ServiceRegistration factoryServiceRegistration;
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext bundleContext) throws Exception {
    EntityFactoryImpl entityFactory = new EntityFactoryImpl();
	factoryServiceRegistration = bundleContext.registerService(EntityFactory.class.getName(), entityFactory, null);
	ServiceRegistry.getInstance().setEntityFactory(entityFactory);
  }

  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext bundleContext) throws Exception {
	  ServiceRegistry.getInstance().setEntityFactory(null);
    if (factoryServiceRegistration != null) {
      factoryServiceRegistration.unregister();
    }
  }

}
