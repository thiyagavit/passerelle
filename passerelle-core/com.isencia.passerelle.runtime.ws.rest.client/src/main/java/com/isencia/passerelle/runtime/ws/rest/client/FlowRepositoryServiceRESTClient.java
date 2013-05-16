/**
 * 
 */
package com.isencia.passerelle.runtime.ws.rest.client;

import java.util.Dictionary;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.DuplicateEntryException;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.ws.rest.CodeList;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

/**
 * @author erwin
 */
public class FlowRepositoryServiceRESTClient implements FlowRepositoryService {

  private final static Logger LOGGER = LoggerFactory.getLogger(FlowRepositoryServiceRESTClient.class);

  private boolean configured = false;

  private Client restClient;
  private WebResource flowReposResource;

  public void init(Dictionary<String, String> configuration) {
    try {
      String debugStr = configuration.get("com.isencia.passerelle.runtime.ws.rest.client.debug");
      String resourceURLStr = configuration.get("com.isencia.passerelle.runtime.ws.rest.client.resourceURL");
      boolean debug = Boolean.parseBoolean(debugStr);
      restClient = Client.create();
      if (debug) {
        restClient.addFilter(new LoggingFilter());
      }
      flowReposResource = restClient.resource(resourceURLStr);
      configured = true;
    } catch (Exception e) {
      configured = false;
      LOGGER.error(ErrorCode.SYSTEM_CONFIGURATION_ERROR.getFormattedCode() + " - Error configuring REST client", e);
    }
  }

  public boolean isConfigured() {
    return configured;
  }

  @Override
  public FlowHandle commit(Flow flow) throws DuplicateEntryException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FlowHandle commit(String flowCode, Flow flow) throws DuplicateEntryException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FlowHandle[] delete(String flowCode) throws EntryNotFoundException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FlowHandle update(FlowHandle handle, Flow updatedFlow, boolean activate) throws EntryNotFoundException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FlowHandle getActiveFlow(String flowCode) throws EntryNotFoundException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FlowHandle getMostRecentFlow(String flowCode) throws EntryNotFoundException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getAllFlowCodes() {
    CodeList codeList = flowReposResource.accept(MediaType.APPLICATION_JSON).get(CodeList.class);
    if (codeList != null && codeList.getCodes() != null) {
      return codeList.getCodes().toArray(new String[0]);
    } else {
      return new String[0];
    }
  }

  @Override
  public FlowHandle[] getAllFlowRevisions(String flowCode) throws EntryNotFoundException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FlowHandle activateFlowRevision(FlowHandle handle) throws EntryNotFoundException {
    // TODO Auto-generated method stub
    return null;
  }

}
