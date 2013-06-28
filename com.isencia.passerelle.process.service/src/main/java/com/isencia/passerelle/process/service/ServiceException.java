/**
 * 
 */
package com.isencia.passerelle.process.service;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.process.common.exception.ErrorCode;

/**
 * @author puidir
 *
 */
public class ServiceException extends PasserelleException {

	private static final long serialVersionUID = 1L;

	public ServiceException(ErrorCode errorCode, String message) {
		super(errorCode, message, (Throwable)null);
	}

	public ServiceException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}

}
