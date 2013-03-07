package com.isencia.passerelle.process.model.service;

import com.isencia.passerelle.process.model.Context;

/**
 * 
 * @author erwin
 *
 */
public interface ContextRepository {

  /**
   * Store a context in the repository
   * 
   * @param context
   * @return
   */
  Context storeContext(Context context);

  /**
   * Retrieve the context with the given identifier
   * 
   * @param id Context identifier
   * @return The Context, null if not found
   */
  Context getContext(Long id);

}
