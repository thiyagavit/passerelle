/**
 * 
 */
package com.isencia.passerelle.process.service;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.isencia.passerelle.process.model.Task;

/**
 * Abstract base class for RequestProcessingServices that delegate work to an Adapter.
 * 
 * The Adapter typically encapsulates knowledge of the protocol needed to communicate
 * with and external system, e.g. through SOAP, MQUEUEs, etc.
 * 
 * @author puidir
 *
 */
public abstract class AbstractExternalRequestProcessingService<A extends Adapter> implements RequestProcessingService<Task> {

  A adapter;

  public AbstractExternalRequestProcessingService(A adapter) {
    this.adapter = adapter;
  }
  
  @Override
  public Future<Task> process(Task task, Long timeout, TimeUnit unit) {
    return adapter.process(task, timeout, unit);
  }
  
}
