/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */

package fr.soleil.passerelle.errorcontrol;

import ptolemy.actor.CompositeActor;
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
public class StopStrategy extends DefaultErrorControlStrategy {

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public StopStrategy(Director container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

	@Override
	public void handleFireException(Actor a, ProcessingException e) throws IllegalActionException {
		//System.out.println("2:Stopping model execution");
		ExecutionTracerService.trace(a, "Error "+e.getMessage());
		ExecutionTracerService.trace(a, "!!!ERROR occured - Stopping model!!!");
		CompositeActor model = (CompositeActor) ((Director) getContainer()).getContainer();
		model.getManager().stop();
	}
}
