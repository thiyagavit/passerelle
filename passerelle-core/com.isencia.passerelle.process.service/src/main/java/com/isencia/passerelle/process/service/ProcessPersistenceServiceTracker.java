package com.isencia.passerelle.process.service;

public class ProcessPersistenceServiceTracker {
	private static ProcessPersistenceService SERVICE = null;
	
	public static ProcessPersistenceService getService() {
		return(SERVICE);
	}
	
	public static void setService(ProcessPersistenceService service) {
		SERVICE = service;
	}
}
