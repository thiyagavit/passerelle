/* Copyright 2012 - iSencia Belgium NV

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
package com.isencia.passerelle.actor.test;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.isencia.passerelle.actor.error.ErrorCatcher;
import com.isencia.passerelle.actor.general.CommandExecutor;
import com.isencia.passerelle.actor.general.Const;
import com.isencia.passerelle.actor.general.DevNullActor;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.MessageHistoryStack;

public class ActorTest extends TestCase {
  private Flow flow;
  private FlowManager flowMgr;

  protected void setUp() throws Exception {
    flow = new Flow("actors unit test", null);
    flowMgr = new FlowManager();
  }

  /**
   * A unit test for a plain HelloPasserelle model.
   * 
   * @throws Exception
   */
  public void testHelloPasserelle() throws Exception {
    flow.setDirector(new Director(flow, "director"));

    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");

    flow.connect(source, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
  }

  public void testCommandExecutor() throws Exception {
    flow.setDirector(new Director(flow, "director"));

    CommandExecutor cmdExecutor = new CommandExecutor(flow, "cmdExecutor");
    MessageHistoryStack cmdStdOutSink = new MessageHistoryStack(flow, "cmdStdOutSink");
    MessageHistoryStack cmdStdErrSink = new MessageHistoryStack(flow, "cmdStdErrSink");
    MessageHistoryStack cmdExitErrorSink = new MessageHistoryStack(flow, "cmdExitErrorSink");
    ErrorCatcher errorCatcher = new ErrorCatcher(flow, "errorCatcher");

    flow.connect(cmdExecutor.cmdOut, cmdStdOutSink.input);
    flow.connect(cmdExecutor.cmdErr, cmdStdErrSink.input);
    flow.connect(cmdExecutor.errorPort, errorCatcher.input);
    flow.connect(errorCatcher.errorDescrOutput, cmdExitErrorSink.input);

    Map<String, String> props = new HashMap<String, String>();
    props.put("cmdExecutor.command", "runEchoes.bat");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(cmdExecutor.cmdOut, 2L)
    .expectMsgSentCount(cmdExecutor.cmdErr, 1L)
    .expectMsgSentCount(cmdExecutor.errorPort, 0L)
    .assertFlow(flow);
    
    assertEquals("Wrong last stdOut", "and now for something completely different", cmdStdOutSink.poll().getBodyContentAsString());
    assertEquals("Wrong first stdOut", "this is a first output to stdout", cmdStdOutSink.poll().getBodyContentAsString());
    assertEquals("Wrong stdErr", "some error msg", cmdStdErrSink.poll().getBodyContentAsString());
  }

  public void testCommandExecutorWithErrorExit() throws Exception {
    flow.setDirector(new Director(flow, "director"));

    CommandExecutor cmdExecutor = new CommandExecutor(flow, "cmdExecutor");
    MessageHistoryStack cmdStdOutSink = new MessageHistoryStack(flow, "cmdStdOutSink");
    MessageHistoryStack cmdStdErrSink = new MessageHistoryStack(flow, "cmdStdErrSink");
    MessageHistoryStack cmdExitErrorSink = new MessageHistoryStack(flow, "cmdExitErrorSink");
    ErrorCatcher errorCatcher = new ErrorCatcher(flow, "errorCatcher");

    flow.connect(cmdExecutor.cmdOut, cmdStdOutSink.input);
    flow.connect(cmdExecutor.cmdErr, cmdStdErrSink.input);
    flow.connect(cmdExecutor.errorPort, errorCatcher.input);
    flow.connect(errorCatcher.errorDescrOutput, cmdExitErrorSink.input);

    Map<String, String> props = new HashMap<String, String>();
    props.put("cmdExecutor.command", "runEchoesWithErrorExit.bat");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(cmdExecutor.cmdOut, 2L)
    .expectMsgSentCount(cmdExecutor.cmdErr, 1L)
    .expectMsgSentCount(cmdExecutor.errorPort, 1L)
    .expectMsgReceiptCount(cmdExitErrorSink.input, 1L)
    .assertFlow(flow);
    
    assertTrue("Wrong exit code", cmdExitErrorSink.poll().getBodyContentAsString().contains("Exit : 13"));
  }
}