/*
 * (c) Copyright 2002, Tuple NV Belgium
 * All Rights Reserved.
 */
package fr.soleil.passerelle.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TriggeredSource;
import com.isencia.passerelle.message.ManagedMessage;

import fr.soleil.passerelle.util.ExceptionUtil;

//////////////////////////////////////////////////////////////////////////
//// Const
/**
 * Produce a constant output.
 */
@SuppressWarnings("serial")
public class Const extends TriggeredSource {

    private static Logger logger = LoggerFactory.getLogger(Const.class);
    private boolean messageSent = false;
    private ManagedMessage dataMsg = null;

    /**
     * Construct a constant source with the given container and name.
     * Create the <i>value</i> parameter, initialize its value to
     * the default value of an IntToken with value 1.
     * 
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the entity cannot be contained
     *                by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *                actor with this name.
     */
    public Const(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
        super(container, name);
        value = new StringParameter(this, "value");
        value.setExpression("");
        registerConfigurableParameter(value);

    }

    // /////////////////////////////////////////////////////////////////
    // // ports and parameters ////

    /**
     * The value produced by this constant source.
     * By default, it contains an StringToken with an empty string.
     */
    public Parameter value;

    /*
     *  (non-Javadoc)
     * @see com.isencia.passerelle.actor.Actor#doInitialize()
     */
    protected void doInitialize() throws InitializationException {
		if (logger.isTraceEnabled())
			logger.trace(getName());

		messageSent = false;
		try {
			String tokenMessage = ((StringToken) value.getToken()).stringValue();
			dataMsg = createMessage(tokenMessage, "text/plain");
		} catch (Exception e) {
		    ExceptionUtil.throwInitializationException(getName()+" - getMessage() generated exception "+e,value,e);
		} 
		super.doInitialize();

		if (logger.isTraceEnabled())
			logger.trace(getName()+" - exit ");
	}

    protected ManagedMessage getMessage() throws ProcessingException {
        if (logger.isTraceEnabled())
            logger.trace(getName());

        if (messageSent && !isTriggerConnected())
            return null;

        messageSent = true;

        if (logger.isTraceEnabled())
            logger.trace(getName() + " - exit ");

        return dataMsg;
    }

    /**
     * @see be.tuple.passerelle.engine.actor.Source#getInfo()
     */
    protected String getExtendedInfo() {
        return value.getExpression();
    }

    /**
     * @see be.tuple.passerelle.engine.actor.TriggeredSource#doWaitForTrigger()
     */
    protected boolean mustWaitForTrigger() {
        return true;
    }

}