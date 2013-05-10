package com.isencia.passerelle.runtime.ws.rest;


public class ErrorCode extends com.isencia.passerelle.core.ErrorCode {

  public final static ErrorCode MISSING_PARAM = new ErrorCode("MISSING_PARAM", "1000", ErrorCategory.WS_REST_FUNCTIONAL, ErrorCode.Severity.WARNING,"Request contents error");

  public ErrorCode(String name, String code, ErrorCategory category, Severity severity, String description) {
    super(name, code, category, severity, description);
  }

  public ErrorCode(String name, String code, String topic, ErrorCategory category, Severity severity, String description) {
    super(name, code, topic, category, severity, description);
  }

}
