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
package com.isencia.passerelle.runtime.test;

import java.io.File;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.FlowProcessingService;
import com.isencia.passerelle.runtime.FlowProcessingService.StartMode;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.impl.FlowProcessingServiceImpl;
import com.isencia.passerelle.runtime.repos.impl.filesystem.FlowRepositoryServiceImpl;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.DevNullActor;

public class FlowProcessingTest1 extends TestCase {
  private static final String HELLO_WORLD_FLOWNAME = "HelloWorld";
  private static final String HELLO_CODE = "HELLO";

  private static final File userHome              = new File(System.getProperty("user.home"));
  private static final File defaultRootFolderPath = new File(userHome, ".passerelle/passerelle-repository");
  private static final String REPOS_ROOTFOLDER      = System.getProperty("com.isencia.passerelle.repository.root", defaultRootFolderPath.getAbsolutePath());

  public static FlowProcessingService processingService;
  public static FlowRepositoryService repositoryService;

  @Override
  protected void setUp() throws Exception {
    if(processingService==null) {
      processingService = new FlowProcessingServiceImpl(3);
    }
    if (repositoryService == null) {
      File repositoryRootFolder = new File(REPOS_ROOTFOLDER);
      FileUtils.deleteDirectory(repositoryRootFolder);
      repositoryService = new FlowRepositoryServiceImpl(repositoryRootFolder);
    } else {
      // this is a bit of a hack, assuming that when we run this test on a REST client facade,
      // the system property for the backing repos root folder has been set to the same location
      // for the server-side and this test-client-side.
      // we need to ensure the repos is cleared before each test, to avoid DuplicateEntryExceptions....
      File repositoryRootFolder = new File(REPOS_ROOTFOLDER);
      FileUtils.deleteDirectory(repositoryRootFolder);
      repositoryRootFolder.mkdirs();
    }
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public final void testStartAndCheckProcessHandle() throws Exception {
    FlowHandle flowHandle = repositoryService.commit(HELLO_CODE, buildTrivialFlow(HELLO_WORLD_FLOWNAME));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    assertNotNull("Process handle must be not-null", procHandle);
    assertNotNull("Process handle must have a non-null process context ID", procHandle.getProcessContextId());
    assertNotNull("Process status must be not-null", procHandle.getExecutionStatus());
    assertNotNull("Process's flow must be not-null", procHandle.getFlow());
    assertEquals("Process's flow code must be as defined", HELLO_CODE, procHandle.getFlow().getCode());
    // then we just let it die
  }

  public final void testGetHandle() {
    fail("Not yet implemented"); // TODO
  }

  public final void testRefresh() {
    fail("Not yet implemented"); // TODO
  }

  public final void testTerminate() {
    fail("Not yet implemented"); // TODO
  }

  public final void testGetProcessEventsProcessHandleInt() {
    fail("Not yet implemented"); // TODO
  }

  public final void testGetProcessEventsStringInt() {
    fail("Not yet implemented"); // TODO
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
