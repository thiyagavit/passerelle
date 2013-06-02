/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.runtime.ws.rest.client;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.runtime.Event;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.FlowNotExecutingException;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.process.ProcessListener;
import com.isencia.passerelle.runtime.ws.rest.ErrorInfo;
import com.isencia.passerelle.runtime.ws.rest.FlowHandleResource;
import com.isencia.passerelle.runtime.ws.rest.ProcessHandleResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

/**
 * @author erwin
 */
public class FlowProcessingServiceRESTClient implements FlowProcessingService {

  private final static Logger LOGGER = LoggerFactory.getLogger(FlowProcessingServiceRESTClient.class);

  private boolean configured = false;

  private Client restClient;
  private WebResource flowProcResource;

  public void init(Dictionary<String, String> configuration) {
    try {
      String debugStr = configuration.get("debug");
      String resourceURLStr = configuration.get("resourceURL");
      boolean debug = Boolean.parseBoolean(debugStr);
      restClient = Client.create();
      if (debug) {
        restClient.addFilter(new LoggingFilter());
      }
      flowProcResource = restClient.resource(resourceURLStr);
      configured = true;
    } catch (Exception e) {
      configured = false;
      LOGGER.error(ErrorCode.SYSTEM_CONFIGURATION_ERROR.getFormattedCode() + " - Error configuring REST client", e);
    }
  }

  public boolean isConfigured() {
    return configured;
  }

  // TODO finish this : handle all arguments ; better error handling
  @Override
  public ProcessHandle start(StartMode mode, FlowHandle flowHandle, String processContextId, Map<String, String> parameterOverrides, ProcessListener listener,
      String... breakpointNames) {
    try {
      return flowProcResource.path(mode.name()).type(MediaType.APPLICATION_XML).post(ProcessHandleResource.class, FlowHandleResource.buildFlowHandleResource(flowHandle));
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new IllegalArgumentException();
    }
  }
  
  // TODO finish this : better error handling
  @Override
  public ProcessHandle getHandle(String processId) {
    try {
      return flowProcResource.path(processId).type(MediaType.APPLICATION_XML).get(ProcessHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new IllegalArgumentException();
    }
  }
  
  @Override
  public ProcessHandle refresh(ProcessHandle processHandle) {
    return getHandle(processHandle.getProcessContextId());
  }

  @Override
  public ProcessHandle terminate(ProcessHandle processHandle) throws FlowNotExecutingException {
    try {
      return flowProcResource.path(processHandle.getProcessContextId()).delete(ProcessHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new FlowNotExecutingException(processHandle.getProcessContextId());
    }
  }

  @Override
  public ProcessHandle suspend(ProcessHandle processHandle) throws FlowNotExecutingException {
    try {
      return flowProcResource.path(processHandle.getProcessContextId()).path("suspend").post(ProcessHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new FlowNotExecutingException(processHandle.getProcessContextId());
    }
  };
  
  @Override
  public ProcessHandle resume(ProcessHandle processHandle) throws FlowNotExecutingException {
    try {
      return flowProcResource.path(processHandle.getProcessContextId()).path("resume").post(ProcessHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new FlowNotExecutingException(processHandle.getProcessContextId());
    }
  }

  // TODO implement local resume, once this is supported
  @Override
  public ProcessHandle resume(ProcessHandle processHandle, String suspendedElement) throws FlowNotExecutingException {
    return resume(processHandle);
  }

  @Override
  public ProcessHandle step(ProcessHandle processHandle) throws FlowNotExecutingException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcessHandle addBreakpoints(ProcessHandle processHandle, String... extraBreakpoints) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcessHandle removeBreakpoints(ProcessHandle processHandle, String... breakpointsToRemove) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcessHandle signalEvent(ProcessHandle processHandle, Event event) throws FlowNotExecutingException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Event> getProcessEvents(ProcessHandle processHandle, int maxCount) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Event> getProcessEvents(String processId, int maxCount) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  };
}
