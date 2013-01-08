package fr.soleil.passerelle.actor.flow5;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * A loop actor implementation based on the Passerelle v5 Actor API.
 * <p>
 * It can be configured with a numerical range and a step size. This loop actor buffers the loop steps and only triggers a next step after getting a
 * confirmation that the previous one has been completely processed by the model's loop.
 * </p>
 * <p>
 * This is done by sending an output "step" msg containing the numerical value for a given step. <br/>
 * The actor sends a first message immediately after receiving a loop "trigger"message. It expects to receive a "handled" msg when the loop has done its
 * iteration for a given step value. Only after receiving such a "handled" msg, the next step's value msg is sent out.
 * </p>
 * 
 * @author erwin
 */
public class ForLoop extends Actor {

  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(ForLoop.class);

  public static final String STEP_WIDTH_PARAM_NAME = "Step Width";
  public static final String END_VALUE_PARAM_NAME = "End Value";
  public static final String START_VALUE_PARAM_NAME = "Start Value";
  public static final String END_LOOP_PORT_NAME = "end loop trigger";
  public static final String OUTPUT_PORT_NAME = "output value";

  // input ports
  public Port triggerPort;
  public Port handledPort;
  // output ports
  public Port outputPort;
  public Port endLoopPort;
  // Parameters
  public Parameter startValueParam;
  double startValue;
  public Parameter endValueParam;
  double endValue;
  public Parameter stepWidthParam;
  double stepWidth;
  // simple flag to maintain the direction of the loop value range
  boolean up;
  // number of steps in the loop
  long stepNumber;

  private BlockingQueue<Step> loopStepQueue = new LinkedBlockingQueue<Step>();

