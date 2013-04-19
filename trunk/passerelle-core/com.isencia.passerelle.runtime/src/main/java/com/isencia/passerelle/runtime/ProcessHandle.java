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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.isencia.passerelle.core.Event;



/**
 * A light-weight handle on a Flow-based process execution.
 * <p>
 * </p>
 * 
 * @author erwin
 *
 */
public interface ProcessHandle {
  
  /**
   * 
   * @return the flow that is running the process
   */
  FlowHandle getFlow();
  
  /**
   * For context-aware executions, this can be used to retrieve 
   * the <code>Context</code> from the <code>ContextRepository</code> if needed.
   * <b>Remark that such retrieval can be a heavy operation and should only be attempted when really necessary.</b> 
   * <br/>
   * For process executions without assigned <code>Context</code>s, this returns an id that can be used to
   * uniquely identify the execution in any related actions, e.g. to obtain execution logs, pause/resume it etc.
   * 
   * @return the UUID of the process execution;
   * 
   */
  String getProcessId();
  
  /**
   * 
   * @return the current execution status
   */
  ProcessExecutionStatus getExecutionStatus();
  
  ProcessHandle terminate();
  ProcessHandle suspend();
  ProcessHandle resume();
  ProcessHandle step();
  ProcessHandle signalEvent(Event event);
  
  /**
   * 
   * @param maxCount
   * @return the list of processing events, from newest to oldest
   */
  List<Event> getProcessEvents(int maxCount);
  
  /**
   * Wait until the process has finished and return the final status.
   * <p>
   * If the process has not finished before the given maximum wait time,
   * a TimeOutException is thrown.
   * </p>
   * @param time
   * @param unit
   * @return the final status
   * 
   * @throws InterruptedException when the waiting thread has been interrupted
   * @throws TimeoutException when the process did not finish within the given maximum wait time
   */
  ProcessExecutionStatus waitUntilFinished(long time, TimeUnit unit) throws TimeoutException, InterruptedException;
}
