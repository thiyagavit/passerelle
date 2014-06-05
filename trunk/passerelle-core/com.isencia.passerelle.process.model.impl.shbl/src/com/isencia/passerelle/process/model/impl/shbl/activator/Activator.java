package com.isencia.passerelle.process.model.impl.shbl.activator;

import org.osgi.framework.BundleContext;

import com.isencia.sherpa.persistence.osgi.AbstractActivator;

public class Activator extends AbstractActivator {
  
//  private ServiceRegistration<?> mgrSvcReg;
  
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    
    // set a custom provider that creates a LightWeightEntityManager if defined with PersistenceUnit.LIGHTWEIGHT
//    SherpaPersistence.setProvider(new PasserellePersistenceProvider());
    // register the PersistenceUnit on the application context
//    PersistenceApplicationContext.setPersistenceUnit(new PersistenceUnit());
    // force creation of the factory to load meta-data now
//    EntityManagerPool.setPersistenceUnit(new PersistenceUnit());
//    EntityManagerPool.createFactory("passerelle");
  
//    if (ServiceRegistry.getInstance().getEntityManager() == null) {
//      EntityManager entityManager = (EntityManager)Class.forName("com.isencia.passerelle.process.model.impl.shbl.LightWeightEntityManagerWrapper").newInstance();
//      ServiceRegistry.getInstance().setEntityManager(entityManager);
//      mgrSvcReg = context.registerService(EntityManager.class.getName(), entityManager, null);
//    }
  }

  public void stop(BundleContext context) throws Exception {
//    EntityManagerPool.closeFactory("passerelle");

//    if (mgrSvcReg != null)
//      mgrSvcReg.unregister();
  }
}
