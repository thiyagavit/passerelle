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
package com.isencia.passerelle.process.actor.flow;

import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * A <code>AggregationStrategy</code> that looks for processing <code>Context</code>s in the sequenced messages, and
 * aggregates their tasks and results.
 * 
 * @author erwin
 * 
 */
public class ContextAggregationStrategy implements AggregationStrategy {

  public ManagedMessage aggregateMessages(ProcessManager processManager, ManagedMessage initialMsg, ManagedMessage... otherMessages) throws MessageException {
    MessageContainer scopeMsg = (MessageContainer) initialMsg;
    ManagedMessage msg = scopeMsg.copy();
    Context mergedCtxt = processManager.getRequest().getProcessingContext();
    Context[] branches = new Context[otherMessages.length];
    for (int i = 0; i < otherMessages.length; i++) {
      msg.addCauseID(otherMessages[i].getID());
      Object bodyContent = otherMessages[i].getBodyContent();
      if (bodyContent instanceof Context) {
        Context branchedCtx = (Context) bodyContent;
        branches[i] = branchedCtx;
      }else{
        branches[i] = mergedCtxt;
      }
    }
    mergedCtxt.join(branches);
    return msg;
  }
}
