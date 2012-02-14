/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package fr.soleil.passerelle.actor.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import be.isencia.passerelle.actor.InitializationException;
import be.isencia.passerelle.core.Port;

/**
 * An actor that synchronizes the messages on all input ports, and then offers
 * them to a script.
 *
 * It can have a configurable, equal nr of input and output ports. The input
 * ports are all single-channel.
 *
 * @author erwin.de.ley@isencia.be
 */
@SuppressWarnings("serial")
public class SynchronizingScriptConverter extends
		be.isencia.passerelle.actor.convert.SynchronizingScriptConverter {
	private static Logger logger = LoggerFactory.getLogger(SynchronizingScriptConverter.class);

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public SynchronizingScriptConverter(final CompositeEntity container,
			final String name) throws IllegalActionException,
			NameDuplicationException {
		super(container, name);
	}

	/**
	 * This implementation allows different port counts...
	 */
	@Override
	protected boolean hasEqualNumberOfInputAndOutputPorts() {
		return false;
	}

	@Override
	protected String getExtendedInfo() {
		// return numberOfPorts!=null?numberOfPorts.getExpression():"0";
		return null;
	}

	@Override
	public void doInitialize() throws InitializationException {
		super.doInitialize();
		if (!isMockMode()) {
			for (final Port port : this.getInputPorts()) {
				port.setExpectedMessageContentType(String.class);
			}
			// small soleil code to limit usage of scripts
			if (script.contains("import fr.esrf.Tango")
					|| script.contains("PyTango")) {
				throw new InitializationException("Tango is not allowed", this,
						null);
			}
		}
	}

}
