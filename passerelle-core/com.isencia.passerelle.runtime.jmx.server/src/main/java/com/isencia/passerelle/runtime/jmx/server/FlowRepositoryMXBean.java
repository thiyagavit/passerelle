package com.isencia.passerelle.runtime.jmx.server;

import com.isencia.passerelle.runtime.jmx.FlowHandleBean;
import com.isencia.passerelle.runtime.repository.DuplicateEntryException;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;


public interface FlowRepositoryMXBean {
  
  String[] getAllFlowCodes();
  
  FlowHandleBean getActiveFlow(String flowCode) throws EntryNotFoundException;

  FlowHandleBean getMostRecentFlow(String flowCode) throws EntryNotFoundException;

  FlowHandleBean[] getAllFlowRevisions(String flowCode) throws EntryNotFoundException;
  
  FlowHandleBean activateFlowRevision(FlowHandleBean handle) throws EntryNotFoundException;
  
  FlowHandleBean commit(String flowCode, String rawFlowDefinition) throws DuplicateEntryException;

  FlowHandleBean[] delete(String flowCode) throws EntryNotFoundException;

  FlowHandleBean update(String flowCode, String rawFlowDefinition, boolean activate) throws EntryNotFoundException;

}
