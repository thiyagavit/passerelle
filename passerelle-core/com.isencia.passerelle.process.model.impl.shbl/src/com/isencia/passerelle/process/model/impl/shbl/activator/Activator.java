package com.isencia.passerelle.process.model.impl.shbl.activator;

import org.osgi.framework.BundleContext;

import com.isencia.sherpa.persistence.osgi.AbstractActivator;

public class Activator extends AbstractActivator {
  
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
  }
}
