/**
 * 
 */
package com.isencia.passerelle.process.actor;

import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.message.internal.SettableMessage;
import com.isencia.passerelle.process.model.Context;

/**
 * 
 * @author erwin
 *
 */
public class ProcessRequest extends com.isencia.passerelle.actor.v5.ProcessRequest {
  
  public static final String HEADER_PROCESS_CONTEXT = "__PSRL_PROC_CTXT_ID";
  
  private Context processContext;

  /**
   * 
   */
  public ProcessRequest(Context processContext) {
    this.processContext = processContext;
  }
  
  public Context getProcessContext() {
    return processContext;
  }
  
  /**
   * For this context-aware <code>ProcessRequest</code>, the msg is only accepted if it relates to the same process <code>Context</code>.
   */
  @Override
  public void addInputContext(MessageInputContext msgCtxt) {
    // TODO Auto-generated method stub
    super.addInputContext(msgCtxt);
  }
  
  /**
   * For this context-aware <code>ProcessRequest</code>, the msg is only accepted if it relates to the same process <code>Context</code>.
   */
  @Override
  public void addInputMessage(int inputIndex, String inputName, ManagedMessage inputMsg) {
    String[] ctxtIDs = ((SettableMessage)inputMsg).getHeader(HEADER_PROCESS_CONTEXT);
    boolean itsOk= (processContext==null) || (ctxtIDs.length==0);
    if(!itsOk) {
      for (String id : ctxtIDs) {
        
      }
    }
    super.addInputMessage(inputIndex, inputName, inputMsg);
  }

}
