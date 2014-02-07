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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.factory.EntityManager;
import com.isencia.passerelle.process.service.ContextRepository;

/**
 * @author erwin
 */
public class ContextRepositoryImpl implements ContextRepository {

  private final static ContextRepository INSTANCE = new ContextRepositoryImpl();

  private EntityManager entityManager;
  private Map<String, Context> contexts = new ConcurrentHashMap<String, Context>();

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
    context.setContextRepositoryID(UUID.randomUUID().toString());
    contexts.put(context.getContextRepositoryID(), context);
    if (entityManager != null) {
      return entityManager.persistContext(context);
    } else {
      return context;
    }
  }

  public Context getContext(String reposId) {
    return contexts.get(reposId);
  }

}
