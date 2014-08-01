package fr.soleil.passerelle.util;

import ptolemy.kernel.util.Nameable;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.util.ExecutionTracerService;

public class ProcessingExceptionWithLog extends ProcessingException {

  private static final long serialVersionUID = 7336909999399919562L;

  public ProcessingExceptionWithLog(Actor actor, String message, Object context, Throwable rootException) {
		super(message,context,rootException);
		ExecutionTracerService.trace(actor,"Error: " + message);
	}

	@Deprecated
	public ProcessingExceptionWithLog(Actor actor,Severity severity, String message, Object context, Throwable rootException) {
		super(severity, message,context,rootException);
		ExecutionTracerService.trace(actor,"Error: " + message);
	}

  public ProcessingExceptionWithLog(Actor actor, ErrorCode errorCode, String message, Nameable context, Throwable rootException) {
    super(errorCode, message,context,rootException);
    ExecutionTracerService.trace(actor,"Error: " + message);
  }

}
