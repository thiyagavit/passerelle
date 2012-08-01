/**
 * 
 */
package com.isencia.passerelle.process.model.service;

import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.model.factory.EntityManager;
import com.isencia.passerelle.process.model.factory.HistoricalDataProvider;

/**
 * @author "puidir"
 * 
 */
public class ServiceRegistry {

	private EntityFactory entityFactory;
	private EntityManager entityManager;
	private HistoricalDataProvider historicalDataProvider;

	public HistoricalDataProvider getHistoricalDataProvider() {
		return historicalDataProvider;
	}

	public void setHistoricalDataProvider(HistoricalDataProvider historicalDataProvider) {
		this.historicalDataProvider = historicalDataProvider;
	}

	private static ServiceRegistry _instance = new ServiceRegistry();

	public static ServiceRegistry getInstance() {
		return _instance;
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
	}

}
