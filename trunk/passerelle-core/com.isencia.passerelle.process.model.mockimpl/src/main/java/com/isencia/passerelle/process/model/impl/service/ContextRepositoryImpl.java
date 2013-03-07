/**
 * 
 */
package com.isencia.passerelle.process.model.impl.service;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.impl.factory.EntityManagerImpl;
import com.isencia.passerelle.process.model.service.ContextRepository;

/**
 * @author erwin
 *
 */
public class ContextRepositoryImpl implements ContextRepository {
  
  private EntityManagerImpl entityManager;
  
  /**
   * @param entityManager
   */
  public ContextRepositoryImpl(EntityManagerImpl entityManager) {
    super();
    this.entityManager = entityManager;
  }

  public Context storeContext(Context context) {
    return entityManager.persistContext(context);
  }

  public Context getContext(Long id) {
    return entityManager.getContext(id);
  }

}
