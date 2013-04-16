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
package com.isencia.passerelle.runtime;


/**
 * @author erwin
 *
 */
public interface ProcessHandle {
  
  /**
   * For context-aware executions, this returns the same as <code>getProcessingContextId()</code>.
   * For executions without assigned <code>Context</code>s, this returns an id that can be used to
   * uniquely identify the execution in any related actions, e.g. to obtain execution logs, pause/resume it etc.
   * 
   * @return the UUID of the process execution;
   * 
   */
  String getExecutionId();
  
  /**
   * 
   * @return the flow that is running the process
   */
  FlowHandle getFlow();
  
  /**
   * For non-context-aware executions, this returns null.<br/>
   * For context-aware executions, the returned id can be used to retrieve 
   * the <code>Context</code> from the <code>ContextRepository</code> if needed.
   * Remark that such retrieval can be a heavy operation and should only be attempted when really necessary. 
   * 
   * @return the UUID for the processing context, or null if no context was assigned.
   */
  String getProcessingContextId();

}
