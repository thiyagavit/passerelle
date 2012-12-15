/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.isencia.passerelle.core;


/**
 * PasserelleException Base class for all exceptions in Passerelle.
 * 
 * @author erwin
 */
public class PasserelleException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * @deprecated use ErrorCodes instead
   */
  public static enum Severity {
    NON_FATAL, FATAL;
  }

  // still keeping it around for a while for backwards compatibility
  private Severity severity;
  // should by preference contain a ManagedMessage, to facilitate in-model error handling continuations
  private Object context;
  
  private Throwable rootException;
  
  private ErrorCode errorCode;

  /**
   * @param message
   * @param context
   * @param rootException
   * @deprecated use the constructors with ErrorCodes instead
   */
  public PasserelleException(String message, Object context, Throwable rootException) {
    super(message, rootException);
    this.rootException = rootException;
    this.context = context;
    this.errorCode = ErrorCode.ERROR;
    this.severity = Severity.NON_FATAL;
  }

  /**
   * @param severity can not be null
   * @param message
   * @param context
   * @param rootException
   * @deprecated use the constructors with ErrorCodes instead
   */
  public PasserelleException(Severity severity, String message, Object context, Throwable rootException) {
    super(message, rootException);
    this.rootException = rootException;
    this.context = context;
    this.severity = severity;
    
    switch (severity) {
    case FATAL:
      errorCode = ErrorCode.FATAL;
      break;
    case NON_FATAL:
    default:
      errorCode = ErrorCode.ERROR;
    }
  }

  /**
   * 
   * @param errorCode can not be null
   * @param message
   * @param context
   * @param rootException
   */
  public PasserelleException(ErrorCode errorCode, Object context, Throwable rootException) {
    super(errorCode!=null ? errorCode.getDescription() : null, rootException);
    if(errorCode==null) {
      throw new IllegalArgumentException("error code can not be null");
    }
    
    this.errorCode = errorCode;
    this.rootException = rootException;
    this.context = context;
    if(ErrorCode.Severity.FATAL.equals(errorCode.getSeverity())) {
      this.severity = Severity.FATAL;
    } else {
      this.severity = Severity.NON_FATAL;
    }
  }
  /**
   * 
   * @param errorCode can not be null
   * @param message
   * @param context
   * @param rootException
   */
  public PasserelleException(ErrorCode errorCode, String message, Object context, Throwable rootException) {
    super(message, rootException);
    if(errorCode==null) {
      throw new IllegalArgumentException("error code can not be null");
    }
    this.errorCode = errorCode;
    this.rootException = rootException;
    this.context = context;
    if(ErrorCode.Severity.FATAL.equals(errorCode.getSeverity())) {
      this.severity = Severity.FATAL;
    } else {
      this.severity = Severity.NON_FATAL;
    }
  }

  /**
   * @return the context object that was specified for this exception (can be null)
   */
  public Object getContext() {
    return context;
  }

  /**
   * 
   * @return the error code of this exception
   */
  public ErrorCode getErrorCode() {
    return errorCode;
  }

  /**
   * @return the root exception that caused this exception (can be null)
   * @deprecated since JDK 1.4; use Throwable.getCause()
   */
  public Throwable getRootException() {
    return rootException;
  }

  /**
   * @return the severity of the exception
   */
  public Severity getSeverity() {
    return severity;
  }

  /**
   * @return a string with the full info about the exception, incl severity, context etc.
   */
  public String getMessage() {
    return getErrorCode() + " - " + super.getMessage() + "\n - Context:" + getContext() + "\n - RootException:" + getCause();
  }

  /**
   * @return just the simple message, as passed in the exception's constructor
   */
  public String getSimpleMessage() {
    return super.getMessage();
  }
}
