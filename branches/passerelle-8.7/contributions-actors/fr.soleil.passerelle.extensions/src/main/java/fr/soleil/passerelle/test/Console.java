/*
 * (c) Copyright 2002, Tuple NV Belgium
 * All Rights Reserved.
 */
package fr.soleil.passerelle.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

import fr.soleil.passerelle.util.ExceptionUtil;

//////////////////////////////////////////////////////////////////////////
//// Const

/**
 * Produce a constant output. The value of the output is that of the token
 * contained by the <i>value</i> parameter.
 * 
 * @author dja
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Console extends Sink {
	// ~ Instance/static variables
	// ..............................................................................................................................

	// /////////////////////////////////////////////////////////////////
	// // variables ////
	private static Logger logger = LoggerFactory.getLogger(Console.class);

	public Parameter chopLengthParam;
	private int chopLength = 80;

	// ~ Constructors
	// ...........................................................................................................................................

	/**
	 * Construct a constant source with the given container and name. Create the
	 * <i>value</i> parameter, initialize its value to the default value of an
	 * IntToken with value 1.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this actor.
	 * @exception IllegalActionException
	 *                If the entity cannot be contained by the proposed
	 *                container.
	 * @exception NameDuplicationException
	 *                If the container already has an actor with this name.
	 */
	public Console(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		input.setMultiport(true);
		input.setExpectedMessageContentType(String.class);
		chopLengthParam = new Parameter(this, "Chop output at #chars",
				new IntToken(chopLength));
	}

	@Override
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {

		if (logger.isTraceEnabled())
			logger.trace(getName() + " :" + attribute);

		if (attribute == chopLengthParam) {
			IntToken chopLengthToken = (IntToken) chopLengthParam.getToken();
			if (chopLengthToken != null) {
				chopLength = chopLengthToken.intValue();
				logger.debug("Chop length changed to : " + chopLength);
			}
		} else
			super.attributeChanged(attribute);

		if (logger.isTraceEnabled())
			logger.trace(getName() + " - exit ");
	}

	@Override
	protected void sendMessage(ManagedMessage message)
			throws ProcessingException {
		if (logger.isTraceEnabled())
			logger.trace(getName());

		if (message != null) {
			if (isPassThrough()) {
				System.out.println(message.toString());
			} else {
				String content = null;
				try {
					content = message.getBodyContentAsString();
					if (chopLength < content.length()) {
						content = content.substring(0, chopLength)
								+ " !! CHOPPED !! ";
					}
				} catch (MessageException e) {
				    ExceptionUtil.throwProcessingException(ErrorCode.FATAL, e.getMessage(), message, e);
				}
				if (content != null)
					System.out.println(content);
			}

			System.out.flush();
		}

		if (logger.isTraceEnabled())
			logger.trace(getName() + " - exit ");
	}

	/**
	 * @see be.tuple.passerelle.engine.actor.Sink#getExtendedInfo()
	 */
	@Override
	protected String getExtendedInfo() {
		return "StdOut Console";
	}
}