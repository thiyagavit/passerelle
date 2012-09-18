/**
 * 
 */
package com.isencia.passerelle.process.model;

/**
 * Maintains all info related to an error that occurred during 
 * the processing of a request/task.
 * 
 * @author "puidir"
 *
 */
public interface ErrorItem {

  public enum Severity {
    INFO, WARNING, ERROR, FATAL;
  }
  
  public enum Category {
    FUNCTIONAL, TECHNICAL;
  }

  /**
   * @return how severe was the error
   */
  Severity getSeverity();
  
  /**
   * @return whether the error concerns a functional or technical issue.
   */
  Category getCategory();

  /**
   * @return a formatted error code identifying the error type of this item, e.g. DAR-1234
   */
  String getCode();

  /**
   * @return short description, e.g. Missing data, Timed out
   */
  String getShortDescription();
  
  /**
   * @return a readable description of the error type of this item
   */
  String getDescription();

}
