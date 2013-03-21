/**
 * 
 */
package com.isencia.passerelle.process.service;

import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.common.exception.ProcessException;

/**
 * @author puidir
 *
 */
public class ServiceException extends ProcessException {

	private static final long serialVersionUID = 1L;

	public ServiceException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public ServiceException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}

}
