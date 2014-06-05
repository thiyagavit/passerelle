package com.isencia.passerelle.process.service;

public interface ProcessManagerService {
	ProcessManager addProcessManager(ProcessManager processManager);
	ProcessManager getProcessManager(String id);
	ProcessManager removeProcessManager(String id);
}
