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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.isencia.passerelle.runtime.process.FlowNotExecutingException;
import com.isencia.passerelle.runtime.process.ProcessStatus;



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
   * For the rare process executions without assigned <code>Context</code>s, this returns an id that can be used to
   * uniquely identify the execution in any related actions, e.g. to obtain execution logs, pause/resume it etc.
   * 
   * @return the UUID of the process execution;
   * 
   */
  String getProcessContextId();
  
  /**
   * 
   * @return the current execution status
   */
  ProcessStatus getExecutionStatus();
  
  /**
   * Suspensions can  be caused by breakpoints and/or the end of a step execution.
   * @return the names of the currently suspended Flow elements (typically actors)
   */
  String[] getSuspensionElements();

  /**
   * Wait until the process has finished and return the final status.
   * <p>
   * If the process has not finished before the given maximum wait time,
   * a TimeOutException is thrown.
   * <br/>
   * If the process has already finished and its information can still be retrieved,
   * the call will return immediately with the final status.
   * <br/>
   * If no execution information can be found, a FlowNotExecutingException will be thrown.
   * </p>
   * @param time
   * @param unit
   * @return the final status
   * 
   * @throws FlowNotExecutingException when no process execution information was found.
   * @throws ExecutionException when there was an error executing the process; 
   *  Typically <code>getCause()</code> will contain a <code>PasserelleException</code> with concrete error info.
   * @throws InterruptedException when the waiting thread has been interrupted
   * @throws TimeoutException when the process did not finish within the given maximum wait time
   */
  ProcessStatus waitUntilFinished(long time, TimeUnit unit) throws FlowNotExecutingException, ExecutionException, TimeoutException, InterruptedException;
}
