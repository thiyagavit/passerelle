/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package fr.soleil.passerelle.actor.flow;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;
import fr.soleil.passerelle.actor.DynamicPortsActor;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * An actor that synchronizes the messages on all input ports, and then sends
 * them onwards via the corresponding output ports.
 * 
 * Besides this port, it can have a configurable, equal nr of extra input and
 * output ports. The extra input ports are all single-channel.
 * 
 * @author erwin.de.ley@isencia.be
 */
@SuppressWarnings("serial")
public class SingleSynchronizer extends DynamicPortsActor {
    private static Logger logger = LoggerFactory.getLogger(SingleSynchronizer.class);
    public Port output;

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public SingleSynchronizer(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        output = PortFactory.getInstance().createOutputPort(this, "output");

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

    @Override
    protected void doInitialize() throws InitializationException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - entry");
        }
        for (final Port port : getInputPorts()) {
            if (port.getWidth() == 0) {
                ExceptionUtil.throwInitializationException(port.getName() + " is not connected", port);
            }
        }
        super.doInitialize();

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - exit");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
     */
    @Override
    protected String getExtendedInfo() {
        return null;// numberOfPorts != null ? numberOfPorts.getExpression() :
        // "0";
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {
        final Iterator<MessageInputContext> allInputContexts = request.getAllInputContexts();
        int inputProcessed = 0;
        while (allInputContexts.hasNext()) {
            final MessageInputContext messageInputContext = allInputContexts.next();
            if (!messageInputContext.isProcessed()) {
                inputProcessed++;
            }
        }
        // output data only if all inputs have received something
        if (inputProcessed == this.getInputPorts().size()) {
            response.addOutputMessage(output, PasserelleUtil.createTriggerMessage());
        }

    }

    @Override
    protected DynamicPortType getPortConfiguration() {
        return DynamicPortType.ONLY_INPUTS;
    }

    /**
     * Return PULL mode for all input ports, so the fire loop blocks till all
     * ports have received an input msg.
     */
    @Override
    protected PortMode getPortModeForNewInputPort(final String portName) {
        return PortMode.PULL;
    }

}
