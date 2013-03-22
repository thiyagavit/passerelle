/**
 * 
 */
package com.isencia.passerelle.process.model.impl.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.model.factory.EntityManager;
import com.isencia.passerelle.process.model.impl.factory.EntityFactoryImpl;
import com.isencia.passerelle.process.model.impl.factory.EntityManagerImpl;
import com.isencia.passerelle.process.model.service.ServiceRegistry;

/**
 * @author "puidir"
 */
public class Activator implements BundleActivator {

  private ServiceRegistration<?> factorySvcReg;
  private ServiceRegistration<?> mgrSvcReg;

  public void start(BundleContext bundleContext) throws Exception {
    EntityFactoryImpl entityFactory = new EntityFactoryImpl();
    EntityManagerImpl entityManager = new EntityManagerImpl();
    ServiceRegistry.getInstance().setEntityFactory(entityFactory);
    ServiceRegistry.getInstance().setEntityManager(entityManager);
    factorySvcReg = bundleContext.registerService(EntityFactory.class.getName(), entityFactory, null);
    mgrSvcReg = bundleContext.registerService(EntityManager.class.getName(), entityManager, null);
  }

  public void stop(BundleContext bundleContext) throws Exception {
    factorySvcReg.unregister();
    mgrSvcReg.unregister();
    ServiceRegistry.getInstance().setEntityFactory(null);
    ServiceRegistry.getInstance().setEntityManager(null);
  }
}
