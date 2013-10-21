/**
 * 
 */
package com.isencia.passerelle.process.model.impl.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.factory.EntityManager;
import com.isencia.passerelle.process.model.impl.ContextImpl;
import com.isencia.passerelle.process.service.ContextRepository;

/**
 * @author erwin
 *
 */
public class ContextRepositoryImpl implements ContextRepository {
  
  private EntityManager entityManager;
  private Map<String, Context> contexts = new ConcurrentHashMap<String, Context>();
  
  /**
   * @param entityManager
   */
  public ContextRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Context storeContext(Context context) {
    ((ContextImpl)context).setContextRepositoryID(UUID.randomUUID().toString());
    contexts.put(context.getContextRepositoryID(), context);
    return entityManager.persistContext(context);
  }

  public Context getContext(String reposId) {
    return contexts.get(reposId);
  }

}
