/*
 * (c) Copyright 2004, iSencia NV Belgium
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia NV, Belgium.
 * Use is subject to license terms.
 */
package fr.soleil.passerelle.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;

import fr.soleil.passerelle.util.ExceptionUtil;

/**
 * Simple actor that reads input tokens and and forwards them to the output port
 * after a configurable delay (ms).
 * 
 * @version 1.0
 * @author erwin.de.ley@isencia.be
 */
@SuppressWarnings("serial")
public class Delay extends Transformer {
    private static Logger logger = LoggerFactory.getLogger(Delay.class);

    public Parameter timeParameter = null;
    private int time = 0;

    /**
     * Construct an actor with the given container and name.
     * 
     * @param container
     *            The container.
     * @param name
     *            The name of this actor.
     * @exception IllegalActionException
     *                If the actor cannot be contained by the proposed
     *                container.
     * @exception NameDuplicationException
     *                If the container already has an actor with this name.
     */
    public Delay(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
        super(container, name);
        timeParameter = new Parameter(this, "time(s)", new IntToken(1));
        timeParameter.setTypeEquals(BaseType.INT);
        registerConfigurableParameter(timeParameter);
    }

    @Override
    protected void doInitialize() throws InitializationException {
        // test pour class loader
        // this.getClass().forName(arg0);
        try {
            this.getClass().getClassLoader().loadClass("fr.soleil.passerelle.test.TestClass");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.doInitialize();
    }

    @Override
    public void doFire(ManagedMessage message) throws ProcessingException {
        if (logger.isTraceEnabled())
            logger.trace(getName() + " doFire() - entry");

        try {
            if (time > 0) {
                Thread.sleep(time * 1000);
            }
        } catch (InterruptedException e) {
            // do nothing, means someone wants us to stop
        } catch (Exception e) {
            ExceptionUtil.throwProcessingException(e.getMessage(), this, e);
        }

        try {
            sendOutputMsg(output, message);
        } catch (IllegalArgumentException e) {
            ExceptionUtil.throwProcessingException(getName() + " - doFire() generated exception " + e, this, e);
        }

        if (logger.isTraceEnabled())
            logger.trace(getName() + " doFire() - exit");
    }

    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (logger.isTraceEnabled())
            logger.trace(getName() + " attributeChanged() - entry :" + attribute);
        if (attribute == timeParameter) {
            time = ((IntToken) timeParameter.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
        if (logger.isTraceEnabled())
            logger.trace(getName() + " attributeChanged() - exit");
    }

    @Override
    protected String getExtendedInfo() {
        return Integer.toString(time) + " (s)";
    }

}