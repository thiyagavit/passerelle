package com.isencia.passerelle.process;

/**
 * Base class for all Processing related exceptions
 * 
 * @author durdav
 *
 */
public class ProcessException extends Exception {
	private static final long serialVersionUID = 1957811170216330265L;
	
	private String error;

	public ProcessException(String error, String message) {
		super(message);
		this.error = error;
	}

	public ProcessException(String error, Throwable cause) {
		super(cause);
		this.error = error;
	}

	public ProcessException(String error, String message, Throwable cause) {
		super(message, cause);
		this.error = error;
	}
	
	public String getError() {
		return error;
	}
}