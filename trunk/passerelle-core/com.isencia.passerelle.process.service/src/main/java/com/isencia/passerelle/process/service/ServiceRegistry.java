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
package com.isencia.passerelle.process.service;

import com.isencia.passerelle.process.model.factory.HistoricalDataProvider;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.service.impl.DefaultRequestProcessingBroker;

/**
 * A central singleton for easy access to all kinds of services used in the Passerelle process domain.
 * 
 * @author "puidir"
 * 
 */
public class ServiceRegistry {
  private static ServiceRegistry _instance = new ServiceRegistry();

  public static ServiceRegistry getInstance() {
    return _instance;
  }

  private ProcessFactory processFactory;
  private ProcessManagerService processManagerService;
  private ProcessPersistenceService processPersistenceService;
  private HistoricalDataProvider historicalDataProvider;
  private RequestProcessingBroker requestProcessingBroker = DefaultRequestProcessingBroker.getInstance();

  public HistoricalDataProvider getHistoricalDataProvider() {
    return historicalDataProvider;
  }

  public ProcessFactory getProcessFactory() {
    return processFactory;
  }

  public ProcessManagerService getProcessManagerService() {
    return processManagerService;
  }

  public ProcessPersistenceService getProcessPersistenceService() {
    return processPersistenceService;
  }

  public RequestProcessingBroker getRequestProcessingBroker() {
    return requestProcessingBroker;
  }
	
  public void setHistoricalDataProvider(HistoricalDataProvider historicalDataProvider) {
	this.historicalDataProvider = historicalDataProvider;
  }
	
  public void setProcessFactory(ProcessFactory processFactory) {
    this.processFactory = processFactory;
  }
  
  public void setProcessManagerService(ProcessManagerService processManagerService) {
	this.processManagerService = processManagerService;
  }

  public void setProcessPersistenceService(ProcessPersistenceService processPersistenceService) {
	this.processPersistenceService = processPersistenceService;
  }
  
  public void setRequestProcessingBroker(RequestProcessingBroker requestProcessingBroker) {
    this.requestProcessingBroker = requestProcessingBroker;
  }
}
