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

import java.util.ArrayList;
import java.util.List;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.process.actor.ProcessRequest;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.service.ContextRepository;
import com.isencia.passerelle.process.model.service.ServiceRegistry;

/**
 * A <code>AggregationStrategy</code> that looks for processing <code>Context</code>s in the sequenced messages,
 * and aggregates their tasks and results.
 * 
 * @author erwin
 *
 */
public class ContextAggregationStrategy implements AggregationStrategy {

  private ContextRepository contextRepository;

  public ContextAggregationStrategy(ContextRepository contextRepository) {
    this.contextRepository = contextRepository;
  }

  public ManagedMessage aggregateMessages(ManagedMessage initialMsg, ManagedMessage... otherMessages) throws MessageException {
    MessageContainer scopeMsg = (MessageContainer) initialMsg;
    ManagedMessage msg = scopeMsg.copy();
    Context mergedCtxt = getRequiredContextForMessage(scopeMsg);
    if(mergedCtxt==null) {
      // not a context-aware msg flow it would seem, so just return the initial msg
      return initialMsg;
    }
    List<Context> branches = new ArrayList<Context>();
    for (ManagedMessage branchMsg : otherMessages) {
      Context branchedCtx = getRequiredContextForMessage(branchMsg);
      // if no branched context is found, there's nothing to merge... 
      if(branchedCtx!=null) {
        msg.addCauseID(branchMsg.getID());
        branches.add(branchedCtx);
      }
    }
    mergedCtxt = ServiceRegistry.getInstance().getEntityManager().mergeWithBranchedContexts(mergedCtxt, branches);
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
    return contextRepository.getContext(ctxtHdrs[0]);
  }
}
