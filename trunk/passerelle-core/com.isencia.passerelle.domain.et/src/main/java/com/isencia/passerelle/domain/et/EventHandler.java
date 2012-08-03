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
 * Contract for concrete event handlers.
 *  
 * @author delerw
 *
 */
public interface EventHandler {
  
  enum HandleType {
    // indicates that the handler does not want to handle the event
    SKIP,
    // indicates that the handler has no (side)-effects, i.e. the event can freely be offered to other handlers after its processing here
    FUNCTIONAL, 
    // indicates that the handler has (side)-effects, and the event should only be offered to remaining FUNCTIONAL handlers
    EFFECT;
  }
  
  enum HandleResult {
    // means the handler has successfully handled the event
    DONE,
    // means that the handler was not able to handle the event for whatever reason, and is OK with this
    SKIPPED,
    // means that the handler was currently not able to handle the event for whatever reason, but would like to retry later
    // a retry loop will only be done when the event was not yet handled by a previous handler with EFFECT type...
    RETRY;
  }
  
  void initialize();
  
  /**
   * 
   * @param event
   * @return
   */
  HandleType canHandleAs(Event event);
  
  /**
   * 
   * @param event
   * @throws Exception
   * @return 
   */
  HandleResult handle(Event event) throws Exception;

}
