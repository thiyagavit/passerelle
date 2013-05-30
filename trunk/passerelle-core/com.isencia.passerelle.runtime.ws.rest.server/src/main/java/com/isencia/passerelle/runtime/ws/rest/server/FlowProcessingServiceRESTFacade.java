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
package com.isencia.passerelle.runtime.ws.rest.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.process.FlowProcessingService.StartMode;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.ws.rest.ErrorCode;
import com.isencia.passerelle.runtime.ws.rest.FlowHandleResource;
import com.isencia.passerelle.runtime.ws.rest.InvalidRequestException;
import com.isencia.passerelle.runtime.ws.rest.ProcessHandleResource;
import com.isencia.passerelle.runtime.ws.rest.server.activator.Activator;

/**
 * A REST service provider (or root resource) mapped on the FlowProcessingService interface.
 * <p>
 * </p>
 * 
 * @author erwin
 */
@Path("processes")
@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class FlowProcessingServiceRESTFacade {
  
  @GET
  public String getHello() {
    return "hello";
  }
  
  @POST
  @Path("{mode}")
  public ProcessHandle start(@PathParam("mode") String mode, FlowHandleResource flowHandle, @QueryParam("processContextId") String processContextId) throws EntryNotFoundException, InvalidRequestException {
    if (flowHandle == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_CONTENT, "flow definition");
    } if (mode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "mode");
    } else {
      try {
        // (re)load the flowhandle contents based on code and version
        // this allows to send compact handles around, without the complete raw flow definition
        FlowHandle handle = getFlowRepositoryService().loadFlowHandleWithContent(flowHandle);
        StartMode _mode = StartMode.valueOf(mode);
        ProcessHandle localHandle = getFlowProcessingService().start(_mode, handle, processContextId, null, null);
        return buildRemoteHandle(localHandle);
      } catch (IllegalArgumentException e) {
        throw new InvalidRequestException(ErrorCode.INVALID_PARAM, "mode");
      }
    }
  }

  private ProcessHandle buildRemoteHandle(ProcessHandle localHandle) {
    return new ProcessHandleResource(localHandle);
  }

  private FlowProcessingService getFlowProcessingService() {
    return Activator.getInstance().getFlowProcessingSvc();
  }
  private FlowRepositoryService getFlowRepositoryService() {
    return Activator.getInstance().getFlowReposSvc();
  }

}
