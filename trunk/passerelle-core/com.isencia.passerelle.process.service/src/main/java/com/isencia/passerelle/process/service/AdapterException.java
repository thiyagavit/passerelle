package com.isencia.passerelle.process.service;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;

public class AdapterException extends PasserelleException {
  
  private static final long serialVersionUID = 1L;

  public AdapterException(ErrorCode errorCode, String message) {
    super(errorCode, message, null);
  }

  public AdapterException(ErrorCode errorCode, String message, Throwable cause) {
    super(errorCode, message, cause);
  }

}
