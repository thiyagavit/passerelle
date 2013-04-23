/**
 * 
 */
package com.isencia.passerelle.runtime;

import com.isencia.passerelle.core.Event;

/**
 * 
 * TODO : check what's better : different methods per large event categories
 * or one hyper-generic method as below...
 * 
 * @author erwin
 *
 */
public interface ProcessListener {
  
  /**
   * 
   * @param event
   */
  void handle(Event event);

}
