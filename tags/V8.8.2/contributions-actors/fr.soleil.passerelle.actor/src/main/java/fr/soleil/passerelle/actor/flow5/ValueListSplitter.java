package fr.soleil.passerelle.actor.flow5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleToken;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.util.ptolemy.PortParameter;

public class ValueListSplitter extends AbstractSequenceStepper {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(ValueListSplitter.class);
  private static final String VALUELIST_PARAM_NAME = "Value List";

  public PortParameter valueListParam;
  private String[] values;

  // marker for first iteration
  private volatile boolean firstIteration = true;

  public ValueListSplitter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    valueListParam = PortFactory.getInstance().createPortParameter(this, VALUELIST_PARAM_NAME, String.class);
    valueListParam.setToken(new StringToken(""));
  }
  
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    firstIteration = true;
  }

  protected void setSequenceConfiguration() throws ProcessingException {
    try {
      valueListParam.setOnce();
      String valueList = getValueFromPortParameter(valueListParam);
      values = valueList.split(",");
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error reading value list", this, e);
    }
    clearStepQueue();
  }
  
  protected String getValueFromPortParameter(PortParameter portParam) throws IllegalActionException {
    Token t = portParam.getToken();
    if(t instanceof PasserelleToken) {
      try {
        Object res = ((PasserelleToken)t).getMessage().getBodyContent();
        return res.toString();
      } catch (Exception e) {
        throw new IllegalActionException(this, e, "error reading msg content");
      }
    } else {
      return portParam.getExpression();
    }
  }

  @Override
  protected void generateSteps(ProcessResponse response) {
    for(int i=0; i<values.length; ++i) {
      addStep(new Step(i, values[i], response));
    }
    addEndMarkerStep(response);
  }

  /**
   * React on each received trigger message, by generating a complete loop step sequence again, and store the steps in a queue.
   */
  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    if(firstIteration) {
      setSequenceConfiguration();
      firstIteration = false;
    }
    super.process(ctxt, request, response);
  }
}
