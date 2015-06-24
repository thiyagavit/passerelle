/*
 * Synchrotron Soleil
 * 
 * File : SimpleLoop.java
 * 
 * Project : soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 8 janv. 07
 * 
 * Revision: Author: Date: State:
 * 
 * Log: SimpleLoop.java,v
 */
/*
 * Created on 8 janv. 07
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.NoRoomException;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
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
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.util.ExceptionUtil;

@SuppressWarnings("serial")
public class SimpleLoop extends Actor {

    public static final String END_LOOP_PORT_NAME = "end loop trigger";
    public static final String OUTPUT_PORT_NAME = "output value";

    private final static Logger logger = LoggerFactory.getLogger(SimpleLoop.class);

    // input ports
    public Port triggerPort;
    public Port handledPort;

    private boolean triggerPortExhausted = false;
    private boolean handledPortExhausted = false;

    // output ports
    public Port outputPort;
    public Port endLoopPort;

    // Parameter
    public Parameter useValuesListParam;
    Boolean useValuesList;

    public Parameter valuesListParam;
    Double[] valuesList;

    public Parameter loopNumberParam;
    Integer loopNumber;

    // double currentValue = 0;
    int currentIndex = 0;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.IllegalActionException
     * @throws ptolemy.kernel.util.NameDuplicationException
     */
    public SimpleLoop(final CompositeEntity container, final String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        triggerPort = PortFactory.getInstance().createInputPort(this, "trigger (start loop)", null);
        triggerPort.setMultiport(false);
        handledPort = PortFactory.getInstance().createInputPort(this, "handled", null);
        handledPort.setMultiport(false);
        endLoopPort = PortFactory.getInstance().createOutputPort(this, END_LOOP_PORT_NAME);
        outputPort = PortFactory.getInstance().createOutputPort(this, OUTPUT_PORT_NAME);

        loopNumberParam = new StringParameter(this, "Number of Loops");
        loopNumberParam.setExpression("2");

        useValuesListParam = new Parameter(this, "Use Values List", new BooleanToken(false));
        useValuesListParam.setTypeEquals(BaseType.BOOLEAN);

        valuesListParam = new StringParameter(this, "Values List (separated by commas)");
        valuesListParam.setExpression("1,3,5,10");

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

        triggerPortExhausted = !(triggerPort.getWidth() > 0);

        handledPortExhausted = !(handledPort.getWidth() > 0);
        currentIndex = 0;
        // currentValue = valuesList[currentIndex];
        // if (useValuesList)
        // loopNumber = valuesList.length;
        super.doInitialize();
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - doInitialize() - exit");
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
        if (arg0 == loopNumberParam) {
            loopNumber = Integer.valueOf(((StringToken) loopNumberParam.getToken()).stringValue());
        } else if (arg0 == useValuesListParam) {
            useValuesList = Boolean.valueOf(useValuesListParam.getExpression());
        } else if (arg0 == valuesListParam) {
            final String[] table = ((StringToken) valuesListParam.getToken()).stringValue().split(
                    ",");
            valuesList = new Double[table.length];
            for (int i = 0; i < table.length; i++) {
                valuesList[i] = new Double(table[i]);
            }
            currentIndex = 0;
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
            while (!handledPortExhausted && currentStep < loopNumber + 1) {
                try {
                    ManagedMessage handledMsg = null;

                    if (!handledPortExhausted) {
                        handledMsg = MessageHelper.getMessage(handledPort);
                        if (handledMsg != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(getName() + " doFire() - received msg on port "
                                        + handledPort.getName());
                            }

                        } else {
                            handledPortExhausted = true;
                            if (logger.isDebugEnabled()) {
                                logger.debug(getName() + " doFire() - found exhausted port "
                                        + handledPort.getName());
                            }
                        }
                    }
                    // send output message only for the number of loops asked.
                    if (currentStep < loopNumber.intValue()) {
                        sendLoopData();
                    } else {
                        // output end loop signal
                        currentIndex = 0;
                        final ManagedMessage resultMsg = MessageFactory.getInstance()
                                .createTriggerMessage();
                        sendOutputMsg(endLoopPort, resultMsg);
                        ExecutionTracerService.trace(this, "All loops done");
                    }
                    currentStep++;
                    if (logger.isTraceEnabled()) {
                        logger.trace(getName() + " - doFire() - iteration " + currentStep);
                    }
                } catch (final PasserelleException e) {
                    ExceptionUtil.throwProcessingException("Error on loop", this, e);
                } catch (final NoRoomException e) {
                    ExceptionUtil.throwProcessingException("Error on loop", this, e);
                }
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
        ManagedMessage resultMsg = createMessage();
        if (useValuesList) {
            try {
                resultMsg
                        .setBodyContent(valuesList[currentIndex], ManagedMessage.objectContentType);
                ExecutionTracerService.trace(this, "Loop with value: " + valuesList[currentIndex]);
            } catch (final MessageException e) {
                ExceptionUtil.throwProcessingException("Cannot send message out", this, e);
            }
            if (currentIndex == valuesList.length - 1) {
                currentIndex = 0;
            } else {
                currentIndex++;
            }
        } else {
            resultMsg = MessageFactory.getInstance().createTriggerMessage();
            ExecutionTracerService.trace(this, "Loop number " + (currentIndex + 1));
            currentIndex++;
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
