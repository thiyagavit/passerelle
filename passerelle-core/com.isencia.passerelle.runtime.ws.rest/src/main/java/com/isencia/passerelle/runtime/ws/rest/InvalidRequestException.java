package com.isencia.passerelle.runtime.ws.rest;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;

public class InvalidRequestException extends PasserelleException {
  
  public InvalidRequestException(ErrorCode errorCode, String parameterName) {
    super(errorCode, parameterName, (Throwable)null);
  }

}
