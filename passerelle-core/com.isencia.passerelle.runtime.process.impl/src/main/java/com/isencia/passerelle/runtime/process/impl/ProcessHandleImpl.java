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
package com.isencia.passerelle.runtime.process.impl;

import java.lang.ref.WeakReference;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.FlowNotExecutingException;
import com.isencia.passerelle.runtime.process.ProcessStatus;
import com.isencia.passerelle.runtime.process.impl.executor.FlowExecutionFuture;

/**
 * @author erwin
 */
public class ProcessHandleImpl implements ProcessHandle {

  private FlowHandle flowHandle;
  private String processContextId;
  private ProcessStatus status;
  private WeakReference<FlowExecutionFuture> fetRef;

  /**
   * @param fetFuture
   *          .getStatus()
   */
  public ProcessHandleImpl(FlowExecutionFuture fetFuture) {
    this.processContextId = fetFuture.getProcessContextId();
    this.status = fetFuture.getStatus();
    this.flowHandle = fetFuture.getFlowHandle();
    this.fetRef = new WeakReference<FlowExecutionFuture>(fetFuture);
  }

  @Override
  public FlowHandle getFlow() {
    return flowHandle;
  }

  @Override
  public String getProcessContextId() {
    return processContextId;
  }

  @Override
  public ProcessStatus getExecutionStatus() {
    return status;
  }

  @Override
  public String[] getSuspendedElements() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessStatus waitUntilFinished(long time, TimeUnit unit) throws TimeoutException, InterruptedException, FlowNotExecutingException, ExecutionException {
    FlowExecutionFuture fet = fetRef.get();
    if (fet != null) {
      try {
        status = fet.get(time, unit);
      } catch (CancellationException e) {
        status = fet.getStatus();
      }
      return status;
    } else {
      throw new FlowNotExecutingException(getFlow().getCode());
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((processContextId == null) ? 0 : processContextId.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProcessHandleImpl other = (ProcessHandleImpl) obj;
    if (processContextId == null) {
      if (other.processContextId != null)
        return false;
    } else if (!processContextId.equals(other.processContextId))
      return false;
    if (status != other.status)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ProcessHandleImpl [flowHandle=" + flowHandle.getCode() + ", processContextID=" + processContextId + ", status=" + status + "]";
  }
}
