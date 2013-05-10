package com.isencia.passerelle.runtime.ws.rest;

import java.net.MalformedURLException;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.sun.jersey.api.ParamException;

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
  public FlowHandle getActiveFlow(@QueryParam("code") String flowCode) {
    try {
      return new FlowHandleResource(new URL("http://localhost/flows/flow/hello"), "hello", "hello some text");
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  @GET
  @Path("/mostRecentFlow")
  public FlowHandle getMostRecentFlow(@QueryParam("code") String flowCode) throws EntryNotFoundException, InvalidRequestException {
    if(flowCode!=null) {
      throw new EntryNotFoundException("Flow not found for "+flowCode);
    } else {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    }
  }

  @GET
  @Path("/allCodes")
  public String[] getAllFlowCodes() {
    return new String[] {"hello", "world"};
  }

  @GET
  @Path("/allFlows")
  public FlowHandleResources getAllFlowRevisions(@QueryParam("code") String flowCode) {
    try {
      return new FlowHandleResources(new FlowHandleResource(new URL("http://localhost/flows/flow/hello"), "hello", "hello some text"),
          new FlowHandleResource(new URL("http://localhost/flows/flow/hello2"), "hello2", "hello2 some text"));
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public FlowHandle activateFlowRevision(FlowHandle handle) {
    // TODO Auto-generated method stub
    return null;
  }

  public FlowHandle commit(Flow flow) {
    // TODO Auto-generated method stub
    return null;
  }

  public FlowHandle commit(String flowCode, Flow flow) {
    // TODO Auto-generated method stub
    return null;
  }

  public FlowHandle update(FlowHandle handle, Flow updatedFlow, boolean activate) {
    // TODO Auto-generated method stub
    return null;
  }
}
