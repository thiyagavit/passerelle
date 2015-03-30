package fr.soleil.passerelle.test;

/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */

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

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;

import fr.soleil.passerelle.util.ExceptionUtil;

/**
 * An actor that synchronizes the messages on all input ports, and then sends
 * them onwards via the corresponding output ports.
 * 
 * It has one fixed input port, the syncInput port which is multi-channel. This
 * allows to resolve bootstrap issues for synchronized loops, e.g. combining the
 * loop feedback msg and an ordinary start-up trigger.
 * 
 * Besides this port, it can have a configurable, equal nr of extra input and
 * output ports. The extra input ports are all single-channel.
 * 
 * @author erwin.de.ley@isencia.be
 */
@SuppressWarnings("serial")
public class Synchronizer extends Actor {
    private static Logger logger = LoggerFactory.getLogger(Synchronizer.class);

    public static final String NUMBER_OF_PORTS = "Extra nr of ports";

    public static final String INPUTPORTPREFIX = "input";

    public static final String OUTPUTPORTPREFIX = "output";

    public Port syncInput = null;
    private PortHandler syncInputHandler = null;
    private List<Port> inputPorts = null;

    private List<Port> outputPorts = null;
    // private List finishRequests = null;

    public Parameter numberOfPorts = null;

    private boolean finishRequest = false;

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public Synchronizer(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        syncInput = PortFactory.getInstance().createInputPort(this, "syncInput", null);
        // Create the lists to which the ports can be added
        inputPorts = new ArrayList<Port>(5);
        outputPorts = new ArrayList<Port>(5);
        // finishRequests = new ArrayList(5);

        // Create the parameters
        numberOfPorts = new Parameter(this, NUMBER_OF_PORTS, new IntToken(0));
        numberOfPorts.setTypeEquals(BaseType.INT);

        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"15\" " + "style=\"stroke-width:3.0\"/>\n" +

                "<line x1=\"-15\" y1=\"0\" x2=\"-1\" y2=\"0\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
                + "<line x1=\"-5\" y1=\"-3\" x2=\"-1\" y2=\"0\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
                + "<line x1=\"-5\" y1=\"3\" x2=\"-1\" y2=\"0\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
                + "<line x1=\"-15\" y1=\"-10\" x2=\"-1\" y2=\"-10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
                + "<line x1=\"-5\" y1=\"-13\" x2=\"-1\" y2=\"-10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
                + "<line x1=\"-5\" y1=\"-7\" x2=\"-1\" y2=\"-10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
                + "<line x1=\"-15\" y1=\"10\" x2=\"-1\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
                + "<line x1=\"-5\" y1=\"7\" x2=\"-1\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
                + "<line x1=\"-5\" y1=\"13\" x2=\"-1\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n" +

                "<line x1=\"1\" y1=\"0\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
                + "<line x1=\"10\" y1=\"-3\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
                + "<line x1=\"10\" y1=\"3\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
                + "<line x1=\"1\" y1=\"-10\" x2=\"15\" y2=\"-10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
                + "<line x1=\"10\" y1=\"-13\" x2=\"15\" y2=\"-10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
                + "<line x1=\"10\" y1=\"-7\" x2=\"15\" y2=\"-10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
                + "<line x1=\"1\" y1=\"10\" x2=\"15\" y2=\"10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
                + "<line x1=\"10\" y1=\"7\" x2=\"15\" y2=\"10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
                + "<line x1=\"10\" y1=\"13\" x2=\"15\" y2=\"10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
                + "</svg>\n");

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
     */
    @Override
    protected String getExtendedInfo() {
        return numberOfPorts != null ? numberOfPorts.getExpression() : "0";
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName());
        }

        finishRequest = false;

        syncInputHandler = new PortHandler(syncInput);
        if (syncInput.getWidth() > 0) {
            syncInputHandler.start();
        } else {
            ExceptionUtil.throwInitializationException("syncInput is not connected", syncInput);
        }

        for (final Port port : inputPorts) {
            if (port.getWidth() == 0) {
                ExceptionUtil.throwInitializationException(port.getName() + " is not connected", port);
            }
        }

        /*
         * for(int i=0;i<inputPorts.size();++i) finishRequests.set(i,new
         * Boolean(false));
         */
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
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - entry");
        }

        // just loop over all input ports
        // when we've passed all of them, this means
        // we've seen messages on all of them
        // and then we just need to forward the messages
        // on corresponding output ports
        isFiring = false;
        final Token token = syncInputHandler.getToken();
        isFiring = true;
        if (token != null && token != Token.NIL) {
            if (logger.isDebugEnabled()) {
                logger.debug(getName() + " - doFire() - received msg on port " + syncInput.getName());
            }
            final int nrPorts = inputPorts.size();
            final ManagedMessage[] messages = new ManagedMessage[nrPorts];
            for (int i = 0; i < nrPorts; ++i) {
                // if(!((Boolean)finishRequests.get(i)).booleanValue()) {
                final Port inputPort = inputPorts.get(i);
                try {
                    final ManagedMessage msg = MessageHelper.getMessage(inputPort);
                    if (msg != null) {
                        messages[i] = msg;
                        if (logger.isDebugEnabled()) {
                            logger.debug(getName() + " doFire() - received msg on port " + inputPort.getName());
                        }
                    } else {
                        finishRequest = true;
                        // finishRequests.set(i,new Boolean(true));
                        if (logger.isDebugEnabled()) {
                            logger.debug(getName() + " doFire() - found exhausted port " + inputPort.getName());
                        }
                    }
                } catch (final PasserelleException e) {
                    ExceptionUtil.throwProcessingException("Error reading from port", inputPort, e);
                }
                // }
            }
            if (!finishRequest) {
                for (int i = 0; i < nrPorts; ++i) {
                    // if(!((Boolean)finishRequests.get(i)).booleanValue()) {
                    final Port outputPort = outputPorts.get(i);
                    System.out.println("looping for " + outputPort.getName());
                    final ManagedMessage msg = messages[i];
                    if (msg != null) {
                        System.out.println("not null- output data for " + outputPort.getName());
                        sendOutputMsg(outputPort, msg);
                    }
                    // }
                }
            } else {
                requestFinish();
            }

            /*
             * if(areAllInputsFinished()) { requestFinish(); }
             */
        } else {
            requestFinish();
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - exit");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.isencia.passerelle.actor.Actor#getAuditTrailMessage(com.isencia.passerelle
     * .message.ManagedMessage, com.isencia.passerelle.core.Port)
     */
    @Override
    protected String getAuditTrailMessage(final ManagedMessage message, final Port port) {
        // no need for audit trail logging
        return null;
    }

    /**
     * @return
     */
    /*
     * private boolean areAllInputsFinished() { boolean result = true; for(int
     * i=0;i<finishRequests.size();++i) { result = result &&
     * ((Boolean)finishRequests.get(i)).booleanValue(); } return result; }
     */

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " attributeChanged() - entry - attribute :" + attribute);
        }

        // Change numberOfOutputs
        if (attribute == numberOfPorts) {
            int nrPorts = inputPorts.size();
            final int newPortCount = ((IntToken) numberOfPorts.getToken()).intValue();
            if (newPortCount < nrPorts) {
                for (int i = nrPorts - 1; i >= newPortCount; --i) {
                    try {
                        inputPorts.get(i).setContainer(null);
                        inputPorts.remove(i);
                        outputPorts.get(i).setContainer(null);
                        outputPorts.remove(i);
                        // finishRequests.remove(i);
                    } catch (final NameDuplicationException e) {
                        // should never happen for a setContainer(null)
                    }
                }
            } else if (newPortCount > nrPorts) {
                for (int i = nrPorts; i < newPortCount; ++i) {
                    try {
                        final String inputPortName = INPUTPORTPREFIX + i;
                        final String outputPortName = OUTPUTPORTPREFIX + i;
                        // need this extra step because Ptolemy maintains and
                        // loads duplicate stuff from the moml
                        // both the nr-of-ports parameter is in there, and all
                        // dynamically cfg ports themselves
                        // and these might have been loaded before the
                        // attributeChanged is invoked...
                        Port extraInputPort = (Port) getPort(inputPortName);
                        if (extraInputPort == null) {
                            extraInputPort = PortFactory.getInstance().createInputPort(this, inputPortName, null);
                            extraInputPort.setMultiport(false);
                        }
                        Port extraOutputPort = (Port) getPort(outputPortName);
                        ;
                        if (extraOutputPort == null) {
                            extraOutputPort = PortFactory.getInstance().createOutputPort(this, outputPortName);
                        }
                        inputPorts.add(extraInputPort);
                        outputPorts.add(extraOutputPort);
                        // finishRequests.add(new Boolean(false));
                    } catch (final NameDuplicationException e) {
                        logger.error("", e);
                        throw new IllegalActionException(this, e, "Error for index " + i);
                    }
                }
            }
            nrPorts = newPortCount;
        } else {
            super.attributeChanged(attribute);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " attributeChanged() - exit");
        }
    }

}
