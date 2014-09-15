/**
 * 
 */
package com.isencia.passerelle.process.actor.flow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.dynaport.OutputPortConfigurationExtender;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.service.ServiceRegistry;

/**
 * <p>
 * An actor with configurable named output ports, that sends out copies of the
 * incoming Context on each outgoing port. The original Context is sent on the
 * default output port.
 * </p>
 * <p>
 * This is useful to ensure that parallel branches in a sequence don't see each
 * others intermediate results.
 * </p>
 * <p>
 * The <code>Fork</code> is typically used with the <code>Join</code>, to merge
 * the results of the parallel branches into one Context again.
 * </p>
 * 
 * @author erwin
 * 
 */
public class Fork extends AbstractMessageSequenceGenerator {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(Fork.class);

  public Port input;
  public OutputPortConfigurationExtender outputPortCfgExt;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Fork(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name, true);
    input = PortFactory.getInstance().createInputPort(this, null);
    outputPortCfgExt = new OutputPortConfigurationExtender(this, "output port configurer");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void process(Context taskContext) throws ProcessingException {
    
  }
  
  @Override
  protected void postProcess(ManagedMessage message, Context taskContext, com.isencia.passerelle.process.actor.ProcessResponse response) throws Exception {
    
    if (message != null) {
      try {
        Context processContext = (Context) message.getBodyContent();
        Long scopeId = processContext.getRequest().getId();
        registerSequenceScopeMessage(scopeId, message);

        try {
          getAuditLogger().info("Forking msg with scope " + scopeId + " : " + getAuditTrailMessage(message, input));
        } catch (Exception e) {
          getLogger().error("Error logging audit trail", e);
        }

        List<Port> outputPorts = outputPortCfgExt.getOutputPorts();
        for (int i = 0; i < outputPorts.size(); ++i) {
          Context newOne = processContext.fork();
          MessageContainer outputMsg = (MessageContainer) MessageFactory.getInstance().createMessageCloneInSequence(
              message,
              processContext.getRequest().getId(),  // sequence ID
              new Long(i),                          // sequence position
              (i == (outputPorts.size() - 1)));     // end of sequence?
          // enforce single Fork name
          outputMsg.setHeader(HEADER_SEQ_SRC, getName());
          outputMsg.setBodyContent(newOne, ManagedMessage.objectContentType);
          response.addOutputMessage(outputPorts.get(i), outputMsg);
        }
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error generating forked messages", this, message, e);
      }
    }
    
    super.postProcess(message, taskContext, response);
    
    ServiceRegistry.getInstance().getContextManager().notifyFinished(taskContext);
  }
}