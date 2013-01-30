/**
 * 
 */
package com.isencia.passerelle.process.common.exception;

/**
 * @author delerw
 * 
 */
public class ErrorCategory extends com.isencia.passerelle.core.ErrorCategory {

  private static final long serialVersionUID = 1L;

  public final static ErrorCategory ROOT = new ErrorCategory("ROOT", null, "ROOT");
  public final static ErrorCategory FUNCTIONAL = new ErrorCategory("FUNCTIONAL", ROOT, "FUNC");
  public final static ErrorCategory TECHNICAL = new ErrorCategory("TECHNICAL", ROOT, "TECH");

  /**
   * @param name
   * @param parent
   * @param prefix
   */
  public ErrorCategory(String name, ErrorCategory parent, String prefix) {
    super(name, parent, prefix);
  }

}
