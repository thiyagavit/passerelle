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
import com.isencia.passerelle.process.model.impl.service.ContextRepositoryImpl;
import com.isencia.passerelle.process.model.service.ContextRepository;
import com.isencia.passerelle.process.model.service.ServiceRegistry;

/**
 * @author "puidir"
 */
public class Activator implements BundleActivator {

  private ServiceRegistration<?> factorySvcReg;
  private ServiceRegistration<?> mgrSvcReg;
  private ServiceRegistration<?> ctxtReposSvcReg;
  
  public void start(BundleContext bundleContext) throws Exception {
    EntityFactoryImpl entityFactory = new EntityFactoryImpl();
    EntityManagerImpl entityManager = new EntityManagerImpl();
    ContextRepositoryImpl contextRepositoryImpl = new ContextRepositoryImpl(entityManager);
    
    ServiceRegistry.getInstance().setEntityFactory(entityFactory);
    ServiceRegistry.getInstance().setEntityManager(entityManager);
    ServiceRegistry.getInstance().setContextRepository(contextRepositoryImpl);
    
    factorySvcReg = bundleContext.registerService(EntityFactory.class.getName(), entityFactory, null);
    mgrSvcReg = bundleContext.registerService(EntityManager.class.getName(), entityManager, null);
    ctxtReposSvcReg = bundleContext.registerService(ContextRepository.class.getName(), contextRepositoryImpl, null);
  }

  public void stop(BundleContext bundleContext) throws Exception {
    ctxtReposSvcReg.unregister();
    factorySvcReg.unregister();
    mgrSvcReg.unregister();
    ServiceRegistry.getInstance().setEntityFactory(null);
    ServiceRegistry.getInstance().setEntityManager(null);
    ServiceRegistry.getInstance().setContextRepository(null);
  }
}
