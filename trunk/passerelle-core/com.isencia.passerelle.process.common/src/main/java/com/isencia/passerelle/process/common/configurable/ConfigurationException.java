/**
 * 
 */
package com.isencia.passerelle.process.common.configurable;

import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.common.exception.ProcessException;

/**
 * @author delerw
 *
 */
public class ConfigurationException extends ProcessException {

	/**
	 * @param errorCode
	 * @param message
	 */
	public ConfigurationException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	/**
	 * @param errorCode
	 * @param message
	 * @param cause
	 */
	public ConfigurationException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}

}
