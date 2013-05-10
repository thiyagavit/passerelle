package com.isencia.passerelle.runtime.ws.rest;

import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.isencia.passerelle.runtime.FlowHandle;

@XmlRootElement(name="FlowHandles")
public class FlowHandleResources {
  
  private List<FlowHandle> flowHandles;

  
  public FlowHandleResources() {
  }

  public FlowHandleResources(FlowHandle... flowHandles) {
    this.flowHandles = Arrays.asList(flowHandles);
  }

  @XmlElement(type=FlowHandleResource.class, name="FlowHandle")
  public List<FlowHandle> getFlowHandles() {
    return flowHandles;
  }
}
