/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package fr.soleil.passerelle.actor.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;

/**
 * ErrorCollector
 * 
 * A class that collect the first error occured in a model and output the error 
 * on its output port. Useful for executing a particular action in case of error.
 * This actor must be placed at the end of the model, its input port connected to the last executed actor
 * (to force its termination if no error occurs).
 * @author erwin.de.ley@isencia.be
 */
@SuppressWarnings("serial")
public class ErrorCollector extends Actor implements com.isencia.passerelle.ext.ErrorCollector {
	
	private static final Logger logger = LoggerFactory.getLogger(ErrorCollector.class);

	/** The input port.  
	 * This input must be connected to force termination is not error occured
	 */
	public Port input;
	public PortHandler inputHandler = null;

	/** The output port. By default, the type of this output is constrained
	 *  to be at least that of the input.
	 */
	public Port output;
	private PasserelleException error = null;
	/**
	 * @param container
	 * @param name
	 * @throws NameDuplicationException
	 * @throws IllegalActionException
	 */
	public ErrorCollector(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		input = PortFactory.getInstance().createInputPort(this, "end", null);
		output = PortFactory.getInstance().createOutputPort(this, "output");
	}

	/*
	 *  (non-Javadoc)
	 * @see com.isencia.passerelle.actor.Actor#doInitialize()
	 */	
	protected void doInitialize() throws InitializationException {
		if (logger.isTraceEnabled()) {
			logger.trace(getName());
		}
		error = null;
		inputHandler = new PortHandler(input);
		if(input.getWidth()>0) {
			inputHandler.start();
		}

		try {
			getDirectorAdapter().addErrorCollector(this);
		} catch (ClassCastException e) {
			// means the actor is used without a Passerelle Director
			// just log this. Only consequence is that we'll never receive
			// any error messages via acceptError
			logger.info(getName()+" - used without Passerelle Director!!");
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(getName()+" - exit ");
		}
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.actor.Actor#doFire()
	 */
	protected void doFire() throws ProcessingException {
		if (logger.isTraceEnabled())
			logger.trace(getName());
		
		isFiring = false;	
		if (error != null) {
				sendOutputMsg(output,createErrorMessage(error));
				requestFinish();
		}else{
			Token token = inputHandler.getToken();
			if (token != null) {
				requestFinish();
			}
		}
		isFiring = true;

		if(logger.isTraceEnabled())
			logger.trace(getName()+" - exit ");
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
	 */
	protected String getExtendedInfo() {
		return "";
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.domain.cap.ErrorCollector#acceptError(com.isencia.passerelle.actor.PasserelleException)
	 */
	public void acceptError(PasserelleException e) {
		if (logger.isTraceEnabled()) {
			logger.trace(getName() + " - received :" + e);
		}
		error = e;
		
		if (logger.isTraceEnabled()) {
			logger.trace(getName()+" - exit ");
		}
	}
}
