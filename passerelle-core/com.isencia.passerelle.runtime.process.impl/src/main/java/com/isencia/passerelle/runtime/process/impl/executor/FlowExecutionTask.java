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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.RunnableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager.State;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Manager;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.process.ProcessListener;
import com.isencia.passerelle.runtime.process.ProcessStatus;
import com.isencia.passerelle.runtime.process.FlowProcessingService.StartMode;

/**
 * @author erwin
 */
public class FlowExecutionTask implements CancellableTask<ProcessStatus>, ExecutionListener {

  private final static Logger LOGGER = LoggerFactory.getLogger(FlowExecutionTask.class);

  private final static Map<State, ProcessStatus> STATUS_MAPPING = new HashMap<Manager.State, ProcessStatus>();

  private final FlowHandle flowHandle;
  private final Map<String, String> parameterOverrides;
  private final String processContextId;
  private volatile ProcessStatus status;
  private volatile boolean canceled;
  private volatile boolean busy;
  private volatile boolean suspended;
  private Manager manager;

  public FlowExecutionTask(StartMode mode, FlowHandle flowHandle, String processContextId, Map<String, String> parameterOverrides, ProcessListener listener,
      String... breakpointNames) {
    if (flowHandle == null)
      throw new IllegalArgumentException("FlowHandle can not be null");
    this.flowHandle = flowHandle;
    if (parameterOverrides != null) {
      this.parameterOverrides = new HashMap<String, String>(parameterOverrides);
    } else {
      this.parameterOverrides = null;
    }
    this.processContextId = processContextId;
    status = ProcessStatus.IDLE;
  }

  public RunnableFuture<ProcessStatus> newFutureTask() {
    return new FlowExecutionFuture(this);
  }

  /**
   * @return the flow that is being executed by this task
   */
  public FlowHandle getFlowHandle() {
    return flowHandle;
  }

  /**
   * @return the process context ID for this execution
   */
  public String getProcessContextId() {
    return processContextId;
  }

