package com.isencia.passerelle.process.service;

import java.util.Set;

import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.persist.ProcessPersister;
import com.isencia.passerelle.runtime.ProcessHandle;

public interface ProcessManagerService {
	ProcessFactory getFactory();
	ProcessPersister getPersister();
	ProcessManager addProcessManager(ProcessManager processManager);
	Set<ProcessHandle> getProcessHandles(String userId, boolean master);
	ProcessManager getProcessManager(String id);
	ProcessManager getProcessManager(Request request);
	ProcessManager removeProcessManager(String id);
}
