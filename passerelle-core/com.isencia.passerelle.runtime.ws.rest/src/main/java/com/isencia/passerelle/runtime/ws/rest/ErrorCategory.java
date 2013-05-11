package com.isencia.passerelle.runtime.ws.rest;


public class ErrorCategory extends com.isencia.passerelle.core.ErrorCategory {
  private static final long serialVersionUID = 4259231927471828901L;
  public final static ErrorCategory WS_REST_ROOT = new ErrorCategory("WS_REST_ROOT", null, "WS-REST");
  public final static ErrorCategory WS_REST_TECHNICAL = new ErrorCategory("WS_REST_TECHNICAL", WS_REST_ROOT, "TECH");
  public final static ErrorCategory WS_REST_FUNCTIONAL = new ErrorCategory("WS_REST_FUNCTIONAL", WS_REST_ROOT, "FUNC");

  public ErrorCategory(String name, com.isencia.passerelle.core.ErrorCategory parent, String prefix) {
    super(name, parent, prefix);
  }

}
