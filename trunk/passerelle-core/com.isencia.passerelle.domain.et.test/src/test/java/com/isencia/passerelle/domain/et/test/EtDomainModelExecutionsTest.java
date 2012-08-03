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
package com.isencia.passerelle.domain.et.test;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.domain.et.Event;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.testsupport.actor.ExceptionGenerator;
import com.isencia.passerelle.testsupport.actor.Forwarder;
import com.isencia.passerelle.testsupport.actor.MessageHistoryStack;
import com.isencia.passerelle.testsupport.actor.TextSource;

/**
 * Some unit tests for Passerelle's ET domain
 * 
 * @author erwin
 */
public class EtDomainModelExecutionsTest extends TestCase {
  private Flow flow;
  private FlowManager flowMgr;

  protected void setUp() throws Exception {
    flow = new Flow("EtDomainModelExecutionsTest", null);
    flowMgr = new FlowManager();
  }

  public void testHelloPasserelle() throws Exception {
    ETDirector director = new ETDirector(flow, "director");
    flow.setDirector(director);

    Const constant = new Const(flow, "Constant");
    Forwarder helloHello = new Forwarder(flow, "HelloHello");
    MessageHistoryStack tracerConsole = new MessageHistoryStack(flow, "TracerConsole");

    flow.connect(constant, helloHello);
    flow.connect(helloHello, tracerConsole);

    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "2");
    props.put("director.Dispatch timeout(ms)", "250");
    props.put("Constant.value", "Hello world");
    flowMgr.executeBlockingLocally(flow, props);

