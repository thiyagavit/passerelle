/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.isencia.passerelle.process.model.ErrorItem;

/**
 * @author "puidir"
 * 
 */
public class ErrorItemImpl implements ErrorItem {

  private Category category;
  private Severity severity;
  private String code;
  // limited to length 100
  private String shortDescription;
  // limited to length 1000
  private String description;

  /**
   * @param severity
   * @param category
   * @param code
   * @param description
   */
  public ErrorItemImpl(Severity severity, Category category, String code, String shortDescription, String description) {
    super();
    this.severity = severity;
    this.category = category;
    this.code = code;
    setShortDescription(shortDescription);
    setDescription(description);
  }

  /**
   * Creates an error item based on a Throwable that was caught during the request processing.
   * <p>
   * The cause's stack trace is stored in the description of the error item.
   * </p>
   * 
   * @param severity
   * @param category
   * @param code
   * @param cause
   */
  public ErrorItemImpl(Severity severity, Category category, String code, String shortDescription, Throwable cause) {
    super();
    this.severity = severity;
    this.category = category;
    this.code = code;
    setShortDescription(shortDescription);
    setDescription(buildDescription(cause));
  }

  private String buildDescription(Throwable cause) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw, true);
    cause.printStackTrace(pw);
    return sw.toString();
  }

  public Category getCategory() {
    return category;
  }

  public String getCode() {
    return code;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  // limit the size to ensure it can be serialized in a context event,
  // where msg length is max 2000
  protected void setShortDescription(String shortDescr) {
    if (shortDescr != null && shortDescr.length() > 100) {
      this.shortDescription = shortDescr.substring(0, 100);
    } else {
      this.shortDescription = shortDescr;
    }
  }

  public String getDescription() {
    return description;
  }

  // limit the size to ensure it can be serialized in a context event,
  // where msg length is max 2000
  protected void setDescription(String descr) {
    if (descr != null && descr.length() > 1000) {
      this.description = descr.substring(0, 1000);
    } else {
      this.description = descr;
    }
  }

  public Severity getSeverity() {
    return severity;
  }

  public int compareTo(ErrorItem o) {
    if (o == this)
      return 0;
    if (o == null)
      return 1;

    int result = this.severity.compareTo(o.getSeverity());
    if (result == 0) {
      if (code == null || o.getCode() == null || (this.code.compareTo(o.getCode()) == 0)) {
        if (description == null || o.getDescription() == null)
          result = 1;
        else
          result = this.description.compareTo(o.getDescription());
      } else {
        result = this.code.compareTo(o.getCode());
      }
    }

    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((category == null) ? 0 : category.hashCode());
    result = prime * result + ((code == null) ? 0 : code.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((severity == null) ? 0 : severity.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ErrorItemImpl other = (ErrorItemImpl) obj;
    if (category == null) {
      if (other.category != null)
        return false;
    } else if (!category.equals(other.category))
      return false;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (severity == null) {
      if (other.severity != null)
        return false;
    } else if (!severity.equals(other.severity))
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ErrorItemImpl [severity=");
    builder.append(severity);
    builder.append(", category=");
    builder.append(category);
    builder.append(", code=");
    builder.append(code);
    builder.append(", short description=");
    builder.append(shortDescription);
    builder.append(", description=");
    builder.append(description);
    builder.append("]");
    return builder.toString();
  }
}
