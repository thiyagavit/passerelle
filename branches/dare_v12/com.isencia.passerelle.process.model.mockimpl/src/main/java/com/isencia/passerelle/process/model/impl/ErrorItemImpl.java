/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.Set;
import com.isencia.passerelle.core.ErrorCategory;
import com.isencia.passerelle.core.ErrorCode.Severity;
import com.isencia.passerelle.process.model.ErrorItem;

/**
 * @author "puidir"
 *
 */
public class ErrorItemImpl implements ErrorItem {

  public ErrorItemImpl() {
  }
  
  public ErrorItemImpl(Severity severity, ErrorCategory category, String code, String shortDescription, String description, Set<String> relatedDataTypes) {
  }

  public ErrorItemImpl(Severity severity, ErrorCategory category, String code, String shortDescription, Throwable cause, Set<String> relatedDataTypes) {
  }

  public Severity getSeverity() {
    return null;
  }

  public ErrorCategory getCategory() {
    return null;
  }

  public String getCode() {
    return null;
  }

  public String getShortDescription() {
    return null;
  }

  public String getDescription() {
    return null;
  }

  public Set<String> getRelatedDataTypes() {
    // TODO Auto-generated method stub
    return null;
  }

}
