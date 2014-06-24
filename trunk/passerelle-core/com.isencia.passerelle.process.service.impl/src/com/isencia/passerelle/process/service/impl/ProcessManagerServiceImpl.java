package com.isencia.passerelle.process.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.persist.ProcessPersister;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerService;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.runtime.ProcessHandle;

public class ProcessManagerServiceImpl implements ProcessManagerService {

  protected Collection<ContextProcessingCallback> overallCallbacks;
  protected Map<String, ProcessManager> processManagers = new ConcurrentHashMap<String, ProcessManager>(16, 0.9F, 1);

  protected ProcessFactory factory;
  protected ProcessPersister persister;

  public ProcessManager addProcessManager(ProcessManager processManager) {
    return (processManagers.put(processManager.getHandle().getProcessId(), processManager));
  }

  public void destroy() {
    ProcessManagerServiceTracker.setService(null);
  }

  @Override
  public ProcessFactory getFactory() {
    return factory;
  }

  @Override
  public ProcessPersister getPersister() {
    return persister;
  }

  @Override
  public ProcessManager getProcessManager(Request request) {
    return getProcessManager(request.getProcessingContext().getProcessId());
  }

  @Override
  public ProcessManager getProcessManager(String id) {
    return (processManagers.get(id));
  }

  @Override
  public Set<ProcessHandle> getProcessHandles(String userId, boolean master) {
    Set<ProcessHandle> set = new HashSet<ProcessHandle>();

    for (ProcessManager processManager : processManagers.values()) {
      // skip processes that the user is not allowed to see
      String initiator = processManager.getRequest().getInitiator();
      if (!master
          && (userId != null && initiator == null || userId == null && initiator != null || userId != null && initiator != null && !userId.equals(initiator)))
        continue;

      set.add(processManager.getHandle());
    }

    return set;
  }

  public void init() {
    ProcessManagerServiceTracker.setService(this);
  }

  public ProcessManager removeProcessManager(String id) {
    return (processManagers.remove(id));
  }

  public void setFactory(ProcessFactory factory) {
    this.factory = factory;
  }

  public void setPersister(ProcessPersister persister) {
    this.persister = persister;
  }

  @Override
  public boolean subscribeToAll(ContextProcessingCallback callback) {
    return overallCallbacks.add(callback);
  }
  
  @Override
  public boolean unsubscribe(ContextProcessingCallback callback) {
    return overallCallbacks.remove(callback);
  }

  @Override
  public Collection<ContextProcessingCallback> getOverallCallbacks() {
  	return (overallCallbacks);
  }
}
