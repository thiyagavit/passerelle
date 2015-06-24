package fr.soleil.passerelle.actor.flow5;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleToken;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.util.ptolemy.PortParameter;

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
 * <p>
 * This variation allows to set configuration data via input ports.
 * </p>
 * 
 * @author erwin
 */
public class ForLoopWithPortCfg extends AbstractSequenceStepper {

  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(ForLoopWithPortCfg.class);

  public static final String STEP_WIDTH_PARAM_NAME = "Step Width";
  public static final String END_VALUE_PARAM_NAME = "End Value";
  public static final String START_VALUE_PARAM_NAME = "Start Value";

  // Parameters
  public PortParameter startValueParam;
  private double startValue;
  public PortParameter endValueParam;
  private double endValue;
  public PortParameter stepWidthParam;
  private double stepWidth;
  // simple flag to maintain the direction of the loop value range
  private boolean up;
  // number of steps in the loop
  private long stepNumber;
  
  // marker for first iteration
  private volatile boolean firstIteration = true;

  /**
   * @param container
   * @param name
   * @throws ptolemy.kernel.util.IllegalActionException
   * @throws ptolemy.kernel.util.NameDuplicationException
   */
  public ForLoopWithPortCfg(final CompositeEntity container, final String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    startValueParam = PortFactory.getInstance().createPortParameter(this, START_VALUE_PARAM_NAME, Integer.class);
    startValueParam.setToken(new IntToken(0));
    endValueParam = PortFactory.getInstance().createPortParameter(this, END_VALUE_PARAM_NAME, Integer.class);
    endValueParam.setToken(new IntToken(3));
    stepWidthParam = PortFactory.getInstance().createPortParameter(this, STEP_WIDTH_PARAM_NAME, Integer.class);
    stepWidthParam.setToken(new IntToken(1));

    final StringAttribute outputPortCardinal = new StringAttribute(outputPort, "_cardinal");
    outputPortCardinal.setExpression("SOUTH");

    final StringAttribute handledPortCardinal = new StringAttribute(nextPort, "_cardinal");
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
  public void attributeChanged(final Attribute arg0) throws IllegalActionException {
    // check the stepWidth param on each change, so we can complain immediately if it has an invalid value
    if (arg0 == stepWidthParam) {
      stepWidth =  getValueFromPortParameter(stepWidthParam, 1L);
      if (stepWidth <= 0) {
        throw new IllegalActionException(stepWidthParam, "Step Width must be positive");
      }
    } else {
      super.attributeChanged(arg0);
    }
  }
  
  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    firstIteration = true;
  }

  /**
   * In this actor impl, we need to obtain the loop config once, from the PortParameters,
   * i.o. from simple parameters as in the plain ForLoop.
   * For this purpose, this utility method is called from within the processing loop.
   * But it should only do it's stuff one time.
   * 
   * @throws ProcessingException
   */
  protected void setLoopConfiguration() throws ProcessingException {
    try {
      startValueParam.setOnce();
      endValueParam.setOnce();
      stepWidthParam.setOnce();
      
      startValue = getValueFromPortParameter(startValueParam, 0L);
      endValue = getValueFromPortParameter(endValueParam, 3L);
      stepWidth = getValueFromPortParameter(stepWidthParam, 1L);
      if (stepWidth <= 0) {
        throw new IllegalActionException(stepWidthParam, "Step Width must be positive");
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error reading loop start/end values", this, e);
    }
    up = (endValue - startValue >= 0);
    final BigDecimal start = new BigDecimal(startValue);
    final BigDecimal end = new BigDecimal(endValue);
    final BigDecimal totalWidth = end.subtract(start, MathContext.DECIMAL32).abs();
    final BigDecimal div = totalWidth.divide(new BigDecimal(stepWidth), MathContext.DECIMAL32);
    stepNumber = (long) Math.floor(div.doubleValue()) + 1;
    getLogger().debug("stepNumber {}", stepNumber);
    clearStepQueue();
  }

  protected double getValueFromPortParameter(PortParameter portParam, double defValue) throws IllegalActionException {
    Token t = portParam.getToken();
    if(t instanceof PasserelleToken) {
      try {
        Object res = ((PasserelleToken)t).getMessage().getBodyContent();
        return Double.parseDouble(res.toString());
      } catch (Exception e) {
        throw new IllegalActionException(this, e, "error reading msg content");
      }
    } else if (t instanceof ScalarToken) {
      return ((ScalarToken) portParam.getToken()).doubleValue();
    }
    return defValue;
  }

  /**
   * React on each received trigger message, by generating a complete loop step sequence again, and store the steps in a queue.
   */
  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    if(firstIteration) {
      setLoopConfiguration();
      firstIteration = false;
    }
    super.process(ctxt, request, response);
  }

  /**
   * Generate all loop steps and queue them, followed by an "end-of-loop-marker".
   * 
   * @param response
   */
  protected void generateSteps(ProcessResponse response) {
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
      addStep(new Step(i, currentValue, response));
      if (up) {
        currentValue += stepWidth;
      } else {
        currentValue -= stepWidth;
      }
    }
    addEndMarkerStep(response);
  }
}
