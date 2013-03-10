package com.isencia.passerelle.process.actor.forkjoin;

import java.util.ArrayList;
import java.util.List;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.service.ServiceRegistry;

/**
 * A <code>JoinStrategy</code> that looks for processing <code>Context</code>s in the sequenced messages,
 * and merges their tasks and results.
 * 
 * @author erwin
 *
 */
public class ContextJoinStrategy implements JoinStrategy {

  public ManagedMessage joinMessages(ManagedMessage initialMsg, ManagedMessage... otherMessages) throws MessageException {
    MessageContainer scopeMsg = (MessageContainer) initialMsg;
    ManagedMessage msg = scopeMsg.copy();
    Context mergedCtxt = (Context) scopeMsg.getBodyContent();
    List<Context> branches = new ArrayList<Context>();
    for (ManagedMessage branchMsg : otherMessages) {
      Context branchedCtx = (Context)branchMsg.getBodyContent();
      msg.addCauseID(branchMsg.getID());
      branches.add(branchedCtx);
    }
    mergedCtxt = ServiceRegistry.getInstance().getEntityManager().mergeWithBranchedContexts(mergedCtxt, branches);
    msg.setBodyContent(mergedCtxt, ManagedMessage.objectContentType);
    return msg;
  }
}
