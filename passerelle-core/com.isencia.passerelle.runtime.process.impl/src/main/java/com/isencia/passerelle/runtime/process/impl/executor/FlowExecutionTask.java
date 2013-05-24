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
package com.isencia.passerelle.runtime.process.impl.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RunnableFuture;
import ptolemy.actor.ExecutionListener;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Manager;
import ptolemy.actor.Manager.State;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.FlowProcessingService.StartMode;
import com.isencia.passerelle.runtime.ProcessListener;
import com.isencia.passerelle.runtime.ProcessStatus;

/**
 * 
 * @author erwin
 *
 */
public class FlowExecutionTask implements CancellableTask<ProcessStatus>, ExecutionListener {
  private final static Map<State, ProcessStatus> STATUS_MAPPING = new HashMap<Manager.State, ProcessStatus>();
  
  private final FlowHandle flowHandle;
  private final String processContextID;
  private volatile ProcessStatus status;
  private volatile boolean canceled;
  private Manager manager;

  public FlowExecutionTask(StartMode mode, FlowHandle flowHandle, String processContextId, Map<String, String> parameterOverrides, ProcessListener listener, String... breakpointNames) {
    if(flowHandle==null) throw new IllegalArgumentException("FlowHandle can not be null");
    this.flowHandle = flowHandle;
    this.processContextID = processContextId;
    status = ProcessStatus.IDLE;
  }
  
  /**
   * 
   * @return the flow that is being executed by this task
   */
  public FlowHandle getFlowHandle() {
    return flowHandle;
  }
  
  /**
   * 
   * @return the process context ID for this execution
   */
  public String getProcessContextID() {
    return processContextID;
  }
  
  /**
   * Performs the real flow execution on the caller's thread.
   * @return the final status after the model execution has terminated
   */
  @Override
  public ProcessStatus call() throws Exception {
    try {
      Flow flow = flowHandle.getFlow();
      manager = new Manager(flow.workspace(), flow.getName());
      manager.addExecutionListener(this);
      flow.setManager(manager);
      manager.execute();
      // Just to be sure that for blocking executes, 
      // we don't miss the final manager state changes before returning.
      managerStateChanged(manager);
    } catch (Exception e) {
      if(e.getCause() instanceof PasserelleException) {
        throw ((PasserelleException)e.getCause());
      } else {
        throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, flowHandle.toString(), e);
      }
    }
    return status;
  }
  
  /**
   * Cancel the flow execution in a clean way.
   */
  public synchronized void cancel() {
    canceled = true;
    if(manager!=null) {
      manager.finish();
    }
  }
  
  public RunnableFuture<ProcessStatus> newFutureTask() {
    return new FlowExecutionFuture(this);
  }

  /**
   * 
   * @return the current flow execution status
   */
  public ProcessStatus getStatus() {
    return status;
  }
  
  /**
   * Updates the flow execution status to <code>ProcessStatus.ERROR</code>
   */
  @Override
  public void executionError(ptolemy.actor.Manager manager, Throwable throwable) {
    status = ProcessStatus.ERROR;
  }

  /**
   * Updates the flow execution status to <code>ProcessStatus.FINISHED</code>,
   * or <code>ProcessStatus.INTERRUPTED</code> if the execution finished due to a cancel.
   */
  @Override
  public void executionFinished(ptolemy.actor.Manager manager) {
    if(status==null || !status.isFinalStatus()) {
      if(!canceled) {
        status = ProcessStatus.FINISHED;
      } else {
        status = ProcessStatus.INTERRUPTED;
      }
    }
  }

  /**
   * Changes the flow execution status according to the new manager state.
   */
  @Override
  public void managerStateChanged(ptolemy.actor.Manager manager) {
    State state = manager.getState();
    if(status==null || !status.isFinalStatus()) {
      status = STATUS_MAPPING.get(state);
    }
  }

  static {
    STATUS_MAPPING.put(Manager.IDLE, ProcessStatus.IDLE);
    STATUS_MAPPING.put(Manager.INITIALIZING, ProcessStatus.STARTING);
    STATUS_MAPPING.put(Manager.PREINITIALIZING, ProcessStatus.STARTING);
    STATUS_MAPPING.put(Manager.RESOLVING_TYPES, ProcessStatus.STARTING);
    STATUS_MAPPING.put(Manager.ITERATING, ProcessStatus.ACTIVE);
    STATUS_MAPPING.put(Manager.PAUSED, ProcessStatus.SUSPENDED);
    STATUS_MAPPING.put(Manager.PAUSED_ON_BREAKPOINT, ProcessStatus.SUSPENDED);
    STATUS_MAPPING.put(Manager.WRAPPING_UP, ProcessStatus.STOPPING);
    STATUS_MAPPING.put(Manager.EXITING, ProcessStatus.STOPPING);
    STATUS_MAPPING.put(Manager.CORRUPTED, ProcessStatus.ERROR);
    STATUS_MAPPING.put(Manager.THROWING_A_THROWABLE, ProcessStatus.ERROR);
  };
}
