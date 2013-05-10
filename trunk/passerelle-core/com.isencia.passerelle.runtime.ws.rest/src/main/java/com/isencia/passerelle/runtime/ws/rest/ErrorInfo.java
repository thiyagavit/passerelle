/**
 * 
 */
package com.isencia.passerelle.runtime.ws.rest;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import com.isencia.passerelle.core.ErrorCode.Severity;

/**
 * @author erwin
 */
@XmlRootElement(name = "ErrorInfo")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorInfo {

  private Severity severity;
  private String code;
  private String description;
  private String extraInfo;

  protected ErrorInfo() {
  }

  public ErrorInfo(Severity severity, String code, String description, String extraInfo) {
    super();
    this.severity = severity;
    this.code = code;
    setExtraInfo(extraInfo);
    setDescription(description);
  }

  public String getCode() {
    return code;
  }

  public String getExtraInfo() {
    return extraInfo;
  }

  protected void setExtraInfo(String extraInfo) {
    this.extraInfo = extraInfo;
  }

  public String getDescription() {
    return description;
  }

  protected void setDescription(String descr) {
    this.description = descr;
  }

  public Severity getSeverity() {
    return severity;
  }
}
