package com.isencia.passerelle.process.model.impl.shbl;

import java.util.Map;

import com.isencia.sherpa.persistence.jpa.LightWeightEntityManager;
import com.isencia.sherpa.persistence.jpa.SherpaEntityManager;
import com.isencia.sherpa.persistence.jpa.SherpaEntityManagerFactoryDelegate;
import com.isencia.sherpa.persistence.jpa.SherpaPersistenceProvider;
import com.isencia.sherpa.persistence.jpa.SherpaPersistenceUnit;

public class PasserellePersistenceProvider extends SherpaPersistenceProvider {
  @Override
  protected SherpaEntityManager createEntityManager(SherpaEntityManagerFactoryDelegate factory, Map<?, ?> properties) {
    String property = (String)factory.getProperty(SherpaPersistenceUnit.LIGHTWEIGHT);
    if (property != null && property.equalsIgnoreCase("true"))
      return(new LightWeightEntityManager(factory,null));

    return(super.createEntityManager(factory, properties));
  }
}
