package com.isencia.passerelle.runtime.ws.rest;

import java.io.StringReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.DuplicateEntryException;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.ws.rest.activator.Activator;

/**
 * A REST service provider (or root resource) mapped on the FlowRepositoryService interface.
 * <p>
 * </p>
 * 
 * @author erwin
 *
 */
@Path("/flows")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class RestTest {
  
  public FlowHandle[] delete(String flowCode) {
    // TODO Auto-generated method stub
    return null;
  }
  
  
  @GET
  @Path("/activeFlow")
  public FlowHandle getActiveFlow(@QueryParam("code") String flowCode) throws EntryNotFoundException {
      return new FlowHandleResource(getFlowRepositoryService().getActiveFlow(flowCode));
  }
  
  @GET
  @Path("/mostRecentFlow")
  public FlowHandle getMostRecentFlow(@QueryParam("code") String flowCode) throws EntryNotFoundException, InvalidRequestException {
    return new FlowHandleResource(getFlowRepositoryService().getMostRecentFlow(flowCode));
  }

  @GET
  @Path("/allCodes")
  public CodeList getAllFlowCodes() {
    return new CodeList(getFlowRepositoryService().getAllFlowCodes());
  }

  @GET
  @Path("/allFlows")
  public FlowHandleResources getAllFlowRevisions(@QueryParam("code") String flowCode) throws EntryNotFoundException {
    return new FlowHandleResources(getFlowRepositoryService().getAllFlowRevisions(flowCode));
  }

  public FlowHandle activateFlowRevision(FlowHandle handle) {
    // TODO Auto-generated method stub
    return null;
  }
// it seems difficult to differentiate a rest method with/without query param but with same content type
// so we drop this variation. the client side should always specify the flowCode.
//  @POST
//  @Consumes({MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
//  public FlowHandle commit(String rawFlowDefinition) throws InvalidRequestException, DuplicateEntryException {
//    Flow flow = null;
//    try {
//      flow = FlowManager.readMoml(new StringReader(rawFlowDefinition));
//    } catch (Exception e) {
//      throw new InvalidRequestException(ErrorCode.ERROR, "");
//    }
//    return new FlowHandleResource(getFlowRepositoryService().commit(flow));
//  }
//
  @POST
  @Consumes({MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
  public FlowHandle commit(@QueryParam("code") String flowCode, String rawFlowDefinition) throws DuplicateEntryException, InvalidRequestException {
    Flow flow = null;
    try {
      flow = FlowManager.readMoml(new StringReader(rawFlowDefinition));
    } catch (Exception e) {
      throw new InvalidRequestException(ErrorCode.ERROR, "");
    }
    return new FlowHandleResource(getFlowRepositoryService().commit(flowCode, flow));
  }

  public FlowHandle update(FlowHandle handle, Flow updatedFlow, boolean activate) {
    // TODO Auto-generated method stub
    return null;
  }

  public FlowRepositoryService getFlowRepositoryService() {
    return Activator.getInstance().getFlowReposSvc();
  }
}
