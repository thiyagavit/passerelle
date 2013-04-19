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

import java.util.List;
import java.util.Map;
import com.isencia.passerelle.core.Event;
import com.isencia.passerelle.model.FlowNotExecutingException;

/**
 * A service interface for everything related to executing a flow,
 * including support for stopping/pausing/resuming/stepping/breakpoints etc.
 * 
 * @author erwin
 *
 */
public interface FlowProcessingService {

  /**
   * Start a process in normal mode, typically executing in one shot until the end.
   * <p>
   * Via a <code>suspend()</code> request, the execution can be suspended.
   * After which it can be continued again via <code>resume()</code>, or per <code>step()</code> etc.
   * </p>
   * @param flowHandle
   * @return
   */
  ProcessHandle start(FlowHandle flowHandle, Map<String, String> parameterOverrides);
  
  /**
   * Start a process in stepping mode, i.e. where actor iterations are done one-by-one,
   * each time a <code>step()</code> has been requested.
   *   
   * @param flowHandle
   * @param parameterOverrides
   * @return
   */
  ProcessHandle startStep(FlowHandle flowHandle, Map<String, String> parameterOverrides);
  
  /**
   * Start a process in debug mode, with one or more breakpoints.
   * <p>
   * Breakpoints must refer to named elements in the running process : actors and/or ports.
   * <br/> The names given should be the full hierarchic names, without the flow's name.
   * E.g. in a HelloWorld model with a Constant actor connected to a Console, valid breakpoints could be :
   * 
   * </p>
   * @param flowHandle
   * @param parameterOverrides
   * @param breakpointNames names of the Flow elements (ports and/or actors) 
   * where the process should place a breakpoint
   * @return
   */
  ProcessHandle startDebug(FlowHandle flowHandle, Map<String, String> parameterOverrides, String... breakpointNames);
  
  ProcessHandle terminate(ProcessHandle processHandle) throws FlowNotExecutingException;
  ProcessHandle suspend(ProcessHandle processHandle) throws FlowNotExecutingException;
  ProcessHandle resume(ProcessHandle processHandle) throws FlowNotExecutingException;
  ProcessHandle step(ProcessHandle processHandle) throws FlowNotExecutingException;
  
  /**
   * Signal an <code>Event</code> to the running process identified by the handle.
   * These can be pure events, or may also pass more complex data (e.g. use input) into a running process.
   * 
   * @param processHandle
   * @param event
   * @return the updated processHandle after delivering the event to the running process
   * @throws FlowNotExecutingException when the process identified by the handle is (no longer) running
   */
  ProcessHandle signalEvent(ProcessHandle processHandle, Event event) throws FlowNotExecutingException;
  
  /**
   * 
   * @param processHandle
   * @param maxCount
   * @return the list of processing events, from newest to oldest and limited to the given maxCount
   */
  List<Event> getProcessEvents(ProcessHandle processHandle, int maxCount);
  
  /**
   * 
   * @param processId
   * @param maxCount
   * @return the list of processing events, from newest to oldest and limited to the given maxCount
   */
  List<Event> getProcessEvents(String processId, int maxCount);

  /**
   * 
   * @param processHandle
   * @return the refreshed handle, i.e. with updated status info
   */
  ProcessHandle refresh(ProcessHandle processHandle);
}
