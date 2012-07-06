/**
 * 
 */
package com.isencia.passerelle.process.model.Activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.model.factory.EntityManager;
import com.isencia.passerelle.process.model.service.ServiceRegistry;

/**
 * @author "puidir"
 *
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer {

  private static Activator _instance;
  private BundleContext bundleContext;
  
  private ServiceTracker factoryServiceTracker;
  private ServiceTracker managerServiceTracker;

  public static Activator getInstance() {
    return _instance;
  }
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext bundleContext) throws Exception {
    _instance = this;
    this.bundleContext = bundleContext;
    
    factoryServiceTracker = new ServiceTracker(bundleContext, EntityFactory.class.getName(), this);
    factoryServiceTracker.open();
    
    managerServiceTracker = new ServiceTracker(bundleContext, EntityManager.class.getName(), this);
    managerServiceTracker.open();
  }

  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext bundleContext) throws Exception {

    factoryServiceTracker.close();
    managerServiceTracker.close();

    this.bundleContext = null;
  }

  /* (non-Javadoc)
   * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
   */
  public Object addingService(ServiceReference ref) {
    Object service = bundleContext.getService(ref);
    if (service instanceof EntityFactory) {
      ServiceRegistry.getInstance().setEntityFactory((EntityFactory)service);
    } else if (service instanceof EntityManager) {
      ServiceRegistry.getInstance().setEntityManager((EntityManager)service);
    }
    
    return service;
  }

  /* (non-Javadoc)
   * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
   */
  public void modifiedService(ServiceReference ref, Object service) {
  }

  /* (non-Javadoc)
   * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
   */
  public void removedService(ServiceReference ref, Object service) {

    if (service instanceof EntityFactory) {
      ServiceRegistry.getInstance().setEntityFactory((EntityFactory)null);
    } else if (service instanceof EntityManager) {
      ServiceRegistry.getInstance().setEntityManager((EntityManager)null);
    }
    
    bundleContext.ungetService(ref);
  }

}
