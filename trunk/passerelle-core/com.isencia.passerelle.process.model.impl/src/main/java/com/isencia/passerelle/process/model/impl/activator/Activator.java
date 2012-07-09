/**
 * 
 */
package com.isencia.passerelle.process.model.impl.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.model.impl.factory.EntityFactoryImpl;

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
    factoryServiceRegistration = bundleContext.registerService(EntityFactory.class.getName(), new EntityFactoryImpl(), null);
  }

  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext bundleContext) throws Exception {
    if (factoryServiceRegistration != null) {
      factoryServiceRegistration.unregister();
    }
  }

}
