package com.isencia.passerelle.process.model.impl.shbl;

import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.sherpa.persistence.commons.TargetDatabase;
import com.isencia.sherpa.persistence.jpa.SherpaPersistenceUnit;
import com.isencia.sherpa.persistence.jpa.Slf4jSessionLogger;
import com.isencia.sherpa.persistence.valuetypes.EntityManagerScope;

public class PersistenceUnit extends SherpaPersistenceUnit {
  public static final Logger LOGGER = LoggerFactory.getLogger(PersistenceUnit.class);

  public PersistenceUnit() {
    super("passerelle");

    // indicate that LightWeightEntityManager should be used (see PasserellePersistenceProvider)
    addProperty(LIGHTWEIGHT,"true");
    
    setEntityManagerScope(EntityManagerScope.TRANSACTION);
    
    try {
      setNonJtaDataSource((DataSource)JndiResourceFactory.findJndiResource("jdbc/passerelle-edm"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    setTargetDatabase(TargetDatabase.Derby);

    addEntities(new String[]{
        "com.isencia.passerelle.process.model.impl.CaseImpl",
        "com.isencia.passerelle.process.model.impl.ClobItem", 
//        "com.isencia.passerelle.process.model.impl.ContextErrorEventImpl",
        "com.isencia.passerelle.process.model.impl.ContextEventImpl",
        "com.isencia.passerelle.process.model.impl.ContextImpl", 
        "com.isencia.passerelle.process.model.impl.DoubleResultItemImpl",
        "com.isencia.passerelle.process.model.impl.MainRequestImpl", 
        "com.isencia.passerelle.process.model.impl.RequestAttributeImpl",
        "com.isencia.passerelle.process.model.impl.RequestImpl", 
        "com.isencia.passerelle.process.model.impl.ResultBlockAttributeImpl",
        "com.isencia.passerelle.process.model.impl.ResultBlockImpl", 
        "com.isencia.passerelle.process.model.impl.ResultItemAttributeImpl",
        "com.isencia.passerelle.process.model.impl.ResultItemImpl", 
        "com.isencia.passerelle.process.model.impl.StringResultItemImpl",
        "com.isencia.passerelle.process.model.impl.TaskImpl"
    });

    addProperty(PersistenceUnitProperties.LOGGING_LOGGER,Slf4jSessionLogger.class.getName());
  }
}