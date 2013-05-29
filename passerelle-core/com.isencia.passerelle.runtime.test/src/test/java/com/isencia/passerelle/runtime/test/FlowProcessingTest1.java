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
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowProcessingService;
import com.isencia.passerelle.runtime.FlowProcessingService.StartMode;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.FlowNotExecutingException;
import com.isencia.passerelle.runtime.process.ProcessStatus;
import com.isencia.passerelle.runtime.process.impl.FlowProcessingServiceImpl;
import com.isencia.passerelle.runtime.repos.impl.filesystem.FlowRepositoryServiceImpl;
import com.isencia.passerelle.runtime.repository.FlowHandle;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import com.isencia.passerelle.testsupport.actor.ExceptionGenerator;

public class FlowProcessingTest1 extends TestCase {

  private static final File userHome = new File(System.getProperty("user.home"));
  private static final File defaultRootFolderPath = new File(userHome, ".passerelle/passerelle-repository");
  private static final String REPOS_ROOTFOLDER = System.getProperty("com.isencia.passerelle.repository.root", defaultRootFolderPath.getAbsolutePath());

  public static FlowProcessingService processingService;
  public static FlowRepositoryService repositoryService;

  @Override
  protected void setUp() throws Exception {
    if (processingService == null) {
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
      // due to asynchronous stuff going on, file/folder locks may still hang around a bit,
      // so we need to be prepared to retry...
      boolean deleteOk = false;
      while(!deleteOk) {
        try {
          FileUtils.deleteDirectory(repositoryRootFolder);
          deleteOk = true;
        } catch (IOException e) {}
      }
      repositoryRootFolder.mkdirs();
    }
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

   public final void testStartAndCheckProcessHandle() throws Exception {
     FlowHandle flowHandle = repositoryService.commit("testStartAndCheckProcessHandle", buildTrivialFlow("testStartAndCheckProcessHandle"));
     ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
     assertNotNull("Process handle must be not-null", procHandle);
     assertNotNull("Process handle must have a non-null process context ID", procHandle.getProcessContextId());
     assertNotNull("Process status must be not-null", procHandle.getExecutionStatus());
     assertNotNull("Process's flow must be not-null", procHandle.getFlow());
     assertEquals("Process's flow code must be as defined", "testStartAndCheckProcessHandle", procHandle.getFlow().getCode());
   }

  public final void testStartFlowWithPreinitError() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testStartFlowWithPreinitError", buildPreInitErrorFlow("testStartFlowWithPreinitError"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    Thread.sleep(500);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should have finished in ERROR", ProcessStatus.ERROR, procHandle.getExecutionStatus());
  }

  public final void testStartFlowWithProcessError() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testStartFlowWithProcessError", buildProcessErrorFlow("testStartFlowWithProcessError"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    Thread.sleep(500);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should have finished in ERROR", ProcessStatus.ERROR, procHandle.getExecutionStatus());
  }

  public final void testStartFlowWithWrapupError() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testStartFlowWithWrapupError", buildWrapupErrorFlow("testStartFlowWithWrapupError"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    Thread.sleep(500);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should have finished in ERROR", ProcessStatus.ERROR, procHandle.getExecutionStatus());
  }

  public final void testGetHandle() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testGetHandle", buildDelay1sFlow("testGetHandle"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    ProcessHandle procHandle2 = processingService.getHandle(procHandle.getProcessContextId());
    assertEquals("Process handle from start() should be equal to one returned by getHandle()", procHandle, procHandle2);
  }

  public final void testRefresh() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testRefresh", buildTrivialFlow("testRefresh"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    assertNotNull("Process handle must be not-null", procHandle);
    Thread.sleep(500);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should have finished OK", ProcessStatus.FINISHED, procHandle.getExecutionStatus());
  }

  // with this one, we hope to invoke terminate before the actual execution has started
  public final void testTerminateImmediately() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testTerminateImmediately", buildDelay1sFlow("testTerminateImmediately"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    ProcessHandle procHandle2 = processingService.terminate(procHandle);
    // this is a bit risky, as we can not strictly be certain that the process was indeed terminated/canceled before its start...
    assertTrue("Process should have terminated", procHandle2.getExecutionStatus().isFinalStatus());
    // then we just let it die
    Thread.sleep(200);
    ProcessHandle procHandle3 = processingService.refresh(procHandle2);
    assertTrue("Process should have terminated", procHandle3.getExecutionStatus().isFinalStatus());
  }

  // with this one, we hope to invoke terminate when the actual execution has started
  public final void testTerminateAfterSomeTime() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testTerminateAfterSomeTime", buildDelay1sFlow("testTerminateAfterSomeTime"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    Thread.sleep(100);
    ProcessHandle procHandle2 = processingService.refresh(procHandle);
    assertEquals("Process should have started", ProcessStatus.ACTIVE, procHandle2.getExecutionStatus());
    processingService.terminate(procHandle);
    // then we just let it die
    Thread.sleep(1000);
    ProcessHandle procHandle3 = processingService.refresh(procHandle2);
    assertTrue("Process should have terminated", procHandle3.getExecutionStatus().isFinalStatus());
  }

  public final void testWaitUntilFinished() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testWaitUntilFinished", buildDelay1sFlow("testWaitUntilFinished"));
    long startTime = new Date().getTime();
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    ProcessStatus status = procHandle.waitUntilFinished(3, TimeUnit.SECONDS);
    long endTime = new Date().getTime();
    assertTrue("Process should have terminated", status.isFinalStatus());
    assertTrue("Process should last for at least 1s", (endTime - startTime) > 1000);
  }

  public final void testWaitForTerminatedProcess() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testWaitForTerminatedProcess", buildDelay1sFlow("testWaitForTerminatedProcess"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    processingService.terminate(procHandle);
    try {
      ProcessStatus status = procHandle.waitUntilFinished(3, TimeUnit.SECONDS);
      assertEquals("Process should be INTERRUPTED", ProcessStatus.INTERRUPTED, status);
    } catch (FlowNotExecutingException e) {
      // this is also possible
    }
  }

  public final void testWaitNotLongEnoughUntilFinished() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testWaitNotLongEnoughUntilFinished", buildDelay1sFlow("testWaitNotLongEnoughUntilFinished"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    try {
      procHandle.waitUntilFinished(1, TimeUnit.SECONDS);
      fail("waitUntilFinished should have gone in timeout");
    } catch (TimeoutException e) {
      // this is as it should be
    }
  }

  public final void testWaitUntilFinishedOfFlowWithError() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testWaitUntilFinishedOfFlowWithError", buildPreInitErrorFlow("testWaitUntilFinishedOfFlowWithError"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    try {
      procHandle.waitUntilFinished(3, TimeUnit.SECONDS);
      fail("Process should have caused an ExecutionException");
    } catch (ExecutionException e) {
      assertTrue("Error cause should be an InitializationException", e.getCause() instanceof InitializationException);
    }
  }
  
  public final void testStartWithParameterOverrides() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testStartWithParameterOverrides", buildDelay1sFlow("testStartWithParameterOverrides"));
    Map<String,String> overrides = new HashMap<String, String>();
    overrides.put("delay.time(s)", "3");
    long startTime = new Date().getTime();
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, overrides, null);
    ProcessStatus status = procHandle.waitUntilFinished(5, TimeUnit.SECONDS);
    long endTime = new Date().getTime();
    assertTrue("Process should have terminated", status.isFinalStatus());
    assertTrue("Process should last for at least 1s", (endTime - startTime) > 3000);
  }

  public final void testSuspendResume() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testSuspendResume", buildMultiDelay1sFlow("testSuspendResume"));
    ProcessHandle procHandle = processingService.start(StartMode.NORMAL, flowHandle, null, null, null);
    //an immediate suspend could happen BEFORE the model is already executing (as the start is an asynchronous)
    //this is OK as the processingService will maintain the suspension indicator and will suspend the execution
    //as soon as possible after its actual start.
    ProcessHandle suspendHandle = processingService.suspend(procHandle);
    // need to wait > 1s here as the Delay actor in the test model REALLY blocks for its configured delay (1s)
    Thread.sleep(1100);
    suspendHandle = processingService.refresh(suspendHandle);
    assertEquals("Process should be SUSPENDED", ProcessStatus.SUSPENDED, suspendHandle.getExecutionStatus());
    ProcessHandle resumeHandle = processingService.resume(suspendHandle);
    Thread.sleep(100);
    resumeHandle = processingService.refresh(resumeHandle);
    assertEquals("Process should be RESUMED", ProcessStatus.ACTIVE, resumeHandle.getExecutionStatus());
    resumeHandle.waitUntilFinished(5, TimeUnit.SECONDS);
  }  

  // public final void testGetProcessEventsProcessHandleInt() {
  // fail("Not yet implemented"); // TODO
  // }
  //
  // public final void testGetProcessEventsStringInt() {
  // fail("Not yet implemented"); // TODO
  // }

  protected Flow buildTrivialFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new Director(flow, "director"));
    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, sink);
    return flow;
  }

  protected Flow buildPreInitErrorFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new Director(flow, "director"));
    Const source = new Const(flow, "Constant");
    ExceptionGenerator excGen = new ExceptionGenerator(flow, "excGen");
    excGen.preInitExcParameter.setExpression("true");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, excGen);
    flow.connect(excGen, sink);
    return flow;
  }

  protected Flow buildProcessErrorFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new Director(flow, "director"));
    Const source = new Const(flow, "Constant");
    ExceptionGenerator excGen = new ExceptionGenerator(flow, "excGen");
    excGen.processExcParameter.setExpression("true");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, excGen);
    flow.connect(excGen, sink);
    return flow;
  }

  protected Flow buildWrapupErrorFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new Director(flow, "director"));
    Const source = new Const(flow, "Constant");
    ExceptionGenerator excGen = new ExceptionGenerator(flow, "excGen");
    excGen.wrapupExcParameter.setExpression("true");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, excGen);
    flow.connect(excGen, sink);
    return flow;
  }

  protected Flow buildDelay1sFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new Director(flow, "director"));
    Const source = new Const(flow, "Constant");
    Delay delay = new Delay(flow, "delay");
    delay.timeParameter.setExpression("1");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, delay);
    flow.connect(delay, sink);
    return flow;
  }

  protected Flow buildMultiDelay1sFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new Director(flow, "director"));
    Const source = new Const(flow, "Constant");
    Delay delay1 = new Delay(flow, "delay1");
    Delay delay2 = new Delay(flow, "delay2");
    Delay delay3 = new Delay(flow, "delay3");
    delay1.timeParameter.setExpression("1");
    delay2.timeParameter.setExpression("1");
    delay3.timeParameter.setExpression("1");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, delay1);
    flow.connect(delay1, delay2);
    flow.connect(delay2, delay3);
    flow.connect(delay3, sink);
    return flow;
  }

}
