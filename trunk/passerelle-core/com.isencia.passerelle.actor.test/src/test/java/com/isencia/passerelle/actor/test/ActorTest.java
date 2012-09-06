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

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import ptolemy.actor.Manager;
import ptolemy.actor.Manager.State;
import com.isencia.passerelle.actor.control.Stop;
import com.isencia.passerelle.actor.control.Trigger;
import com.isencia.passerelle.actor.error.ErrorCatcher;
import com.isencia.passerelle.actor.error.ErrorObserver;
import com.isencia.passerelle.actor.general.CommandExecutor;
import com.isencia.passerelle.actor.general.Const;
import com.isencia.passerelle.actor.general.DevNullActor;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.FlowNotExecutingException;
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
   * A unit test for a plain model with Error Observer
   * This model should never stop by itself.
   * 
   * @throws Exception
   */
  public void testFlowWithErrorObserver() throws Exception {
    flow.setDirector(new Director(flow, "director"));

    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    ErrorObserver errObs = new ErrorObserver(flow, "errObs");
    
    flow.connect(source, sink);
    flow.connect(errObs.messageInErrorOutput, sink.input);

    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    // launch the flow in a background thread
    flowMgr.execute(flow, props);
    // now wait a while
    Thread.sleep(5000);
    // the flow will still be running
    try {
      State state = flowMgr.getLocalExecutionState(flow);
      assertEquals("Flow must still be iterating", Manager.ITERATING, state);
      flowMgr.stopExecution(flow, 1000);
      
      new FlowStatisticsAssertion()
      .expectMsgSentCount(source, 1L)
      .expectMsgReceiptCount(sink, 1L)
      .assertFlow(flow);
    } catch (FlowNotExecutingException e) {
      // hmmm weird...
      fail("Flow must still be iterating");
    }

  }

  /**
   * A unit test for a plain model with Error Observer
   * 
   * @throws Exception
   */
  public void testFlowWithErrorObserverAndStop() throws Exception {
    flow.setDirector(new Director(flow, "director"));

    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    ErrorObserver errObs = new ErrorObserver(flow, "errObs");
    Stop stop = new Stop(flow, "stop");
    
    flow.connect(source, sink);
    flow.connect(errObs.messageInErrorOutput, sink.input);
    flow.connect(sink.hasFiredPort, stop.input);

    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
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

  /**
   * A unit test for a plain HelloPasserelle model but with an additional Trigger for the Const.
   * 
   * @throws Exception
   */
  public void testHelloPasserelleWithTrigger() throws Exception {
    flow.setDirector(new Director(flow, "director"));

    Trigger trigger = new Trigger(flow, "trigger");
    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");

    flow.connect(trigger.output, source.trigger);
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

  public void testReadMoml() throws Exception {
    Reader in = new InputStreamReader(getClass().getResourceAsStream("/test.xml"));
    Flow f = FlowManager.readMoml(in);
    Map<String, String> props = new HashMap<String, String>();
    props.put("constant1.value", "howdy madurodam");
    props.put("console1.Chop output at #chars", "200");
    flowMgr.executeBlockingLocally(f,props);
    System.out.println("Finished");
  }
}
