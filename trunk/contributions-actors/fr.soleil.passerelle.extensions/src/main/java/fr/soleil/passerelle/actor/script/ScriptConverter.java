/*
 * (c) Copyright 2002, Tuple NV Belgium
 * All Rights Reserved.
 * 
 * This software is the proprietary information of Tuple NV, Belgium.
 * Use is subject to license terms.
 */

package fr.soleil.passerelle.actor.script;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.IOUtils;
import org.python.core.Py;
import org.python.core.PySystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;

import fr.soleil.passerelle.util.ExceptionUtil;

/**
 * DOCUMENT ME!
 * 
 * @version $Id: ScriptConverter.java,v 1.8 2005/10/26 10:33:43 erwin Exp $
 * @author Dirk Jacobs
 */
public class ScriptConverter extends Transformer {
    // ~ Static variables/initializers
    // ··························································································································

    private static final long serialVersionUID = -3609025105490808449L;
    private static final String JAVASCRIPT = "javascript";
    private static final String JYTHON = "jython";
    public static final String PATH_PARAM = "Path";
    public static final String LANGUAGE_PARAM = "Language";
    public static final String CONTAINERNAME_PARAM = "Container name";

    private static Logger logger = LoggerFactory.getLogger(ScriptConverter.class);

    // ~ Instance variables
    // ·····································································································································

    private BSFManager manager = new BSFManager();
    public StringParameter languageParam = null;
    public FileParameter scriptPathParam = null;
    public Parameter containerNameParam = null;
    private String containerName = "";
    private String language = "";
    private String script = null;
    private String scriptPath = null;

    // private TransformData data = new TransformData();

    // ~ Constructors
    // ···········································································································································

    /**
     * Construct an actor in the specified container with the specified name.
     * 
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException If the actor cannot be contained by the proposed container.
     * @exception NameDuplicationException If the name coincides with an actor already in the container.
     */
    public ScriptConverter(CompositeEntity container, String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        scriptPathParam = new FileParameter(this, PATH_PARAM);

        languageParam = new StringParameter(this, LANGUAGE_PARAM);
        languageParam.setExpression(JYTHON);
        languageParam.addChoice(JAVASCRIPT);
        languageParam.addChoice(JYTHON);

        containerNameParam = new StringParameter(this, CONTAINERNAME_PARAM);
        containerNameParam.setExpression("container");
    }

    /*
     * (non-Javadoc)
     * @see ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute)
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " :" + attribute);
        }

        if (attribute == scriptPathParam) {
            try {
                scriptPath = scriptPathParam.asURL().getPath();
                logger.debug("Path changed to : " + scriptPath);
            } catch (Exception e) {
                // ignore
            }
        } else if (attribute == languageParam) {
            StringToken languageToken = (StringToken) languageParam.getToken();

            if ((languageToken != null) && (languageToken.stringValue().length() > 0)) {
                language = languageToken.stringValue();
                logger.debug("Language changed to : " + language);
            }
        } else if (attribute == containerNameParam) {
            StringToken containerNameToken = (StringToken) containerNameParam.getToken();
            if ((containerNameToken != null) && (containerNameToken.stringValue().length() > 0)) {
                containerName = containerNameToken.stringValue();
                logger.debug("Container name changed to : " + containerName);
            }
        } else {
            super.attributeChanged(attribute);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - exit ");
        }
    }

    protected void doFire(ManagedMessage message) throws ProcessingException {
        if (logger.isTraceEnabled())
            logger.trace(getName() + " - message :" + message);

        if (message != null) {
            MessageFlowElement inputContainer = new MessageFlowElement(
                    new MessageFlowElement.MessageAndPort(0, message), 1);
            try {
                manager.declareBean(containerName, inputContainer, inputContainer.getClass());
                manager.exec(language, scriptPath, -1, -1, script);
            } catch (BSFException e) {
                ExceptionUtil.throwProcessingException(getName() + " - script execution generated an exception " + e, message, e);
            }

            try {
                sendOutputMsg(output, inputContainer.getOutputMessage());
            } catch (IllegalArgumentException e) {
                ExceptionUtil.throwProcessingException(getName() + " - doFire() generated exception " + e, message, e);
            }

        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - exit ");
        }
    }

    /*
     * (non-Javadoc)
     * @see be.isencia.passerelle.actor.Actor#doInitialize()
     */
    protected void doInitialize() throws InitializationException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName());
        }

        super.doInitialize();

        try {
            Reader scriptReader = new FileReader(scriptPath);
            script = IOUtils.getStringFromReader(scriptReader);
            scriptReader.close();
        } catch (FileNotFoundException e) {
            ExceptionUtil.throwInitializationException(ErrorCode.FATAL, getName()
                    + " - Script file not found.", scriptPath,e);
        } catch (IOException e) {
            ExceptionUtil.throwInitializationException(ErrorCode.FATAL, getName()
                    + " - Script file could not be opened.", scriptPath,e);
        }

        // get Jython initialised correctly in OSGi as well
        // then we need to manually specify all packages
        // imported by the jython script for some reason
        if (JYTHON.equals(language)) {
            Py.initPython();
            PySystemState.add_package("com.isencia.passerelle.message");
            PySystemState.add_package("fr.soleil.passerelle.actor.script");
        }
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " - exit ");
        }
    }

    /**
     * @see be.tuple.passerelle.engine.actor.Actor#getExtendedInfo()
     */
    protected String getExtendedInfo() {
        return scriptPath;
    }

    /**
     * @see be.tuple.passerelle.engine.actor.Actor#createPaneFactory()
     */
    // no longer supported in Ptolemy II 4.x
    // protected void createPaneFactory() throws IllegalActionException, NameDuplicationException {
    // PaneFactoryCreator.createPaneFactory(this,
    // be.isencia.passerelle.actor.gui.pane.custom.ScriptConverterParamEditPane.class);
    // }

}