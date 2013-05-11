package com.isencia.passerelle.runtime.ws.rest;

import java.net.URL;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.VersionSpecification;

@XmlRootElement(name="FlowHandle")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlowHandleResource implements FlowHandle {

  private URL resourceLocation;
  private String code;
  private String rawFlowDefinition;
  
  public FlowHandleResource() {
    
  }
  
  public FlowHandleResource(FlowHandle handle) {
    this(handle.getResourceLocation(), handle.getCode(), handle.getRawFlowDefinition());
  }
  
  public FlowHandleResource(URL resourceLocation, String code, String rawFlowDefinition) {
    super();
    this.resourceLocation = resourceLocation;
    this.code = code;
    this.rawFlowDefinition = rawFlowDefinition;
  }

  @Override
  public URL getResourceLocation() {
    return resourceLocation;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public VersionSpecification getVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Flow getFlow() {
    return null;
  }

  @Override
  public String getRawFlowDefinition() {
    return rawFlowDefinition;
  }

  @Override
  public String toString() {
    return "FlowHandleResource [resourceLocation=" + resourceLocation + ", code=" + code + ", rawFlowDefinition=" + rawFlowDefinition + "]";
  }
}
