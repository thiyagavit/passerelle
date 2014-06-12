package com.isencia.passerelle.process.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerService;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;

public class ProcessManagerServiceImpl implements ProcessManagerService {
	private Map<String,ProcessManager> processManagers = new ConcurrentHashMap<String,ProcessManager>(16,0.9F,1);

	public ProcessManager addProcessManager(ProcessManager processManager) {
		return(processManagers.put(processManager.getId(),processManager));
	}
	
	public void destroy() {
		ProcessManagerServiceTracker.setService(null);
	}
	
	@Override
	public ProcessManager getProcessManager(String requestId) {
		return(processManagers.get(requestId));
	}
	
	public void init() {
		ProcessManagerServiceTracker.setService(this);
	}
	
	public ProcessManager removeProcessManager(String requestId) {
		return(processManagers.remove(requestId));
	}
}
