package com.isencia.passerelle.runtime.ws.rest;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;

public class InvalidRequestException extends PasserelleException {
  private static final long serialVersionUID = -242270602480944226L;

  public InvalidRequestException(ErrorCode errorCode, String parameterName) {
    super(errorCode, parameterName, (Throwable)null);
  }

}
