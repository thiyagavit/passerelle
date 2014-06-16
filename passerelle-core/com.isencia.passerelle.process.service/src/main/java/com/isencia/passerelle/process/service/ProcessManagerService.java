package com.isencia.passerelle.process.service;

import java.util.Set;

import com.isencia.passerelle.runtime.ProcessHandle;

public interface ProcessManagerService {
	ProcessManager addProcessManager(ProcessManager processManager);
	Set<ProcessHandle> getProcessHandles(String userId, boolean master);
	ProcessManager getProcessManager(String id);
	ProcessManager removeProcessManager(String id);
}
