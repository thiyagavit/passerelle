package com.isencia.passerelle.process.actor.util;

import com.isencia.edm.service.result.reader.IResultReaderService;
import com.isencia.passerelle.project.repository.api.RepositoryService;

/**
 * A simple facade to set/get services needed by the diagnostic actors.
 * 
 * When the actors are used in a full OSGi-based app (e.g. Passerelle Manager),
 * the bundle activator is responsible for setting the services based on tracked OSGi services.
 * <br/>
 * When using a simple "standalone" Passerelle app (e.g. the HMI),
 * the application "control" layer is responsible for setting compatible
 * service implementations.
 * <br/>
 * As such, this class acts as an intermediary, a very simplified replacement for 
 * a full DI approach, decoupling the Actor code from e.g. direct usage of OSGi-specific
 * things like Actors, Service Trackers etc.
 * 
 * @author delerw
 *
 */
public class ServicesRegistry {
	
	private RepositoryService repositoryService;
	private IResultReaderService resultReaderService;
	
	private static ServicesRegistry instance = new ServicesRegistry();
	
	public static ServicesRegistry getInstance() {
		return instance;
	}

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public IResultReaderService getResultReaderService() {
		return resultReaderService;
	}

	public void setResultReaderService(IResultReaderService resultReaderService) {
		this.resultReaderService = resultReaderService;
	}
	
}