  /**
   * Performs the real flow execution on the caller's thread.
   * 
   * @return the final status after the model execution has terminated
   */
  @Override
  public ProcessStatus call() throws Exception {
    LOGGER.trace("call() - Context {} - Flow {}", processContextId, flowHandle.getCode());
    try {
      Flow flow = flowHandle.getFlow();
      applyParameterSettings(flow, parameterOverrides);
      synchronized (this) {
        manager = new Manager(flow.workspace(), processContextId);
        manager.addExecutionListener(this);
        flow.setManager(manager);
        busy = true;
      }
      if (!canceled) {
        LOGGER.info("Context {} - Starting execution of flow {}", processContextId, flowHandle.getCode());
        manager.execute();
        // Just to be sure that for blocking executes,
        // we don't miss the final manager state changes before returning.
        managerStateChanged(manager);
      }
    } catch (Exception e) {
      if (e.getCause() instanceof PasserelleException) {
        throw ((PasserelleException) e.getCause());
      } else {
        throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, flowHandle.toString(), e);
      }
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("call() exit - Context {} - Flow {} - Final Status {}", new Object[] { processContextId, flowHandle.getCode(), status });
    }
    return status;
  }

  /**
   * Cancel the flow execution in a clean way.
   */
  public synchronized void cancel() {
    LOGGER.trace("cancel() - Context {} - Flow {}", processContextId, flowHandle.getCode());
    canceled = true;
    if (busy) {
      LOGGER.warn("Context {} - Canceling execution of flow {}", processContextId, flowHandle.getCode());
      manager.finish();
    } else {
      LOGGER.warn("Context {} - Canceling execution of flow {} before it started", processContextId, flowHandle.getCode());
      status = ProcessStatus.INTERRUPTED;
      manager = null;
    }
  }

  public synchronized boolean suspend() {
    LOGGER.trace("suspend() - Context {} - Flow {}", processContextId, flowHandle.getCode());
    suspended = true;
    if (busy) {
      manager.pause();
      return true;
    } else {
      return false;
    }
  }

  public synchronized boolean resume() {
    LOGGER.trace("resume() - Context {} - Flow {}", processContextId, flowHandle.getCode());
    suspended = false;
    if (busy && Manager.PAUSED.equals(manager.getState())) {
      manager.resume();
      return true;
    } else {
      return false;
    }
  }

  /**
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
    LOGGER.warn("Context " + processContextId + " - Execution error of flow " + getFlowHandle().getCode(), throwable);
    status = ProcessStatus.ERROR;
  }

  /**
   * Updates the flow execution status to <code>ProcessStatus.FINISHED</code>, or <code>ProcessStatus.INTERRUPTED</code> if the execution finished due to a
   * cancel.
   */
  @Override
  public void executionFinished(ptolemy.actor.Manager manager) {
    if (status == null || !status.isFinalStatus()) {
      if (!canceled) {
        LOGGER.info("Context {} - Execution finished of flow {}", processContextId, getFlowHandle().getCode());
        status = ProcessStatus.FINISHED;
      } else {
        LOGGER.warn("Context {} - Execution interrupted of flow {}", processContextId, getFlowHandle().getCode());
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
    LOGGER.trace("Context {} - Manager state change of flow {} : {}", new Object[] { processContextId, getFlowHandle().getCode(), state });
    if (status == null || !status.isFinalStatus()) {
      ProcessStatus oldStatus = status;
      status = STATUS_MAPPING.get(state);
      if (canceled && ProcessStatus.IDLE.equals(status)) {
        status = ProcessStatus.INTERRUPTED;
      }
      if (oldStatus != status) {
        LOGGER.debug("Context {} - Execution state change of flow {} : {}", new Object[] { processContextId, getFlowHandle().getCode(), status });
      }
      // This handles the case where a suspend() call was done right after the (asynch) start,
      // before the actual execution was effectively already started.
      // The behaviour
      if (suspended && ProcessStatus.ACTIVE.equals(status)) {
        manager.pause();
      }
    }
  }

  protected void applyParameterSettings(Flow flow, Map<String, String> props) throws PasserelleException {
    if(props!=null) {
      Iterator<Entry<String, String>> propsItr = props.entrySet().iterator();
      while (propsItr.hasNext()) {
        Entry<String, String> element = propsItr.next();
        String propName = element.getKey();
        String propValue = element.getValue();
        String[] nameParts = propName.split("[\\.]");
  
        // set model parameters
        if (nameParts.length == 1 && !flow.attributeList().isEmpty()) {
          try {
            final Parameter p = (Parameter) flow.getAttribute(nameParts[0], Parameter.class);
            setParameter(flow, p, propName, propValue);
          } catch (final IllegalActionException e1) {
            throw new PasserelleException(ErrorCode.FLOW_CONFIGURATION_ERROR, "Inconsistent parameter definition " + propName, flow, e1);
          }
        }
        // parts[parts.length-1] is the parameter name
        // all the parts[] before that are part of the nested Parameter name
        Entity e = flow;
        for (int j = 0; j < nameParts.length - 1; j++) {
          if (e instanceof CompositeActor) {
            Entity test = ((CompositeActor) e).getEntity(nameParts[j]);
            if (test == null) {
              try {
                // maybe it is a director
                ptolemy.actor.Director d = ((CompositeActor) e).getDirector();
                if (d != null) {
                  Parameter p = (Parameter) d.getAttribute(nameParts[nameParts.length - 1], Parameter.class);
                  setParameter(flow, p, propName, propValue);
                }
              } catch (IllegalActionException e1) {
                throw new PasserelleException(ErrorCode.FLOW_CONFIGURATION_ERROR, "Inconsistent parameter definition " + propName, flow, e1);
              }
            } else {
              e = ((CompositeActor) e).getEntity(nameParts[j]);
              if (e != null) {
                try {
                  Parameter p = (Parameter) e.getAttribute(nameParts[nameParts.length - 1], Parameter.class);
                  setParameter(flow, p, propName, propValue);
                } catch (IllegalActionException e1) {
                  throw new PasserelleException(ErrorCode.FLOW_CONFIGURATION_ERROR, "Inconsistent parameter definition " + propName, flow, e1);
                }
              }
            }
          } else {
            break;
          }
        }
      }
    }
  }

  private void setParameter(Flow flow, final Parameter p, String propName, String propValue) throws PasserelleException {
    if (p != null) {
      p.setExpression(propValue);
      p.setPersistent(true);
      LOGGER.info("Context {} - Override parameter {} : {}", new Object[] { processContextId, propName, propValue });
    } else {
      throw new PasserelleException(ErrorCode.FLOW_CONFIGURATION_ERROR, "Inconsistent parameter definition " + propName, flow, null);
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
