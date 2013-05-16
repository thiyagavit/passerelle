/**
 * 
 */
package com.isencia.passerelle.runtime.ws.rest.client;

import java.io.StringReader;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.VersionSpecification;
import com.isencia.passerelle.runtime.ws.rest.FlowHandleResource;

/**
 * @author erwin
 */
public class FlowHandleImpl implements FlowHandle {
  private final static Logger LOGGER = LoggerFactory.getLogger(FlowHandleImpl.class);

  private String code;
  private URI resourceLocation;
  private VersionSpecification version;
  private Flow flow;
  private String rawFlowDefinition;

  public FlowHandleImpl(String code, URI resourceLocation, VersionSpecification version, String moml) {
    this.code = code;
    this.resourceLocation = resourceLocation;
    this.version = version;
    this.rawFlowDefinition = moml;
  }
  
  public FlowHandleImpl(FlowHandleResource handleResource) {
    this(handleResource.getCode(), handleResource.getResourceLocation(), handleResource.getVersion(), handleResource.getRawFlowDefinition());
  }

  @Override
  public URI getResourceLocation() {
    return resourceLocation;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public VersionSpecification getVersion() {
    return version;
  }

  @Override
  public Flow getFlow() {
    if (flow == null) {
      try {
        flow = FlowManager.readMoml(new StringReader(rawFlowDefinition));
      } catch (Exception e) {
        LOGGER.error(ErrorCode.FLOW_LOADING_ERROR.getFormattedCode()+" - Error parsing flow from raw definition for "+getCode(), e);
      }
    }
    return flow;
  }

  @Override
  public String getRawFlowDefinition() {
    return rawFlowDefinition;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((code == null) ? 0 : code.hashCode());
    result = prime * result + ((resourceLocation == null) ? 0 : resourceLocation.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    FlowHandleImpl other = (FlowHandleImpl) obj;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    if (resourceLocation == null) {
      if (other.resourceLocation != null)
        return false;
    } else if (!resourceLocation.equals(other.resourceLocation))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "FlowHandleImpl [code=" + code + ", resourceLocation=" + resourceLocation + ", version=" + version + "]";
  }
}
