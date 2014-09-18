/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.process.service.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.factory.EntityManager;
import com.isencia.passerelle.process.service.ContextRepository;
import com.isencia.passerelle.process.service.ContextStore;

/**
 * @author erwin
 */
public class ContextRepositoryImpl implements ContextRepository {

  private final static Logger LOGGER = LoggerFactory.getLogger(ContextRepositoryImpl.class);
  private final static ContextRepository INSTANCE = new ContextRepositoryImpl();

  private EntityManager entityManager;
  private Map<String, Context> contexts = new ConcurrentHashMap<String, Context>();
  private Set<ContextStore> stores = new HashSet<ContextStore>();

  private final ExecutorService extraStorageService = Executors.newSingleThreadExecutor();

  private ContextRepositoryImpl() {
  }

  public static ContextRepository getInstance() {
    return INSTANCE;
  }

  @Override
  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Context storeContext(Context context) {
    LOGGER.trace("storeContext() - entry : {}", context.getRequest().getType());
    // Check if this is not a duplicate storage call
    if (context.getContextRepositoryID() == null) {
      // This check on the context ID is temporarily needed as Passerelle apps may still use
      // mixed storage/persistence configurations.
      // I.e. the ContextRepository is not yet the global master of storage!
      if (context.getId()==null && entityManager != null) {
        context = entityManager.persistContext(context);
      }
      context.setContextRepositoryID(UUID.randomUUID().toString());
      contexts.put(context.getContextRepositoryID(), context);
      LOGGER.debug("stored {} : {}", context.getRequest().getType(), context.getContextRepositoryID());

      try {
        extraStorageService.execute(new StoreTask(context, stores));
      } catch (Exception e) {
        LOGGER.warn("Error submitting storage to ContextStores for context " + context.getContextRepositoryID(), e);
      }
    } else if (getContext(context.getContextRepositoryID())==null) {
      // strange thing as we're assuming for now that there's only 1 ContextRepository,
      // and the repos ID is assigned only when a context is stored in here.
      LOGGER.warn("ContextRepository inconsistency. Context {} with repository ID {} not found.",
          context.getRequest().getType(), context.getContextRepositoryID());
      contexts.put(context.getContextRepositoryID(), context);
      LOGGER.warn("stored {} : {}", context.getRequest().getType(), context.getContextRepositoryID());
    }
    LOGGER.trace("storeContext() - exit : {} : {}", context.getRequest().getType(), context.getContextRepositoryID());
    return context;
  }

  public Context getContext(String reposId) {
    return contexts.get(reposId);
  }

  @Override
  public boolean addAuxiliaryStore(ContextStore store) {
    return stores.add(store);
  }

  @Override
  public boolean removeAuxiliaryStore(ContextStore store) {
    return stores.remove(store);
  }

  /**
   * Task to store a Context asynchronously in the configured stores.
   *
   * @author erwindl
   *
   */
  private static class StoreTask implements Runnable {
    private Set<ContextStore> stores;
    private Context context;

    public StoreTask(Context context, Set<ContextStore> stores) {
      this.context = context;
      this.stores = new HashSet<ContextStore>(stores);
    }

    public void run() {
      for (ContextStore store : stores) {
        try {
          store.storeContext(context);
          LOGGER.debug("Stored context {} in store {}", context.getContextRepositoryID(), store);
        } catch (Exception e) {
          LOGGER.warn("Storage failed for context " + context.getContextRepositoryID() + " in store " + store, e);
        }
      }
    }
  }
}