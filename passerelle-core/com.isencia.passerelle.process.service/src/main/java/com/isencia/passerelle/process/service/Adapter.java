package com.isencia.passerelle.process.service;

/**
 * Encapsulates knowledge of the protocol needed to communicate
 * with and external system, e.g. through SOAP, MQUEUEs, etc.
 * 
 * @author puidir
 *
 */
public interface Adapter extends RequestProcessingService {

  /**
   * 
   * @return The name of the external service
   */
  String getServiceName();
  
}
