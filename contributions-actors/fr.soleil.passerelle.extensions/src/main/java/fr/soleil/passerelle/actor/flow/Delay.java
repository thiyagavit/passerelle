/*
 * (c) Copyright 2004, iSencia NV Belgium All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia NV, Belgium. Use is
 * subject to license terms.
 */
package fr.soleil.passerelle.actor.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;

import fr.soleil.passerelle.actor.TransformerV5;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * Simple actor that reads input tokens and and forwards them to the output port
 * after a configurable delay (ms).
 * 
 * @version 1.0
 * @author erwin.de.ley@isencia.be
 */
public class Delay extends TransformerV5 {
    /**
         *
         */
    private static final long serialVersionUID = -3055643090088110298L;

    private static Logger logger = LoggerFactory.getLogger(Delay.class);

    public Parameter timeParameter;
    private double time = 0;
 
    public Parameter takePortValueParam;
    private boolean takePortValue = false;

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
    public Delay(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        timeParameter = new StringParameter(this, "time(s)");
        timeParameter.setExpression("1");
        takePortValueParam = new Parameter(this, "take delay from port", new BooleanToken(false));
        takePortValueParam.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {
        final long startTime = System.currentTimeMillis();
        double waitingTimeInS = time;
        final ManagedMessage timeMessage = request.getMessage(input);
        if (takePortValue) {
            Object inputValue = PasserelleUtil.getInputValue(timeMessage);
            if (inputValue instanceof Number) {
                waitingTimeInS = ((Number) inputValue).doubleValue();
            } else if (inputValue instanceof String) {
                try {
                    waitingTimeInS = Double.valueOf(inputValue.toString());
                } catch (NumberFormatException e) {
                    ExceptionUtil.throwProcessingExceptionWithLog(this, ErrorCode.FATAL,inputValue + " is not a number",this,e);
                }
            } else {
                ExceptionUtil.throwProcessingExceptionWithLog(this, ErrorCode.FATAL,"input is not supported",this,new Exception("input is not supported"));
            }
        }
        final long waitingTime = (long) (waitingTimeInS * 1000);
        long alreadyTakenTime = System.currentTimeMillis() - startTime;
        while (alreadyTakenTime < waitingTime  && !isFinishRequested()) {
            alreadyTakenTime = System.currentTimeMillis() - startTime;
        }
        logger.debug("waitingTimeInMs : {}", (System.currentTimeMillis() - startTime));
        sendOutputMsg(output, timeMessage);
    }
  
    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == takePortValueParam) {
            takePortValue = PasserelleUtil.getParameterBooleanValue(takePortValueParam);
            if (takePortValue) {
                input.setExpectedMessageContentType(Double.class);
            } else {
                input.setExpectedMessageContentType(null);
            }
        } else if (attribute == timeParameter) {
            time = PasserelleUtil.getParameterDoubleValue(timeParameter);
            // Check the validity of this parameter only if the value is not taken by port
        } else {
            super.attributeChanged(attribute);
        }
        
        if (!takePortValue) {
            if (time < 0) {
                throw new IllegalActionException(this, "Time value must be upper or equal to 0");
            }
        }
    }

    @Override
    protected String getExtendedInfo() {
        return time + " (s)";
    }

}