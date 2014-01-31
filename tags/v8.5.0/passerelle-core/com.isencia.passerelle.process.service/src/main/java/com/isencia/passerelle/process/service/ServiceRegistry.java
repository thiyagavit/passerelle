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
package com.isencia.passerelle.process.service;

import com.isencia.passerelle.process.model.factory.ContextManager;
import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.model.factory.EntityManager;
import com.isencia.passerelle.process.model.factory.HistoricalDataProvider;
import com.isencia.passerelle.process.service.impl.ContextRepositoryImpl;
import com.isencia.passerelle.process.service.impl.DefaultRequestProcessingBroker;

/**
 * A central singleton for easy access to all kinds of services used in the Passerelle process domain.
 * 
 * @author "puidir"
 * 
 */
public class ServiceRegistry {

  private ContextRepository contextRepository = ContextRepositoryImpl.getInstance();
	private EntityFactory entityFactory;
	private EntityManager entityManager;
	private HistoricalDataProvider historicalDataProvider;
  private ContextManager contextManager;
  private RequestProcessingBroker requestProcessingBroker = DefaultRequestProcessingBroker.getInstance();

  private static ServiceRegistry _instance = new ServiceRegistry();

  public static ServiceRegistry getInstance() {
    return _instance;
  }

	/**
   * @return the contextRepository
   */
  public ContextRepository getContextRepository() {
    return contextRepository;
  }

  /**
   * @param contextRepository the contextRepository to set
   */
  public void setContextRepository(ContextRepository contextRepository) {
    this.contextRepository = contextRepository;
  }

  /**
   * 
   * @return the requestProcessingBroker
   */
  public RequestProcessingBroker getRequestProcessingBroker() {
    return requestProcessingBroker;
  }

  /**
   * 
   * @param requestProcessingBroker
   */
  public void setRequestProcessingBroker(RequestProcessingBroker requestProcessingBroker) {
    this.requestProcessingBroker = requestProcessingBroker;
  }

  public HistoricalDataProvider getHistoricalDataProvider() {
		return historicalDataProvider;
	}

	public void setHistoricalDataProvider(HistoricalDataProvider historicalDataProvider) {
		this.historicalDataProvider = historicalDataProvider;
	}

	/**
	 * @return the entityFactory
	 */
	public EntityFactory getEntityFactory() {
		return entityFactory;
	}

	/**
	 * @param entityFactory
	 *            the entityFactory to set
	 */
	public void setEntityFactory(EntityFactory entityFactory) {
		this.entityFactory = entityFactory;
	}

	/**
	 * @return the entityManager
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * @param entityManager
	 *            the entityManager to set
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
		if(contextRepository!=null) {
		  contextRepository.setEntityManager(entityManager);
		}
	}

  /**
   * @return the contextManager service
   */
  public ContextManager getContextManager() {
    return contextManager;
  }

  /**
   * 
   * @param contextManager
   *          the contextManager service to set
   */
  public void setContextManager(ContextManager contextManager) {
    this.contextManager = contextManager;
  }
}
