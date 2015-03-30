/*
 * (c) Copyright 2002, Tuple NV Belgium
 * All Rights Reserved.
 * 
 * This software is the proprietary information of Tuple NV, Belgium.
 * Use is subject to license terms.
 */

package fr.soleil.passerelle;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.NoRoomException;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLParser;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.util.ExceptionUtil;

/**
 * Executes Passerelle Models. the incoming message may contain the list of "models" (files names) separated by commas.
 * 
 * @author Dirk Jacobs
 */
@SuppressWarnings("serial")
public class ModelExecutor extends Actor {

    private static Logger logger = LoggerFactory.getLogger(ModelExecutor.class);

    private PortHandler triggerHandler = null;
    // private FileParameter modelParameter;
    private String modelsPaths = null;
    public Port trigger = null;
    public Port outputTrigger = null;
    private boolean triggerConnected = false;

    public ModelExecutor(CompositeEntity container, String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        /*
         * modelParameter = new FileParameter(this, MODEL_PARAMETER); modelParameter.setExpression("");
         * registerConfigurableParameter(modelParameter);
         */

        trigger = PortFactory.getInstance().createInputPort(this, "models names", null);
        outputTrigger = PortFactory.getInstance().createOutputPort(this, "trigger");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
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
                +

                // body
                "<line x1=\"-9\" y1=\"-16\" x2=\"-12\" y2=\"-8\" "
                + "style=\"stroke-width:2.0\"/>\n"
                +
                // backwards leg
                "<line x1=\"-11\" y1=\"-7\" x2=\"-16\" y2=\"-7\" "
                + "style=\"stroke-width:1.0\"/>\n"
                + "<line x1=\"-13\" y1=\"-8\" x2=\"-15\" y2=\"-8\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-16\" y1=\"-7\" x2=\"-16\" y2=\"-5\" "
                + "style=\"stroke-width:1.0\"/>\n"
                +

                // forward leg
                "<line x1=\"-11\" y1=\"-11\" x2=\"-8\" y2=\"-8\" "
                + "style=\"stroke-width:1.5\"/>\n"
                + "<line x1=\"-8\" y1=\"-8\" x2=\"-8\" y2=\"-6\" "
                + "style=\"stroke-width:1.0\"/>\n"
                + "<line x1=\"-8\" y1=\"-5\" x2=\"-6\" y2=\"-5\" "
                + "style=\"stroke-width:1.0\"/>\n"
                +

                // forward arm
                "<line x1=\"-10\" y1=\"-14\" x2=\"-7\" y2=\"-11\" "
                + "style=\"stroke-width:1.0\"/>\n"
                + "<line x1=\"-7\" y1=\"-11\" x2=\"-5\" y2=\"-14\" "
                + "style=\"stroke-width:1.0\"/>\n"
                +
                // backward arm
                "<line x1=\"-11\" y1=\"-14\" x2=\"-14\" y2=\"-14\" "
                + "style=\"stroke-width:1.0\"/>\n"
                + "<line x1=\"-14\" y1=\"-14\" x2=\"-12\" y2=\"-11\" "
                + "style=\"stroke-width:1.0\"/>\n"
                +
                // miniature model
                "<rect x=\"-16\" y=\"-2\" width=\"32\" "
                + "height=\"20\" style=\"fill:white;stroke:darkgrey\"/>\n"
                +
                // director
                "<circle cx=\"-12\" cy=\"2\" r=\"2\""
                + "style=\"fill:red;stroke:red\"/>\n"
                +
                // source
                "<rect x=\"-14\" y=\"10\" width=\"6\" "
                + "height=\"6\" style=\"fill:orange;stroke:orange\"/>\n"
                + "<circle cx=\"-12\" cy=\"12\" r=\"2\""
                + "style=\"fill:white;stroke:black\"/>\n"
                +
                // other actors
                "<rect x=\"-4\" y=\"10\" width=\"4\" "
                + "height=\"4\" style=\"fill:grey;stroke:grey\"/>\n"
                + "<rect x=\"-2\" y=\"2\" width=\"4\" "
                + "height=\"4\" style=\"fill:grey;stroke:grey\"/>\n"
                +
                // sinks
                "<rect x=\"8\" y=\"0\" width=\"6\" " + "height=\"6\" style=\"fill:green;stroke:green\"/>\n"
                + "<circle cx=\"10\" cy=\"2\" r=\"2\""
                + "style=\"fill:white;stroke:black\"/>\n"
                + "<rect x=\"8\" y=\"8\" width=\"6\" "
                + "height=\"6\" style=\"fill:green;stroke:green\"/>\n"
                + "<circle cx=\"10\" cy=\"10\" r=\"2\""
                + "style=\"fill:white;stroke:black\"/>\n"
                +
                // connections
                "<line x1=\"-7\" y1=\"13\" x2=\"-3\" y2=\"5\" " + "style=\"stroke-width:0.5;stroke:grey\"/>\n"
                + "<line x1=\"-7\" y1=\"13\" x2=\"-5\" y2=\"12\" " + "style=\"stroke-width:0.5;stroke:grey\"/>\n"
                + "<line x1=\"3\" y1=\"4\" x2=\"7\" y2=\"3\" " + "style=\"stroke-width:0.5;stroke:grey\"/>\n"
                + "<line x1=\"1\" y1=\"12\" x2=\"7\" y2=\"11\" " + "style=\"stroke-width:0.5;stroke:grey\"/>\n"
                + "</svg>\n");
    }

    @Override
    protected void doFire() throws ProcessingException {
        ManagedMessage msg = null;
        String[] modelPath = null;

        if (logger.isTraceEnabled()) {
            logger.trace(getName());
        }
        if (triggerConnected) {
            if (logger.isDebugEnabled()) {
                logger.debug(getName() + " - Waiting for trigger");
            }

            Token token = triggerHandler.getToken();

            if (token == null) {
                requestFinish();
            } else {
                try {
                    msg = MessageHelper.getMessageFromToken(token);
                    if (msg != null) {
                        modelsPaths = msg.getBodyContent().toString();
                    }
                } catch (PasserelleException e) {
                    ExceptionUtil.throwProcessingException(getName()
                            + " - doFire() generated an exception while reading message", token, e);
                }
                logger.debug("Received msg :" + msg);
            }

            if (logger.isInfoEnabled()) {
                logger.info(getName() + " - Trigger received");
            }
        }

        if (!isFinishRequested() && modelsPaths != null) {

            modelPath = modelsPaths.split(",");

            if ((modelPath != null) && (modelPath.length > 0)) {
                MoMLParser parser = new MoMLParser();

                for (int i = 0; i < modelPath.length; i++) {
                    try {
                        File file = new File(modelPath[i]);
                        URL xmlFile = file.toURI().toURL();

                        CompositeActor topLevel = (CompositeActor) parser.parse(null, xmlFile);
                        if (getAuditLogger().isInfoEnabled()) {
                            getAuditLogger().info("Executing " + modelPath[i]);
                        }
                        logger.debug("Model Executor - Executing " + modelPath[i]);
                        ExecutionTracerService.trace(this, "Executing " + modelPath[i]);
                        Manager manager = new Manager(topLevel.workspace(), getName());
                        topLevel.setManager(manager);
                        manager.execute();
                    } catch (Exception e) {
                        ExceptionUtil.throwProcessingException(getName() + " - doFire() generated an exception " + e,
                                this, e);
                    }
                }
            }
        }

        ManagedMessage resultMsg = MessageFactory.getInstance().createTriggerMessage();

        try {
            sendOutputMsg(outputTrigger, resultMsg);
            // output.broadcast(new ObjectToken(resultMsg));
        } catch (NoRoomException e2) {
            ExceptionUtil.throwProcessingException("Error sending output data", outputTrigger, e2);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - exit ");
        }
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName());
        }

        super.doInitialize();

        triggerConnected = trigger.getWidth() > 0;

        if (triggerConnected) {
            if (logger.isDebugEnabled())
                logger.debug(getName() + " - Trigger(s) connected");
            triggerHandler = new PortHandler(trigger);
            triggerHandler.start();
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - exit ");
        }
    }

    @Override
    protected boolean doPostFire() throws ProcessingException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName());
        }

        boolean res = triggerConnected;

        if (res) {
            res = super.doPostFire();
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - exit " + " :" + res);
        }
        return res;
    }

    @Override
    protected String getExtendedInfo() {
        return modelsPaths;
    }
}