/* Copyright 2011 - iSencia Belgium NV

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

package com.isencia.passerelle.domain.et;

/**
 * Contract for concrete event handlers, dedicated for one type of Event.
 *  
 * @author delerw
 *
 */
public interface EventHandler {
  
  void initialize();
  
  /**
   * 
   * @param event
   * @return true if the handler is made for handling the given event type; false if not
   */
  boolean canHandle(Event event);
  
  /**
   * 
   * @param event
   * @throws Exception
   * @return true if the event was effectively handled; false if not (e.g. because the actor that should be fired because
   * of the given event was aleady busy with another one)
   */
  boolean handle(Event event) throws Exception;

}
