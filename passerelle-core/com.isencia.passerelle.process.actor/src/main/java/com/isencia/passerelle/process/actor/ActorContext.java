/* Copyright 2013 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.process.actor;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.factory.ProcessFactoryTracker;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerService;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.process.service.ProcessPersister;
import com.isencia.passerelle.process.service.ProcessPersisterTracker;

/**
 * In the new Passerelle Actor API, the ActorContext is a generic container
 * for attributes etc that may be managed by/for an actor instance.
 * 
 * @author erwin
 */
public class ActorContext extends com.isencia.passerelle.actor.v5.ActorContext {
  private ProcessFactory processFactory;
  private ProcessManagerService processManagerService;
  private ProcessPersister processPersistenceService;

  public ActorContext() {
    this(ProcessFactoryTracker.getService(),ProcessManagerServiceTracker.getService(),ProcessPersisterTracker.getService());
  }
  
  protected ActorContext(ProcessFactory processFactory, ProcessManagerService processManagerService, ProcessPersister processPersistenceService) {
    if(processFactory==null)
      throw new NullPointerException("ProcessFactory can not be null");
    if(processManagerService==null)
      throw new NullPointerException("ProcessManagerService can not be null");
    if(processPersistenceService==null)
      throw new NullPointerException("ProcessPersister can not be null");
    
    this.processFactory = processFactory;
    this.processManagerService = processManagerService;
    this.processPersistenceService = processPersistenceService;
  }
  
  public ProcessFactory getProcessFactory() {
    return processFactory;
  }

  public ProcessManager getProcessManager(Context context) {
	return processManagerService.getProcessManager(context.getProcessId());
  }
  
  public ProcessManagerService getProcessManagerService() {
    return processManagerService;
  }
  
  public ProcessPersister getProcessPersistenceService() {
    return processPersistenceService;
  }
}