  /**
   * @param container
   * @param name
   * @throws ptolemy.kernel.util.IllegalActionException
   * @throws ptolemy.kernel.util.NameDuplicationException
   */
  public ForLoop(final CompositeEntity container, final String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    triggerPort = PortFactory.getInstance().createInputPort(this, "trigger (start loop)", null);
    triggerPort.setMultiport(false);
    handledPort = PortFactory.getInstance().createInputPort(this, "handled", PortMode.PUSH, null);
    handledPort.setMultiport(false);
    endLoopPort = PortFactory.getInstance().createOutputPort(this, END_LOOP_PORT_NAME);
    outputPort = PortFactory.getInstance().createOutputPort(this, OUTPUT_PORT_NAME);

    startValueParam = new StringParameter(this, START_VALUE_PARAM_NAME);
    startValueParam.setExpression("0");
    endValueParam = new StringParameter(this, END_VALUE_PARAM_NAME);
    endValueParam.setExpression("3");
    stepWidthParam = new StringParameter(this, STEP_WIDTH_PARAM_NAME);
    stepWidthParam.setExpression("1");

    final StringAttribute outputPortCardinal = new StringAttribute(outputPort, "_cardinal");
    outputPortCardinal.setExpression("SOUTH");

    final StringAttribute handledPortCardinal = new StringAttribute(handledPort, "_cardinal");
    handledPortCardinal.setExpression("SOUTH");

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-25\" y=\"-25\" width=\"50\" " + "height=\"50\" style=\"fill:pink;stroke:pink\"/>\n"
        + "<line x1=\"-24\" y1=\"-24\" x2=\"24\" y2=\"-24\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-24\" y1=\"-24\" x2=\"-24\" y2=\"24\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"25\" y1=\"-24\" x2=\"25\" y2=\"25\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-24\" y1=\"25\" x2=\"25\" y2=\"25\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"24\" y1=\"-23\" x2=\"24\" y2=\"24\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-23\" y1=\"24\" x2=\"24\" y2=\"24\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<circle cx=\"0\" cy=\"0\" r=\"10\""
        + "style=\"fill:white;stroke-width:2.0\"/>\n" + "<line x1=\"10\" y1=\"0\" x2=\"7\" y2=\"-3\" " + "style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"10\" y1=\"0\" x2=\"13\" y2=\"-3\" " + "style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    try {
      startValue = Double.valueOf(((StringToken) startValueParam.getToken()).stringValue());
      endValue = Double.valueOf(((StringToken) endValueParam.getToken()).stringValue());
    } catch (Exception e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error reading loop start/end values", this, e);
    }
    up = (endValue - startValue >= 0);
    final BigDecimal start = new BigDecimal(startValue);
    final BigDecimal end = new BigDecimal(endValue);
    final BigDecimal totalWidth = end.subtract(start, MathContext.DECIMAL32).abs();
    final BigDecimal div = totalWidth.divide(new BigDecimal(stepWidth), MathContext.DECIMAL32);
    stepNumber = (long) Math.floor(div.doubleValue()) + 1;
    getLogger().debug("stepNumber {}", stepNumber);
    loopStepQueue.clear();
  }

  @Override
  public void attributeChanged(final Attribute arg0) throws IllegalActionException {
    // check the stepWidth param on each change, so we can complain immediately if it has an invalid value
    if (arg0 == stepWidthParam) {
      stepWidth = Double.valueOf(((StringToken) stepWidthParam.getToken()).stringValue());
      if (stepWidth <= 0) {
        throw new IllegalActionException(stepWidthParam, "Step Width must be positive");
      }
    } else {
      super.attributeChanged(arg0);
    }
  }

  /**
   * Intercept each "handled" message while it's being pushed via the handledPort, so we can use this as indication that a next loop msg can be sent out.
   */
  @Override
  public void offer(MessageInputContext ctxt) throws PasserelleException {
    if (handledPort.getName().equals(ctxt.getPortName())) {
      sendLoopData();
    }
    super.offer(ctxt);
  }

  /**
   * React on each received trigger message, by generating a complete loop step sequence again, and store the steps in a queue.
   */
  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage inputMsg = request.getMessage(triggerPort);
    if (inputMsg != null) {
      getLogger().debug("{} - received trigger msg {}", getFullName(), inputMsg);
      boolean wasIdle = loopStepQueue.isEmpty();
      generateSteps(response);
      if (wasIdle) {
        // send out first msg of the loop
        sendLoopData();
      }
    }
  }

  /**
   * As the loop actor manages a complete loop execution "in the background" for each received trigger msg, i.e. we do not block the process method during the
   * complete loop execution, this must be marked as "asynchronous" processing.
   */
  @Override
  protected ProcessingMode getProcessingMode(ActorContext ctxt, ProcessRequest request) {
    if (request.getMessage(triggerPort) != null) {
      return ProcessingMode.ASYNCHRONOUS;
    } else {
      return super.getProcessingMode(ctxt, request);
    }
  }

  /**
   * Generate all loop steps and queue them, followed by an "end-of-loop-marker".
   * 
   * @param response
   */
  private void generateSteps(ProcessResponse response) {
    Double currentValue = startValue;
    for (long i = 0; i < stepNumber; ++i) {
      // use formatter to remove the "problem" of precision
      // with java operations.
      final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      dfs.setDecimalSeparator('.');
      final DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
      df.setGroupingUsed(false);
      df.setDecimalFormatSymbols(dfs);
      currentValue = Double.parseDouble(df.format(currentValue));
      loopStepQueue.offer(new Step(i, currentValue, response));
      if (up) {
        currentValue += stepWidth;
      } else {
        currentValue -= stepWidth;
      }
    }
    loopStepQueue.offer(Step.buildEndMarker(response));
  }

  /**
   * @param inputMsg
   * @throws ProcessingException
   */
  private void sendLoopData() throws ProcessingException {
    Step step = loopStepQueue.poll();
    if (step != null) {
      if (step.isEndMarker()) {
        // output end loop signal
        ProcessResponse response = step.response;
        processFinished(response.getContext(), response.getRequest(), response);
        ExecutionTracerService.trace(this, "All steps done");
        sendOutputMsg(endLoopPort, MessageFactory.getInstance().createTriggerMessage());
        if (loopStepQueue.isEmpty()) {
          if (!isFinishRequested() && triggerPort.getActiveSources().isEmpty()) {
            requestFinish();
          }
        } else {
          // continue with next loop that seems to be on the queue
          sendLoopData();
        }
      } else if (isFinishRequested()) {
        ExecutionTracerService.trace(this, "Loop has been interrupted at step number " + step.stepCtr);
        if (getDirectorAdapter().isActorBusy(this)) {
          while (!loopStepQueue.isEmpty()) {
            // clear all busy work
            ProcessResponse response = step.response;
            if (response != null)
              processFinished(response.getContext(), response.getRequest(), response);
          }
        }
      } else {
        final ManagedMessage resultMsg = createMessage();
        getLogger().trace("{} - sendLoopData() - iteration {}", getFullName(), step.stepCtr);
        try {
          resultMsg.setBodyContent(step.stepValue, ManagedMessage.objectContentType);
          sendOutputMsg(outputPort, resultMsg);
          ExecutionTracerService.trace(this, "Loop with value: " + step.stepValue);
        } catch (final MessageException e) {
          throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Cannot send message out", this, e);
        }
      }
    }
  }

  /**
   * Simple container structure to maintain all relevant info for each loop step that must be executed.
   */
  static class Step {
    private boolean endMarker;
    long stepCtr;
    double stepValue;
    ProcessResponse response;

    public Step(long stepCtr, double stepValue, ProcessResponse response) {
      this.stepCtr = stepCtr;
      this.stepValue = stepValue;
      this.response = response;
    }

    private Step() {
    }

    public boolean isEndMarker() {
      return endMarker;
    }

    public static Step buildEndMarker(ProcessResponse response) {
      Step s = new Step();
      s.endMarker = true;
      s.response = response;
      return s;
    }
  }
}
