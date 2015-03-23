/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */

package fr.soleil.passerelle.errorcontrol;

import ptolemy.actor.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.util.ExceptionUtil;

/**
 * @author erwin
 *
 */
@SuppressWarnings("serial")
public class RetryStrategy extends DefaultErrorControlStrategy {

	private int totalnbRetry = 1;
	private int nbRetry = 0;
	
	private double pausingTime = 0;
	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public RetryStrategy(Director container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		nbRetry = 0; 
	}

	@Override
	public void handleFireException(Actor a, ProcessingException e) throws IllegalActionException {
		//System.out.println("RetryStrategy.handleFireException");
		//TODO: gerer nbRetry par acteur
		if(!ExceptionUtil.isSeverityFatal(e.getSeverity())){
			if(nbRetry < totalnbRetry){
				ExecutionTracerService.trace(a, e.getMessage());
				ExecutionTracerService.trace(a, "!!!ERROR occured - Retrying step!!!");
				try {
					Thread.sleep((long)(pausingTime*1000));
				} catch (InterruptedException e1) {
					// not important
				}
				nbRetry ++;
				 //((Director) getContainer()).reportError(e);
				a.fire();
				
			}else{
				ExecutionTracerService.trace(a, "!!!All retries failed!!!");
				nbRetry = 0;
				//CompositeActor model = (CompositeActor) ((Director) getContainer()).getContainer();
				//model.getManager().waitForCompletion();
				//model.getManager().stop();
				super.handleFireException(a, e);
			}
		}else{
			super.handleFireException(a, e);
		}
	}

	public int getTotalnbRetry() {
		return totalnbRetry;
	}

	public void setTotalnbRetry(int totalnbRetry) {
		this.totalnbRetry = totalnbRetry;
	}

	public double getPausingTime() {
		return pausingTime;
	}

	public void setPausingTime(double pausingTime) {
		this.pausingTime = pausingTime;
	}
}
