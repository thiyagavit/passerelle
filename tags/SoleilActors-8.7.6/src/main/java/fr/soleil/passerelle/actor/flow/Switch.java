/*
 * (c) Copyright 2002, Tuple NV Belgium All Rights Reserved.
 * 
 * This software is the proprietary information of Tuple NV, Belgium. Use is
 * subject to license terms.
 */

package fr.soleil.passerelle.actor.flow;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListenerAdapter;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.actor.PortUtilities;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * DOCUMENT ME!
 * 
 * @version $Id: Switch.java,v 1.12 2011/03/15 10:45:22 daniel Exp $
 * @author Dirk Jacobs
 */
@SuppressWarnings("serial")
public class Switch extends Actor {
    private static final String OUTPUT = "output ";

    private static Logger logger = LoggerFactory.getLogger(Switch.class);

    private List<Port> outputPorts = null;
    private PortHandler selectHandler = null;

    public Parameter numberOfOutputs = null;
    public Port input;
    public Port select = null;

    private int outputCount = 0;
    private int selected = 0;
    private boolean selectedReceived = false;
    private boolean tokenIsNull = false;

    public Switch(final CompositeEntity container, final String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        numberOfOutputs = new Parameter(this, "count", new IntToken(1));
        numberOfOutputs.setTypeEquals(BaseType.INT);

        input = PortFactory.getInstance().createInputPort(this, null);
        input.setMultiport(false);
        select = PortFactory.getInstance().createInputPort(this, "select", String.class);
        input.setMultiport(false);

        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<circle cx=\"-2\" cy=\"-7\" r=\"4\"" + "style=\"fill:black\"/>\n"
                + "<line x1=\"-15\" y1=\"-5\" x2=\"15\" y2=\"-5\" "
                + "style=\"stroke-width:2.0\"/>\n"
                + "<line x1=\"0\" y1=\"-5\" x2=\"15\" y2=\"-15\" "
                + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"0\" y1=\"-5\" x2=\"15\" y2=\"5\" "
                + "style=\"stroke-width:2.0\"/>\n"
                + "<line x1=\"-15\" y1=\"10\" x2=\"0\" y2=\"10\" "
                + "style=\"stroke-width:1.0;stroke:gray\"/>\n"
                + "<line x1=\"0\" y1=\"10\" x2=\"0\" y2=\"-5\" "
                + "style=\"stroke-width:1.0;stroke:gray\"/>\n" + "</svg>\n");
    }

    /**
     * DOCUMENT ME!
     * 
     * @param attribute
     *            DOCUMENT ME!
     * 
     * @throws IllegalActionException
     *             DOCUMENT ME!
     */
    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " :" + attribute);
        }

        if (attribute == numberOfOutputs) {
            final int newOutputCount = ((IntToken) numberOfOutputs.getToken()).intValue();

            if (newOutputCount <= 0) {
                throw new IllegalActionException("Number of output can not be less than 1");
            }
            logger.debug("change number of outputs from :  " + outputCount + " to : "
                    + newOutputCount);

            if (outputPorts == null) {
                logger.debug("Create a new list");
                outputPorts = new ArrayList<Port>(5);

                for (int i = 0; i < newOutputCount; i++) {
                    try {
                        Port outputPort = (Port) getPort(OUTPUT + i);

                        if (outputPort == null) {
                            outputPort = PortFactory.getInstance().createOutputPort(this,
                                    OUTPUT + i);
                        }

                        outputPorts.add(i, outputPort);
                        logger.debug("created output : " + i);
                    } catch (final NameDuplicationException e) {
                        throw new IllegalActionException(e.toString());
                    }
                }
            } else if (newOutputCount < outputCount) {
                logger.debug("Decrement number of outputs");

                for (int i = outputCount - 1; i >= 0 && i >= newOutputCount; i--) {
                    try {
                        outputPorts.get(i).setContainer(null);
                        outputPorts.remove(i);
                        logger.debug("removed output : " + i);
                    } catch (final NameDuplicationException e) {
                        throw new IllegalActionException(e.toString());
                    }
                }
            } else if (newOutputCount > outputCount) {
                logger.debug("Increment number of outputs");

                for (int i = outputCount; i < newOutputCount; i++) {
                    try {
                        Port outputPort = (Port) getPort(OUTPUT + i);

                        if (outputPort == null) {
                            outputPort = PortFactory.getInstance().createOutputPort(this,
                                    OUTPUT + i);
                        }

                        outputPorts.add(i, outputPort);
                        logger.debug("created output : " + i);
                    } catch (final NameDuplicationException e) {
                        throw new IllegalActionException(e.toString());
                    }
                }
            }

            outputCount = newOutputCount;

            if (selected >= outputCount) {
                selected = outputCount - 1;
            }
        } else {
            super.attributeChanged(attribute);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - exit ");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.isencia.passerelle.actor.Actor#doFire()
     */
    @Override
    protected void doFire() throws ProcessingException {
        int outNr = 0;
        System.out.println(getName());
        if (logger.isTraceEnabled()) {
            logger.trace(getName());
        }
        Token token = null;

        // If the actor is put alone in the sequence ==> Nothing to do
        if (select.getWidth() == 0 && input.getWidth() == 0) {
            requestFinish();
        } else {
            while (!selectedReceived) {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    // not important
                }
            }
            logger.debug("Switch selectedReceived " + tokenIsNull);
            selectedReceived = false;

            try {
                token = MessageHelper.getMessageAsToken(input);
            } catch (final PasserelleException e) {
                requestFinish();
                ExceptionUtil.throwProcessingException(getName()
                        + " - doFire() generated exception in MessageHelper.getMessageAsToken() "
                        + e, token, e);
            }

            if (token == null) {
                requestFinish();
            } else {
                final List<Port> orderedPorts = PortUtilities
                        .getOrderedOutputPorts(this, OUTPUT, 0);
                outNr = selected;
                if (selected < 0) {
                    outNr = 0;
                    logger.debug(getName() + " : Selected port = " + selected + ". Using port "
                            + outNr + ".");
                    ExecutionTracerService.trace(this, "Selected port = " + selected
                            + ". Using port " + outNr + ".");
                } else if (selected >= outputCount) {
                    outNr = outputCount - 1;
                    logger.debug(getName() + " : Selected port = " + selected + ". Using port "
                            + outNr + ".");
                    ExecutionTracerService.trace(this, "Selected port = " + selected
                            + ". Using port " + outNr + ".");
                }

                try {
                    ExecutionTracerService.trace(this, "Selected port = " + outNr);
                    sendOutputMsg(
                            orderedPorts.get(outNr),
                            PasserelleUtil.createCopyMessage(this,
                                    MessageHelper.getMessageFromToken(token)));
                } catch (final PasserelleException e1) {
                    ExceptionUtil.throwProcessingException(getName()
                            + " - doFire() generated exception in outputPorts...broadcast()", token, e1);
                }

            }
            if (tokenIsNull) {
                requestFinish();
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - exit " + " - Output " + outNr + " has sent message "
                    + token);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.isencia.passerelle.actor.Actor#doInitialize()
     */
    @Override
    protected void doInitialize() throws InitializationException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName());
        }

        super.doInitialize();
        selectedReceived = false;
        tokenIsNull = false;

        // The two input ports are well connected
        if (select.getWidth() > 0 && input.getWidth() > 0) {
            // if (select.getWidth() > 0) {
            selectHandler = new PortHandler(select, new PortListenerAdapter() {
                @Override
                public void tokenReceived() {
                    final Token selectToken = selectHandler.getToken();
                    try {
                        final ManagedMessage msg = MessageHelper.getMessageFromToken(selectToken);
                        if (msg != null) {
                            // force conversion even if input is not an int
                            selectedReceived = true;
                            final String val = (String) PasserelleUtil.getInputValue(msg);
                            selected = Double.valueOf(val).intValue();
                            logger.debug("Event received : " + selected);
                        }
                    } catch (final NumberFormatException e) {
                        e.printStackTrace();
                    } catch (final Exception e) {
                        e.printStackTrace();
                        logger.error("", e);
                    }
                }

                @Override
                public void noMoreTokens() {
                    System.out.println("tokenIsNull");
                    tokenIsNull = true;
                    selectedReceived = true;
                }
            });
            selectHandler.start();
        }
        // Only one port is connected
        else if ((select.getWidth() == 0 && input.getWidth() != 0)
                || (select.getWidth() != 0 && input.getWidth() == 0)) {
            ExceptionUtil.throwInitializationException(select.getName() + " port and " + input.getName()
                    + " have to be connected", (select.getWidth() == 0) ? select : input);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - exit ");
        }

    }

    /**
     * @see be.tuple.passerelle.engine.actor.Actor#getExtendedInfo()
     */
    @Override
    protected String getExtendedInfo() {
        return outputCount + " output ports";
    }

    @Override
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
        final Switch copy = (Switch) super.clone(workspace);
        copy.outputPorts = new ArrayList<Port>(5);
        return copy;
    }
}