/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */

package fr.soleil.passerelle.errorcontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * @author erwin
 *
 */
@SuppressWarnings("serial")
public class ContinueStrategy extends DefaultErrorControlStrategy {

	private final static Logger logger = LoggerFactory.getLogger(ContinueStrategy.class);

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public ContinueStrategy(Director container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

	@Override
	public void handleFireException(Actor a, ProcessingException e) throws IllegalActionException {
		logger.info("1:Continuing model execution");
		ExecutionTracerService.trace(a, "!!!ERROR occured - Continuing model!!!");
	}
}
