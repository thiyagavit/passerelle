/**
 * 
 */
package com.isencia.passerelle.process.common.exception;


/**
 * @author puidir
 *
 */
public class ProcessException extends Exception {

	private static final long serialVersionUID = 1L;

	private ErrorCode errorCode;
	
	public ProcessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public ProcessException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	@Override
	public String getMessage() {
		return getErrorCode() + super.getMessage();
	}
}
