package fr.soleil.passerelle.util;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.util.ExecutionTracerService;

public class ProcessingExceptionWithLog extends ProcessingException {

	public ProcessingExceptionWithLog(Actor actor, String message, Object context, Throwable rootException) {
		super(message,context,rootException);
		ExecutionTracerService.trace(actor,"Error: " + message);
	}

	public ProcessingExceptionWithLog(Actor actor,Severity severity, String message, Object context, Throwable rootException) {
		super(severity, message,context,rootException);
		ExecutionTracerService.trace(actor,"Error: " + message);
	}

}
