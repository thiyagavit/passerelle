package com.isencia.passerelle.runtime.test.activator;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.impl.mock.FlowRepositoryServiceImpl;
import com.isencia.passerelle.runtime.repository.DuplicateEntryException;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.repository.VersionSpecification;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.DevNullActor;

public class FlowRepositoryTest1 extends TestCase {

  private static final String HELLO_WORLD_FLOWNAME = "HelloWorld";
  private static final String HELLO_CODE = "HELLO";
  private static final String HELLO_CODE2 = "HELLO2";
  private static final String HELLO_CODE3 = "HELLO3";
  
  private static final String REPOS_ROOTFOLDER = "/C:/temp/testFlowRepos";
  FlowRepositoryService repositoryService;
  File repositoryRootFolder;
  
  @Override
  protected void setUp() throws Exception {
    repositoryRootFolder = new File(REPOS_ROOTFOLDER);
    FileUtils.deleteDirectory(repositoryRootFolder);
    repositoryService = new FlowRepositoryServiceImpl(repositoryRootFolder);
  }
  
  public void testCommitFlowForCode() throws Exception {
    try {
      FlowHandle handle = repositoryService.commit(HELLO_CODE, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
      assertNotNull("A non-null handle should be returned", handle);
      assertEquals("Wrong flow code", HELLO_CODE, handle.getCode());
      assertEquals("Wrong version spec", VersionSpecification.parse("1.0.0"), handle.getVersion());
      assertNotNull("Flow's commit resource location should be not-null", handle.getResourceLocation());
      String resourceLocationAsString = handle.getResourceLocation().getPath().toString();
      assertTrue("resource location "+resourceLocationAsString+" must be inside repo root folder "+REPOS_ROOTFOLDER, resourceLocationAsString.startsWith(REPOS_ROOTFOLDER));
      assertEquals("Wrong flow name", HELLO_WORLD_FLOWNAME, handle.getFlow().getName());
      assertNotNull("Committed flow shas lost its director", handle.getFlow().getDirector());
      assertNotNull("MOML should be not null", handle.getRawFlowDefinition());
      assertFalse("MOML should be not empty", handle.getRawFlowDefinition().isEmpty());
    } catch (DuplicateEntryException e) {
      fail("First flow commit should not fail with DuplicatEntryException");
    } 
  }

  public void testDelete() throws Exception {
    FlowHandle handle = repositoryService.commit(HELLO_CODE, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    FlowHandle[] handles = repositoryService.delete(HELLO_CODE);
    assertNotNull("Handles from delete() should be not-null", handles);
    assertEquals("Handles from delete() should contain one entry", 1, handles.length);
    assertEquals("Handles from delete() should contain committed flow", handle, handles[0]);
    try {
      repositoryService.getAllFlowRevisions(HELLO_CODE);
      fail("After deletion, no revisions should be found anymore");
    } catch (EntryNotFoundException e) {
      // this is what is expected
    }
  }

  public void testGetActiveFlow() throws Exception {
    FlowHandle commitHandle = repositoryService.commit(HELLO_CODE, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    FlowHandle activeHandle = repositoryService.getActiveFlow(HELLO_CODE);
    
    assertEquals("Committed flow not returned as active flow", commitHandle, activeHandle);
  }

  public void testMostRecentFlowAfterCommit() throws Exception {
    FlowHandle commitHandle = repositoryService.commit(HELLO_CODE, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    FlowHandle mostRecentHandle = repositoryService.getMostRecentFlow(HELLO_CODE);
    
    assertEquals("Committed flow not returned as most recent flow", commitHandle, mostRecentHandle);
  }
  
  public void testMostRecentAfterUpdateWithActivate() throws Exception {
    FlowHandle commitHandle = repositoryService.commit(HELLO_CODE, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    
    Flow f = commitHandle.getFlow();
    Map<String, String> paramOverrides = new HashMap<String, String>();
    paramOverrides.put("Constant.value", "changed");
    FlowManager.applyParameterSettings(f, paramOverrides );
    
    FlowHandle updatedHandle = repositoryService.update(commitHandle, f, true);
    FlowHandle mostRecentHandle = repositoryService.getMostRecentFlow(HELLO_CODE);
    
    assertFalse("Most recent handle should not be the originally committed one", mostRecentHandle.equals(commitHandle));
    assertEquals("Most recent handle should be the updated one", updatedHandle, mostRecentHandle);
  }

  public void testMostRecentAfterUpdateWithoutActivate() throws Exception {
    FlowHandle commitHandle = repositoryService.commit(HELLO_CODE, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    
    Flow f = commitHandle.getFlow();
    Map<String, String> paramOverrides = new HashMap<String, String>();
    paramOverrides.put("Constant.value", "changed");
    FlowManager.applyParameterSettings(f, paramOverrides );
    
    FlowHandle updatedHandle = repositoryService.update(commitHandle, f, false);
    FlowHandle mostRecentHandle = repositoryService.getMostRecentFlow(HELLO_CODE);
    
    assertFalse("Most recent handle should not be the originally committed one", mostRecentHandle.equals(commitHandle));
    assertEquals("Most recent handle should be the updated one", updatedHandle, mostRecentHandle);
  }

  public void testGetAllFlowCodes() throws Exception {
    repositoryService.commit(HELLO_CODE, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    repositoryService.commit(HELLO_CODE2, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    repositoryService.commit(HELLO_CODE3, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    repositoryService.commit(buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    
    String[] allFlowCodes = repositoryService.getAllFlowCodes();
    assertEquals("Repository should know 4 codes", 4, allFlowCodes.length);
    List<String> codesAsList = Arrays.asList(allFlowCodes);
    assertTrue("Repository should know "+HELLO_CODE, codesAsList.contains(HELLO_CODE));
    assertTrue("Repository should know "+HELLO_CODE2, codesAsList.contains(HELLO_CODE2));
    assertTrue("Repository should know "+HELLO_CODE3, codesAsList.contains(HELLO_CODE3));
    assertTrue("Repository should know "+HELLO_WORLD_FLOWNAME, codesAsList.contains(HELLO_WORLD_FLOWNAME));
  }

  public void testUpdateAndActivation() throws Exception {
    repositoryService.commit(HELLO_CODE, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    FlowHandle activeHandle = repositoryService.getActiveFlow(HELLO_CODE);
    
    Flow f = activeHandle.getFlow();
    Map<String, String> paramOverrides = new HashMap<String, String>();
    paramOverrides.put("Constant.value", "changed");
    FlowManager.applyParameterSettings(f, paramOverrides );
    
    FlowHandle updatedHandle = repositoryService.update(activeHandle, f, true);
    
    assertFalse("Updated handle should not be the previously active one", activeHandle.equals(updatedHandle));
    assertEquals("Code should remain the same for an update", activeHandle.getCode(), updatedHandle.getCode());
    assertTrue("Version must have increased after update", updatedHandle.getVersion().compareTo(activeHandle.getVersion())>0);
  }

  public void testUpdateWithoutActivation() throws Exception {
    repositoryService.commit(HELLO_CODE, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    FlowHandle activeHandle = repositoryService.getActiveFlow(HELLO_CODE);
    
    Flow f = activeHandle.getFlow();
    Map<String, String> paramOverrides = new HashMap<String, String>();
    paramOverrides.put("Constant.value", "changed");
    FlowManager.applyParameterSettings(f, paramOverrides );
    
    FlowHandle updatedHandle = repositoryService.update(activeHandle, f, false);
    FlowHandle activeHandle2 = repositoryService.getActiveFlow(HELLO_CODE);
    
    assertFalse("Updated handle should not be the previously active one", activeHandle.equals(updatedHandle));
    assertEquals("Code should remain the same for an update", activeHandle.getCode(), updatedHandle.getCode());
    assertTrue("Version must have increased after update", updatedHandle.getVersion().compareTo(activeHandle.getVersion())>0);
    assertEquals("Active flow should not have changed", activeHandle, activeHandle2);
  }

  public Flow buildTrivialFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new Director(flow, "director"));
    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, sink);

    return flow;
  }

}
