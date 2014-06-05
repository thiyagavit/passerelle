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
package com.isencia.passerelle.process.actor.forkjoin;

import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.process.actor.ProcessRequest;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ServiceRegistry;

/**
 * A <code>AggregationStrategy</code> that looks for processing <code>Context</code>s in the sequenced messages,
 * and aggregates their tasks and results.
 * 
 * @author erwin
 *
 */
public class ContextAggregationStrategy implements AggregationStrategy {

  public ContextAggregationStrategy() {
  }

  public ManagedMessage aggregateMessages(ManagedMessage initialMsg, ManagedMessage... otherMessages) throws MessageException {
    MessageContainer scopeMsg = (MessageContainer) initialMsg;
    ManagedMessage msg = scopeMsg.copy();
    Context mergedCtxt = getRequiredContextForMessage(scopeMsg);
    if(mergedCtxt==null) {
      // not a context-aware msg flow it would seem, so just return the initial msg
      return initialMsg;
    }
    Context[] branches = new Context[otherMessages.length];
    for (int i = 0; i < otherMessages.length; i++) {
      Context branchedCtx = getRequiredContextForMessage(otherMessages[i]);
      // if no branched context is found, there's nothing to merge... 
      if(branchedCtx!=null) {
        msg.addCauseID(otherMessages[i].getID());
        branches[i] = branchedCtx;
      }
    }
    mergedCtxt.join(branches);
    msg.setBodyContent(mergedCtxt, ManagedMessage.objectContentType);
    return msg;
  }
  
  protected Context getRequiredContextForMessage(ManagedMessage message) {
    if (message == null) {
      return null;
    }
    String[] ctxtHdrs = ((MessageContainer) message).getHeader(ProcessRequest.HEADER_PROCESS_CONTEXT);
    if (ctxtHdrs == null || ctxtHdrs.length == 0) {
      return null;
    }
    ProcessManager processManager = ServiceRegistry.getInstance().getProcessManagerService().getProcessManager(ctxtHdrs[0]);
    return processManager.getRequest().getProcessingContext();
  }
}
