package com.isencia.passerelle.process.service;

public class ProcessPersisterTracker {
	private static ProcessPersister SERVICE = null;
	
	public static ProcessPersister getService() {
		return(SERVICE);
	}
	
	public static void setService(ProcessPersister service) {
		SERVICE = service;
	}
}
