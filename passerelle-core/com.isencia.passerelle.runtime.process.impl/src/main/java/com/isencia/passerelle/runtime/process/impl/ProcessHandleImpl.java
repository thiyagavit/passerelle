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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.ProcessStatus;
import com.isencia.passerelle.runtime.process.impl.executor.FlowExecutionFuture;

/**
 * 
 * @author erwin
 *
 */
public class ProcessHandleImpl implements ProcessHandle {
  
  private FlowHandle flowHandle;
  private String processContextID;
  private ProcessStatus status;

  /**
   * 
   * @param fetFuture.getStatus()
   */
  public ProcessHandleImpl(FlowExecutionFuture fetFuture) {
    this.processContextID = fetFuture.getProcessContextID();
    this.status = fetFuture.getStatus();
    this.flowHandle = fetFuture.getFlowHandle();
  }

  @Override
  public FlowHandle getFlow() {
    return flowHandle;
  }

  @Override
  public String getProcessContextId() {
    return processContextID;
  }

  @Override
  public ProcessStatus getExecutionStatus() {
    return status;
  }

  @Override
  public String[] getSuspensionElements() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessStatus waitUntilFinished(long time, TimeUnit unit) throws TimeoutException, InterruptedException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((processContextID == null) ? 0 : processContextID.hashCode());
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
    if (processContextID == null) {
      if (other.processContextID != null)
        return false;
    } else if (!processContextID.equals(other.processContextID))
      return false;
    if (status != other.status)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ProcessHandleImpl [flowHandle=" + flowHandle.getCode() + ", processContextID=" + processContextID + ", status=" + status + "]";
  }
}