    // now check if all went as expected
    new FlowStatisticsAssertion().expectMsgSentCount(constant, 1L).expectMsgReceiptCount(tracerConsole, 1L).expectActorIterationCount(helloHello, 1L)
        .assertFlow(flow);
  }

  /**
   * This test illustrates the chaining of each delay when only 1 work thread is available to execute all work steps. The total model execution time thus
   * becomes of the order of 3*(3+3+3), i.e. each of the 3 src msg needs to pass through 3 consecutive work steps, each one taking 3s.
   */
  public void testChainedDelaysET1Thread() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    __testChainedDelays(new ETDirector(flow, "director"), props);
  }

  public void testChainedDelaysET2Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "2");
    props.put("director.Dispatch timeout(ms)", "250");
    __testChainedDelays(new ETDirector(flow, "director"), props);
  }

  public void testChainedDelaysET3Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "250");
    ETDirector d = new ETDirector(flow, "director");
    __testChainedDelays(d, props);
  }

  /**
   * This test illustrates the "factory chain" advantage of the PN domain, where each actor has its own thread. This leads to all 3 "worker" actors (the delays)
   * to be able to work (spend time) concurrently.
   */
  public void testChainedDelaysPN() throws Exception {
    __testChainedDelays(new Director(flow, "director"), new HashMap<String, String>());
  }

  public void __testChainedDelays(ptolemy.actor.Director d, Map<String, String> paramOverrides) throws Exception {
    flow.setDirector(d);

    TextSource src = new TextSource(flow, "src");
    Delay delay1 = new Delay(flow, "delay1");
    Delay delay2 = new Delay(flow, "delay2");
    Delay delay3 = new Delay(flow, "delay3");
    MessageHistoryStack sink = new MessageHistoryStack(flow, "sink");

    flow.connect(src, delay1);
    flow.connect(delay1, delay2);
    flow.connect(delay2, delay3);
    flow.connect(delay3, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("src.values", "pol,pel,pingo");
    props.put("delay1.time(s)", "1");
    props.put("delay2.time(s)", "1");
    props.put("delay3.time(s)", "1");
    props.put("delay1.Buffer time (ms)", "10");
    props.put("delay2.Buffer time (ms)", "10");
    props.put("delay3.Buffer time (ms)", "10");
    props.putAll(paramOverrides);

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion().expectMsgReceiptCount(sink, 3L).assertFlow(flow);
  }

  public void testConcurrentInputsOnDelayET3Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "250");
    ETDirector d = new ETDirector(flow, "director");
    __testConcurrentInputsOnDelay(d, props);
    for (Event event : d.getEventHistory()) {
      System.out.println(event);
    }
  }

  public void testConcurrentInputsOnDelayET4Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "4");
    props.put("director.Dispatch timeout(ms)", "250");
    ETDirector d = new ETDirector(flow, "director");
    __testConcurrentInputsOnDelay(d, props);
    for (Event event : d.getEventHistory()) {
      System.out.println(event);
    }
  }

  public void testConcurrentInputsOnDelayPN() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    __testConcurrentInputsOnDelay(new Director(flow, "director"), props);
  }

  public void __testConcurrentInputsOnDelay(ptolemy.actor.Director d, Map<String, String> paramOverrides) throws Exception {
    flow.setDirector(d);

    TextSource src1 = new TextSource(flow, "src1");
    TextSource src2 = new TextSource(flow, "src2");
    TextSource src3 = new TextSource(flow, "src3");
    Delay delay1 = new Delay(flow, "delay1");
    Delay delay2 = new Delay(flow, "delay2");
    Delay delay3 = new Delay(flow, "delay3");
    MessageHistoryStack sink = new MessageHistoryStack(flow, "sink");

    flow.connect(src1, delay1);
    flow.connect(src2, delay1);
    flow.connect(src3, delay1);
    flow.connect(delay1, delay2);
    flow.connect(delay2, delay3);
    flow.connect(delay3, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("src1.values", "pol");
    props.put("src2.values", "pel");
    props.put("src3.values", "pingo");
    props.put("delay1.time(s)", "1");
    props.put("delay2.time(s)", "1");
    props.put("delay3.time(s)", "1");
    props.put("delay1.Buffer time (ms)", "10");
    props.put("delay2.Buffer time (ms)", "10");
    props.put("delay3.Buffer time (ms)", "10");
    props.putAll(paramOverrides);

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion().expectMsgReceiptCount(sink, 3L).assertFlow(flow);
  }

  /**
   * A more chaotic delay model, with two parallel branches with delay actors, ending up in their own sinks.
   */
  public void testChainedAndParallelDelaysPN() throws Exception {
    __testChainedAndParallelDelays(new Director(flow, "director"), new HashMap<String, String>());
  }

  public void testChainedAndParallelDelaysET3Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "250");
    __testChainedAndParallelDelays(new ETDirector(flow, "director"), props);
  }

  public void testChainedAndParallelDelaysET5Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "5");
    props.put("director.Dispatch timeout(ms)", "250");
    __testChainedAndParallelDelays(new ETDirector(flow, "director"), props);
  }

  public void __testChainedAndParallelDelays(ptolemy.actor.Director d, Map<String, String> paramOverrides) throws Exception {
    flow.setDirector(d);

    TextSource src = new TextSource(flow, "src");
    Delay delay1 = new Delay(flow, "delay1");
    Delay delay_branch1_1 = new Delay(flow, "delay1_1");
    Delay delay_branch1_2 = new Delay(flow, "delay1_2");
    Delay delay_branch2_1 = new Delay(flow, "delay2_1");
    Delay delay_branch2_2 = new Delay(flow, "delay2_2");
    MessageHistoryStack sink1 = new MessageHistoryStack(flow, "sink1");
    MessageHistoryStack sink2 = new MessageHistoryStack(flow, "sink2");

    flow.connect(src, delay1);
    flow.connect(delay1, delay_branch1_1);
    flow.connect(delay1, delay_branch2_1);
    flow.connect(delay_branch1_1, delay_branch1_2);
    flow.connect(delay_branch2_1, delay_branch2_2);
    flow.connect(delay_branch1_2, sink1);
    flow.connect(delay_branch2_2, sink2);

    Map<String, String> props = new HashMap<String, String>();
    props.put("src.values", "pol,pel,pingo");
    props.put("delay1.time(s)", "1");
    props.put("delay1_1.time(s)", "1");
    props.put("delay1_2.time(s)", "1");
    props.put("delay2_1.time(s)", "1");
    props.put("delay2_2.time(s)", "1");
    props.put("delay1.Buffer time (ms)", "10");
    props.put("delay1_1.Buffer time (ms)", "10");
    props.put("delay1_2.Buffer time (ms)", "10");
    props.put("delay2_1.Buffer time (ms)", "10");
    props.put("delay2_2.Buffer time (ms)", "10");
    props.putAll(paramOverrides);

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion().expectMsgReceiptCount(sink1, 3L).expectMsgReceiptCount(sink2, 3L).assertFlow(flow);
  }

  public void testProcessException() throws Exception {
    ETDirector director = new ETDirector(flow, "director");
    flow.setDirector(director);

    Const constant = new Const(flow, "const");
    ExceptionGenerator excGenerator = new ExceptionGenerator(flow, "excGenerator");
    MessageHistoryStack sink = new MessageHistoryStack(flow, "sink");

    flow.connect(constant, excGenerator);
    flow.connect(excGenerator, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "2");
    props.put("director.Dispatch timeout(ms)", "250");
    props.put("const.value", "Hello world");
    props.put("excGenerator.process Exception", "true");
    flowMgr.executeBlockingLocally(flow, props);

    // now check if all went as expected
    new FlowStatisticsAssertion().expectMsgSentCount(constant, 1L).expectMsgReceiptCount(sink, 0L).expectActorIterationCount(excGenerator, 1L).assertFlow(flow);
  }

  // utility for whenever we would like to get the moml from a java-coded flow
//  private void writeFlow(Flow flow) {
//    try {
//      File flowMomlFile = new File("C:/temp/" + flow.getName() + ".moml");
//      Writer momlWriter = new FileWriter(flowMomlFile);
//      flow.exportMoML(momlWriter);
//      momlWriter.flush();
//      momlWriter.close();
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
}
