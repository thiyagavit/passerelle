package com.isencia.passerelle.process.model.service;

import com.isencia.passerelle.process.model.Context;

/**
 * A <code>ContextRepository</code> provides an API to store & retrieve <code>Context</code>s
 * and associated information.
 * <p> 
 * The main use case is for context-aware actors that need to be able to work with the right <code>Context</code>s
 * for the messages they are processing during a model execution.
 * </p>
 * 
 * TODO : simplify and formalize responsibilities between this <code>ContextRepository</code>, 
 * <code>com.isencia.passerelle.process.model.factory.ContextManager</code> and <code>com.isencia.passerelle.process.model.factory.EntityManager</code>.
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
