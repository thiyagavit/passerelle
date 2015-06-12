/*
 * Synchrotron Soleil
 * 
 * File : ForLoop.java
 * 
 * Project : passerelle-soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 08 janvier 2007
 * 
 * Revision: Author: Date: State:
 * 
 * Log: ForLoop.java,v
 */
/*
 * Created on 08 janvier 2007
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.flow;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.NoRoomException;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListener;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * @author ABEILLE
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ForLoop extends Actor {

    public static final String STEP_WIDTH_PARAM_NAME = "Step Width";
    public static final String END_VALUE_PARAM_NAME = "End Value";
    public static final String START_VALUE_PARAM_NAME = "Start Value";

    public static final String END_LOOP_PORT_NAME = "end loop trigger";
    public static final String OUTPUT_PORT_NAME = "output value";

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(ForLoop.class);

    // input ports
    public Port triggerPort;
    public Port handledPort;
    private PortHandler handledPortHandler;

    private boolean triggerPortExhausted = false;
    private boolean handledPortExhausted = false;

    // output ports
    public Port outputPort;
    public Port endLoopPort;

    // Parameter
    public Parameter startValueParam;
    double startValue;

    public Parameter endValueParam;
    double endValue;

    public Parameter stepWidthParam;
    double stepWidth;

    public Parameter rotatePorts;
    // Parameter upParam;
    boolean up;

    long stepNumber;
    double currentValue = 0;

    protected boolean handleReceived;

    protected boolean tokenIsNull;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.IllegalActionException
     * @throws ptolemy.kernel.util.NameDuplicationException
     */
    public ForLoop(final CompositeEntity container, final String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        triggerPort = PortFactory.getInstance().createInputPort(this, "trigger (start loop)", null);
        triggerPort.setMultiport(false);
        handledPort = PortFactory.getInstance().createInputPort(this, "handled", null);
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

        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-25\" y=\"-25\" width=\"50\" "
                + "height=\"50\" style=\"fill:pink;stroke:pink\"/>\n"
                + "<line x1=\"-24\" y1=\"-24\" x2=\"24\" y2=\"-24\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-24\" y1=\"-24\" x2=\"-24\" y2=\"24\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"25\" y1=\"-24\" x2=\"25\" y2=\"25\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-24\" y1=\"25\" x2=\"25\" y2=\"25\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"24\" y1=\"-23\" x2=\"24\" y2=\"24\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-23\" y1=\"24\" x2=\"24\" y2=\"24\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n" +

                "<circle cx=\"0\" cy=\"0\" r=\"10\"" + "style=\"fill:white;stroke-width:2.0\"/>\n" +

                "<line x1=\"10\" y1=\"0\" x2=\"7\" y2=\"-3\" " + "style=\"stroke-width:2.0\"/>\n"
                + "<line x1=\"10\" y1=\"0\" x2=\"13\" y2=\"-3\" "
                + "style=\"stroke-width:2.0\"/>\n" +

                "</svg>\n");

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.isencia.passerelle.actor.Actor#doInitialize()
     */
    @Override
    protected void doInitialize() throws InitializationException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - doInitialize() - entry");
        }

        handleReceived = false;
        // If something connected to the set port, install a handler
        if (handledPort.getWidth() > 0) {
            // System.out.println("left.getWidth() > 0");
            handledPortHandler = new PortHandler(handledPort, new PortListener() {
                @Override
                public void tokenReceived() {
                    // System.out.println("leftHandler.tokenReceived() ");
                    final Token token = handledPortHandler.getToken();
                    if (token != null && token != Token.NIL) {
                        handleReceived = true;
                        // System.out.println("left received");

                    }
                    performNotify();
                }

                @Override
                public void noMoreTokens() {
                    handleReceived = true;
                    requestFinish();
                    performNotify();
                }
            });
            if (handledPortHandler != null) {
                handledPortHandler.start();
            }
        }
        super.doInitialize();
        triggerPortExhausted = !(triggerPort.getWidth() > 0);
        handledPortExhausted = !(handledPort.getWidth() > 0);

        up = false;
        if (endValue - startValue >= 0) {
            up = true;
        }

        currentValue = startValue;

        final BigDecimal start = new BigDecimal(startValue);
        final BigDecimal end = new BigDecimal(endValue);
        final BigDecimal totalWidth = end.subtract(start, MathContext.DECIMAL32).abs();
        final BigDecimal div = totalWidth.divide(new BigDecimal(stepWidth), MathContext.DECIMAL32);
        stepNumber = (long) Math.floor(div.doubleValue()) + 1;
        logger.debug("stepNumber " + stepNumber);
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - doInitialize() - exit");
        }
    }

    private synchronized void performNotify() {
        notify();
    }

    private synchronized void performWait(final int time) {
        try {
            if (time == -1) {
                wait();
            } else {
                wait(time);
            }
        } catch (final InterruptedException e) {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute
     * )
     */
    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == startValueParam) {
            startValue = Double.valueOf(((StringToken) startValueParam.getToken()).stringValue());
        } else if (arg0 == stepWidthParam) {
            stepWidth = Double.valueOf(((StringToken) stepWidthParam.getToken()).stringValue());
            if (stepWidth <= 0) {
                throw new IllegalActionException(stepWidthParam, "Step Width must be positive");
            }
        } else if (arg0 == endValueParam) {
            endValue = Double.valueOf(((StringToken) endValueParam.getToken()).stringValue());
            // else if (arg0 == upParam)
            // up = Boolean.valueOf(upParam.getExpression());
        } else {
            super.attributeChanged(arg0);
        }

    }

    @Override
    protected void doFire() throws ProcessingException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - doFire() - entry");
        }

        ManagedMessage inputMsg = null;
        int currentStep = 0;
        if (!triggerPortExhausted) {
            try {
                inputMsg = MessageHelper.getMessage(triggerPort);
                if (inputMsg != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(getName() + " doFire() - received msg on port "
                                + triggerPort.getName() + " msg :" + inputMsg);
                    }
                } else {
                    triggerPortExhausted = true;
                    if (logger.isDebugEnabled()) {
                        logger.debug(getName() + " doFire() - found exhausted port "
                                + triggerPort.getName());
                    }
                }
            } catch (final PasserelleException e) {
                ExceptionUtil.throwProcessingException("Error reading from port", triggerPort, e);
            }
        }

        if (inputMsg != null) {
            // send out first msg of the loop
            sendLoopData();
            currentStep++;
            if (logger.isTraceEnabled()) {
                logger.trace(getName() + " - doFire() - iteration " + currentStep);
            }
            // and now do the loop, each time after receiving a loop iteration
            // handled notification.
            // Loop step+1 times to send a message on the output hasFinished at
            // the end of the last loop
            while (!handledPortExhausted && currentStep < stepNumber + 1 && !isFinishRequested()) {
                try {
                    // final ManagedMessage handledMsg = null;

                    while (!handleReceived && !isFinishRequested()) {
                        performWait(100);
                    }
                    handleReceived = false;
                    if (isFinishRequested()) {
                        break;
                    }
                    // send output message only for the number of loops asked.
                    if (currentStep < stepNumber) {
                        sendLoopData();
                    }
                    currentStep++;
                    if (logger.isTraceEnabled()) {
                        logger.trace(getName() + " - doFire() - iteration " + currentStep);
                    }
                } catch (final PasserelleException e) {
                    ExceptionUtil.throwProcessingException("Error on loop", this, e);
                }
            } // while

            if (currentStep >= stepNumber) {
                // output end loop signal
                // if(currentValue == endValue)
                currentValue = startValue;
                ExecutionTracerService.trace(this, "All loops done");
                sendOutputMsg(endLoopPort, PasserelleUtil.createTriggerMessage());
            } else if (isFinishRequested()) {
                ExecutionTracerService.trace(this, "Loop has been interrupted at step number "
                        + currentStep);
            }

            if (currentStep == stepNumber) {
                currentStep = 0;
            }
        }

        if (triggerPortExhausted) {
            requestFinish();
        }

        if (triggerPortExhausted && handledPortExhausted) {
            if (logger.isTraceEnabled()) {
                logger.trace(getName() + " - doFire() - exit");
            }
        }

    }

    /**
     * @param inputMsg
     * @throws ProcessingException
     */
    private void sendLoopData() throws ProcessingException {
        final ManagedMessage resultMsg = createMessage();

        try {
            resultMsg.setBodyContent(new Double(currentValue), ManagedMessage.objectContentType);
            ExecutionTracerService.trace(this, "Loop with value: " + currentValue);
        } catch (final MessageException e) {
            ExceptionUtil.throwProcessingException("Cannot send message out", this, e);
        }
        if (up) {
            // use formater to remove the "problem" of precision
            // with java operations.
            final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            final DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
            df.setGroupingUsed(false);
            df.setDecimalFormatSymbols(dfs);
            currentValue += stepWidth;
            currentValue = Double.parseDouble(df.format(currentValue));
        } else {
            currentValue = currentValue - stepWidth;
        }
        try {
            sendOutputMsg(outputPort, resultMsg);
        } catch (final NoRoomException e) {
            ExceptionUtil.throwProcessingException("No room exception", this, e);
        }
    }

    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }
}
