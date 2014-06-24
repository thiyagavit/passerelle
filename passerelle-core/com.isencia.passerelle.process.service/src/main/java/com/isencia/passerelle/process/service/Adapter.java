package com.isencia.passerelle.process.service;

import com.isencia.passerelle.process.model.Task;

/**
 * Encapsulates knowledge of the protocol needed to communicate
 * with and external system, e.g. through SOAP, MQUEUEs, etc.
 * 
 * @author puidir
 *
 */
public interface Adapter extends RequestProcessingService<Task> {

  /**
   * 
   * @return The name of the external service
   */
  String getServiceName();
  
}
