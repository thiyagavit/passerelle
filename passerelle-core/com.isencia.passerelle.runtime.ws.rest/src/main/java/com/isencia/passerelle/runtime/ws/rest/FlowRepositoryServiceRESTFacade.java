package com.isencia.passerelle.runtime.ws.rest;

import java.io.StringReader;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
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
 */
@Path("/flows")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class FlowRepositoryServiceRESTFacade {

  @Context
  UriInfo uriInfo;

  @GET
  public CodeList getAllFlowCodes() {
    return new CodeList(getFlowRepositoryService().getAllFlowCodes());
  }

  @GET
  @Path("/{code}")
  public FlowHandle getActiveFlow(@PathParam("code") String flowCode) throws EntryNotFoundException, InvalidRequestException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else {
      FlowHandle handle = getFlowRepositoryService().getActiveFlow(flowCode);
      if (uriInfo != null) {
        URI resLoc = uriInfo.getBaseUriBuilder().path(FlowRepositoryServiceRESTFacade.class).path("{code}").build(flowCode);
        return new FlowHandleResource(resLoc, handle.getCode(), handle.getRawFlowDefinition());
      } else {
        return new FlowHandleResource(handle);
      }
    }
  }

  @GET
  @Path("/{code}/mostRecent")
  public FlowHandle getMostRecentFlow(@PathParam("code") String flowCode) throws EntryNotFoundException, InvalidRequestException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else {
      return new FlowHandleResource(getFlowRepositoryService().getMostRecentFlow(flowCode));
    }
  }

  @GET
  @Path("/{code}/all")
  public FlowHandleResources getAllFlowRevisions(@PathParam("code") String flowCode) throws EntryNotFoundException, InvalidRequestException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else {
      return new FlowHandleResources(uriInfo.getBaseUriBuilder(), getFlowRepositoryService().getAllFlowRevisions(flowCode));
    }
  }

  @POST
  @Path("/{code}/activate")
  public FlowHandle activateFlowRevision(FlowHandle handle) throws EntryNotFoundException, InvalidRequestException {
    if (handle == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_CONTENT, "flow definition");
    } else {
      return getFlowRepositoryService().activateFlowRevision(handle);
    }
  }

  @POST
  @Path("/{code}")
  @Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML })
  public FlowHandle commit(@PathParam("code") String flowCode, String rawFlowDefinition) throws DuplicateEntryException, InvalidRequestException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else if (rawFlowDefinition == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_CONTENT, "flow definition");
    } else {
      Flow flow = null;
      try {
        flow = FlowManager.readMoml(new StringReader(rawFlowDefinition));
      } catch (Exception e) {
        throw new InvalidRequestException(ErrorCode.ERROR, "");
      }
      return new FlowHandleResource(getFlowRepositoryService().commit(flowCode, flow));
    }
  }

  @DELETE
  @Path("/{code}")
  public FlowHandleResources delete(@PathParam("code") String flowCode) throws InvalidRequestException, EntryNotFoundException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else {
      return new FlowHandleResources(uriInfo.getBaseUriBuilder(), getFlowRepositoryService().delete(flowCode));
    }
  }

  @PUT
  @Path("/{code}")
  @Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML })
  public FlowHandle update(FlowHandle handle, String rawFlowDefinition, boolean activate) {
    // TODO Auto-generated method stub
    return null;
  }

  public FlowRepositoryService getFlowRepositoryService() {
    return Activator.getInstance().getFlowReposSvc();
  }
}
