package com.isencia.passerelle.process.service.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerService;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.runtime.ProcessHandle;

public class ProcessManagerServiceImpl implements ProcessManagerService {
	protected Map<String,ProcessManager> processManagers = new ConcurrentHashMap<String,ProcessManager>(16,0.9F,1);

	public ProcessManager addProcessManager(ProcessManager processManager) {
		return(processManagers.put(processManager.getHandle().getProcessId(),processManager));
	}
	
	public void destroy() {
		ProcessManagerServiceTracker.setService(null);
	}
	
	@Override
	public ProcessManager getProcessManager(String id) {
		return(processManagers.get(id));
	}
	
	@Override
	public Set<ProcessHandle> getProcessHandles(String userId, boolean master) {
		Set<ProcessHandle> set = new HashSet<ProcessHandle>();
		
		for (ProcessManager processManager : processManagers.values()) {
			// skip processes that the user is not allowed to see
			String initiator = processManager.getRequest().getInitiator();
			if (!master && (userId != null && initiator == null || userId == null && initiator != null || userId != null && initiator != null && !userId.equals(initiator))) 
		    	continue;

			set.add(processManager.getHandle());
		}

		return set;
	}
	
	public void init() {
		ProcessManagerServiceTracker.setService(this);
	}
	
	public ProcessManager removeProcessManager(String id) {
		return(processManagers.remove(id));
	}
}