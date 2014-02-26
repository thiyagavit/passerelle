/* Copyright 2014 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.process.service;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.factory.EntityManager;

/**
 * A <code>ContextRepository</code> provides an API to store & retrieve <code>Context</code>s
 * and associated information.
 * <p> 
 * The main use case is for context-aware actors that need to be able to work with the right <code>Context</code>s
 * for the messages they are processing during a model execution.
 * </p>
 * <p>
 * The ContextRepository also provides a facade for simplified {@link Context} persistence :
 * <ul>
 * <li>via the {@link EntityManager} for the main entity-based persistence (e.g. based on JPA)</li>
 * <li>via additional {@link ContextStore}s for extra/custom trace files, memory stores etc</li>
 * </ul>
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
   * Store a <code>Context</code> in the repository and in the primary entity persistent store, if an {@link EntityManager} has been set.
   * If extra {@link ContextStore}s have been added, these will also be invoked.
   * <p>
   * Remark that a {@link ContextRepository} implementation is allowed to invoke the {@link ContextStore}s asynchronously,
   * and that such storage may fail silently. 
   * On the other hand, the invocation of {@link EntityManager} must be done synchronously in this method and may generate {@link RuntimeException}s.
   * </p> 
   * @param context
   * @return the <code>Context</code> after its storage
   */
  Context storeContext(Context context);

  /**
   * Retrieve the <code>Context</code> with the given identifier
   * 
   * @param processContextId the <code>Context</code>'s repository identifier
   * @return the Context, null if not found
   */
  Context getContext(String processContextId);

  /**
   * Sets the primary EntityManager, responsible for the standard Context persistence. (e.g. based on JPA)
   * 
   * @param entityManager can be null, in which case no standard persistence will be done
   */
  void setEntityManager(EntityManager entityManager);

  /**
   * Adds the given store for use by this repository.
   * <p>
   * All registered auxiliary stores will receive storage requests each time <code>storeContext</code> is invoked.
   * ContextStores should be considered as secondary storage providers, next to the main Context persistence
   * implemented via an {@link EntityManager}.
   * </p>
   * @param store
   * @return
   */
  boolean addAuxiliaryStore(ContextStore store);
  
  /**
   * Removes the given store. 
   * <p>
   * After this operation no further storage operations will be passed to the store,
   * from this repository.
   * </p>
   * @param store
   * @return true if this repository contained the given store and it was removed successfully
   */
  boolean removeAuxiliaryStore(ContextStore store);
}
