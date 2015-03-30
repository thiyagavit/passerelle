/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */

package fr.soleil.passerelle.actor.error;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.actor.ActorV3;
import fr.soleil.passerelle.util.ExceptionUtil;

/**
 * Registers itself as an ErrorCollector, and then sends out each received error
 * as an ErrorMessage on its output port. The input must be connected at the end
 * of the sequence
 * 
 * @author erwin
 * 
 */
@SuppressWarnings("serial")
public class ErrorReceiver extends ActorV3 implements ErrorCollector {

	private final static Logger logger = LoggerFactory.getLogger(ErrorReceiver.class);

	private final BlockingQueue<PasserelleException> errors = new LinkedBlockingQueue<PasserelleException>();

	public Port output;

	public ErrorReceiver(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		output = PortFactory.getInstance().createOutputPort(this);

	}

	@Override
	protected void doInitialize() throws InitializationException {
	  getDirectorAdapter().addErrorCollector(this);
	  super.doInitialize();
	}

	@Override
	protected void process(ActorContext ctxt, ProcessRequest request,
			ProcessResponse response) throws ProcessingException {
		// ErrorReceiver has no data input ports,
		// so it's like a Source in the days of the original Actor API.
		// The BlockingQueue (errors) is our data feed.
		try {
			PasserelleException e = errors.poll(1, TimeUnit.SECONDS);
			if (e != null) {
				ExecutionTracerService.trace(this, "error received");
				ManagedMessage msg = createErrorMessage(e);
				response.addOutputMessage(0, output, msg);
				drainErrorsQueueTo(response);
				// when one error has occured, stop
				this.requestFinish();
			}
		} catch (InterruptedException e) {
			// should not happen,
			// or if it does only when terminating the model execution
			// and with an empty queue, so we can just finish then
			requestFinish();
		}
	}

	private void drainErrorsQueueTo(ProcessResponse response)
			throws ProcessingException {
		while (!errors.isEmpty()) {
			PasserelleException e = errors.poll();
			if (e != null) {
				ManagedMessage msg = createErrorMessage(e);
				if (response != null) {
					// System.out.println("drainErrorsQueueTo- addOutputMessage");
					response.addOutputMessage(0, output, msg);
				} else {
					// System.out.println("drainErrorsQueueTo- sendOutputMsg");
					sendOutputMsg(output, msg);
				}
			} else {
				break;
			}
		}
	}

	public void acceptError(PasserelleException e) {
		// System.err.println("Error Receiver - accept error");
		try {
			errors.put(e);
		} catch (InterruptedException e1) {
			// should not happen,
			// or if it does only when terminating the model execution
			logger.error("Receipt interrupted for ", e);
		}
	}

	@Override
	protected void doWrapUp() throws TerminationException {
	  getDirectorAdapter().removeErrorCollector(this);

		try {
			drainErrorsQueueTo(null);
		} catch (Exception e) {
		    ExceptionUtil.throwTerminationException(getName()
                            + " - doWrapUp() generated exception " + e, errors, e);
		}

		super.doWrapUp();
	}
}
