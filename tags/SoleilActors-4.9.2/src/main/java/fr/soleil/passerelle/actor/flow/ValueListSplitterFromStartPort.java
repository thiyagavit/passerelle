package fr.soleil.passerelle.actor.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;

public class ValueListSplitterFromStartPort extends AbstractSequenceStepper {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(ValueListSplitterFromStartPort.class);
  private static final String VALUELIST_PARAM_NAME = "Value List";
  private static final String USEMSGVALUES_PARAM_NAME = "Use values from message";

  private String[] values;
  private boolean useMsgValues;
  public StringParameter valueListParam;
  public Parameter useMsgValuesParam;

  // marker for first iteration
  private volatile boolean firstIteration = true;

  public ValueListSplitterFromStartPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    startPort.setExpectedMessageContentType(String.class);
    valueListParam = new StringParameter(this, VALUELIST_PARAM_NAME);
    useMsgValuesParam = new Parameter(this, USEMSGVALUES_PARAM_NAME, BooleanToken.TRUE);
    new CheckBoxStyle(useMsgValuesParam, "checkbox");
  }

  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    firstIteration = true;
    values = null;
    try {
      useMsgValues = ((BooleanToken) useMsgValuesParam.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error reading value for useMsgValues", this, e);
    }
  }

  protected void setSequenceConfiguration(ManagedMessage managedMessage) throws ProcessingException {
    try {
      if (useMsgValues && managedMessage != null) {
        String valueList = managedMessage.getBodyContentAsString();
        if (valueList.length() > 0) {
          values = valueList.split(",");
        }
      }
      if (values == null || values.length == 0) {
        values = ((StringToken) valueListParam.getToken()).stringValue().split(",");
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error reading value list", this, e);
    }
    clearStepQueue();
  }

  @Override
  protected void generateSteps(ProcessResponse response) {
    for (int i = 0; i < values.length; ++i) {
      addStep(new Step(i, values[i], response));
    }
    addEndMarkerStep(response);
  }

  /**
   * React on each received trigger message, by generating a complete loop step sequence again, and store the steps in a queue.
   */
  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    if (firstIteration) {
      setSequenceConfiguration(request.getMessage(startPort));
      firstIteration = false;
    }
    super.process(ctxt, request, response);
  }
}
