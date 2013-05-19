package com.isencia.passerelle.runtime.ws.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.isencia.passerelle.runtime.FlowHandle;

@XmlRootElement(name="FlowHandles")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlowHandleResources {
  
  @XmlElement(type=FlowHandleResource.class, name="FlowHandle")
  private List<FlowHandle> flowHandles;

  
  public FlowHandleResources() {
  }

  public FlowHandleResources(UriBuilder uriBldr, FlowHandle... flowHandles) {
    this.flowHandles = new ArrayList<FlowHandle>();
    for(FlowHandle handle : flowHandles) {
      FlowHandleResource fhRes = null;
      if (uriBldr != null) {
//        URI resLoc = uriBldr.clone().path("{code}").queryParam("version", "{version}").build(handle.getCode(), handle.getVersion());
        URI resLoc = uriBldr.clone().path("{code}").build(handle.getCode());
        fhRes = new FlowHandleResource(resLoc, handle.getCode(), handle.getRawFlowDefinition(), handle.getVersion());
      } else {
        fhRes = new FlowHandleResource(handle);
      }
      this.flowHandles.add(fhRes);
    }
  }

  public List<FlowHandle> getFlowHandles() {
    return flowHandles;
  }
}
