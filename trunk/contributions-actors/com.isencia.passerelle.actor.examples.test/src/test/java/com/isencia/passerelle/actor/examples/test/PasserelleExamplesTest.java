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
package com.isencia.passerelle.actor.examples.test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.isencia.passerelle.actor.examples.AddRemoveMessageHeader;
import com.isencia.passerelle.actor.examples.DelayWithExecutionTrace;
import com.isencia.passerelle.actor.examples.HeaderFilter;
import com.isencia.passerelle.actor.examples.HelloPasserelle;
import com.isencia.passerelle.actor.examples.MultiInputsTracerConsole;
import com.isencia.passerelle.actor.examples.TextSource;
import com.isencia.passerelle.actor.flow.Delay;
import com.isencia.passerelle.actor.general.Const;
import com.isencia.passerelle.actor.general.ErrorConsole;
import com.isencia.passerelle.actor.general.TracerConsole;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.domain.et.Event;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.MessageHistoryStack;

/**
 * Some sample unit tests for Passerelle flow executions with example actors
 * 
 * @author erwin
 *
 */
public class PasserelleExamplesTest extends TestCase {
	private Flow flow;
	private FlowManager flowMgr;

	protected void setUp() throws Exception {
		flow = new Flow("unit test",null);
		flowMgr = new FlowManager();
	}

	/**
	 * Test some chaotic connections from 2 TextSources to a MultiInputsTracerConsole,
	 * passing via a Delay actor, using multiple connection channels etc.
	 * 
	 * @throws Exception
	 */
  public void testChaoticTextSources() throws Exception {
    // ETDirector is for the new event-driven execution domain
    flow.setDirector(new ETDirector(flow,"director"));
    
    TextSource textSource1 = new TextSource(flow,"textSource1");
    TextSource textSource2 = new TextSource(flow,"textSource2");
    Delay throttlingDelay = new Delay(flow,"throttlingDelay");
    MultiInputsTracerConsole tracerConsole = new MultiInputsTracerConsole(flow, "tracerConsole");
    
    // configure the console with 3 input ports, and get the port references
    tracerConsole.inputPortNamesParameter.setToken("a,b,c");
    Port aPort = (Port) tracerConsole.getPort("a");
    Port bPort = (Port) tracerConsole.getPort("b");
    Port cPort = (Port) tracerConsole.getPort("c");
    
    // use the Flow api to set actor connections programmatically
    flow.connect(textSource1, throttlingDelay);
    flow.connect(textSource2, throttlingDelay);
    flow.connect(textSource1.output, aPort);
    flow.connect(textSource2.output, bPort);
    flow.connect(textSource2.output, cPort);
    flow.connect(throttlingDelay.output, cPort);

    // set remaining cfg params as map passed for execution
    // params are referenced via their name as specified in the actor code
    Map<String, String> props = new HashMap<String, String>();
    props.put("textSource1.values", "a,b,c,d,e,f,g");
    props.put("textSource2.values", "x,y,z");
    props.put("throttlingDelay.time(s)", "1");
    
    // run it till the finish
    flowMgr.executeBlockingLocally(flow,props);
    
    // now check if all went as expected
    new FlowStatisticsAssertion()
    // the first src should have sent 7 messages
    // as it has a default-named output port, we can just pass the actor as 1st argument
        .expectMsgSentCount(textSource1, 7L)
    // or if we want, we can of course pass the port itself
    // second src just sends 3 messages
        .expectMsgSentCount(textSource2.output, 3L)
    // or refer to it by its containing actor and the port name
    // we hope the delay actor receives and sends 10 messages, i.e. received from both connected sources
        .expectMsgSentCount(throttlingDelay, "output", 10L)
        .assertFlow(flow)
    // if all went well for the above expectations, we can either create a new assertion instance,
    // or clear the previous one and set new expectations. 
    // E.g. now for checking the count of received msgs per port.
        .clear()
        .expectMsgReceiptCount(tracerConsole, "a", 7L)
    // here's an example to illustrate that you can also refer to a port (or actor below) by its full hierarchic name
        .expectMsgReceiptCount(bPort.getFullName(), 3L)
        .expectMsgReceiptCount(cPort, 13L)
        .expectMsgReceiptCount(throttlingDelay, 10L)
        .assertFlow(flow)
    // And now for counting the actor iterations...
    // Actors can also be referred to by name or by their instance.
        .clear()
        .expectActorIterationCount("textSource1",7L)
        .expectActorIterationCount(textSource2,3L)
        .expectActorIterationCount(throttlingDelay,10L)
        .assertFlow(flow)
    ;
  }
  

  /**
   * A unit test for the HelloPasserelle model from the actor development guide.
   * 
   * @throws Exception
   */
  public void testHelloPasserelle() throws Exception {
    // ETDirector is for the new event-driven execution domain
    ETDirector director = new ETDirector(flow,"director");
    flow.setDirector(director);
    
    Const constant = new Const(flow,"Constant");
    HelloPasserelle helloHello = new HelloPasserelle(flow, "HelloHello");
    TracerConsole tracerConsole = new TracerConsole(flow, "TracerConsole");
    
    flow.connect(constant, helloHello);
    flow.connect(helloHello, tracerConsole);
    
    // set remaining cfg params as map passed for execution
    // params are referenced via their name as specified in the actor code
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "2");
    props.put("director.Dispatch timeout(ms)", "1000");
    props.put("Constant.value", "Hello world");
    props.put("HelloHello.Changed text", "Hello Passerelle");
    // run it till the finish
    flowMgr.executeBlockingLocally(flow,props);
    
//    for(Event event : director.getEventHistory()) {
//      System.out.println(event);
//    }
    
    // now check if all went as expected
    new FlowStatisticsAssertion()
    // the constant should have sent 1 message
    // as it has a default-named output port, we can just pass the actor as 1st argument
    .expectMsgSentCount(constant, 1L)
    // same thing for the final console, but this time we expect 1 received msg
    .expectMsgReceiptCount(tracerConsole, 1L)
    // in-between the hello actor should have done 1 iteration,
    // resulting in 1 outgoing message. 
    // But we're lazy and skip this final expectation here, 
    // as we already specified that the console should have received this message anyway...
    .expectActorIterationCount(helloHello, 1L)
    // check our expectations
    .assertFlow(flow);
  }
  
  /**
   * A unit test for the AddRemoveMessageHeader model from the actor development guide.
   * This one is for the case where the filter finds the desired header.
   * 
   * @throws Exception
   */
  public void testAddRemoveMessageHeaderWithMatch() throws Exception {
    flow.setDirector(new ETDirector(flow,"director"));
    
    Const constant = new Const(flow,"Constant");
    AddRemoveMessageHeader addRemoveMessageHeader = new AddRemoveMessageHeader(flow, "AddRemoveMessageHeader");
    HeaderFilter headerFilter = new HeaderFilter(flow, "HeaderFilter");
    TracerConsole tracerConsole = new TracerConsole(flow, "TracerConsole");
    ErrorConsole errorConsole = new ErrorConsole(flow, "ErrorConsole");
    
    flow.connect(constant, addRemoveMessageHeader);
    flow.connect(addRemoveMessageHeader, headerFilter);
    flow.connect(headerFilter.outputMatch, tracerConsole.input);
    flow.connect(headerFilter.outputNoMatch, errorConsole.input);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    props.put("AddRemoveMessageHeader.Header name", "MyHeader");
    props.put("AddRemoveMessageHeader.Header value", "something");
    props.put("AddRemoveMessageHeader.mode", "Add");
    props.put("HeaderFilter.Header name", "MyHeader");
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
      .expectMsgSentCount(constant, 1L)
      .expectMsgSentCount(addRemoveMessageHeader, 1L)
      .expectMsgSentCount(headerFilter.outputMatch, 1L)
      .expectMsgReceiptCount(tracerConsole, 1L)
      .expectMsgReceiptCount(errorConsole, 0L)
      .assertFlow(flow);
  }

  /**
   * A unit test for the AddRemoveMessageHeader model from the actor development guide.
   * This one is for when the filter does NOT find its desired header.
   * 
   * @throws Exception
   */
  public void testAddRemoveMessageHeaderWithNoMatch() throws Exception {
    flow.setDirector(new ETDirector(flow,"director"));
    
    Const constant = new Const(flow,"Constant");
    AddRemoveMessageHeader addRemoveMessageHeader = new AddRemoveMessageHeader(flow, "AddRemoveMessageHeader");
    HeaderFilter headerFilter = new HeaderFilter(flow, "HeaderFilter");
    TracerConsole tracerConsole = new TracerConsole(flow, "TracerConsole");
    ErrorConsole errorConsole = new ErrorConsole(flow, "ErrorConsole");
    
    flow.connect(constant, addRemoveMessageHeader);
    flow.connect(addRemoveMessageHeader, headerFilter);
    flow.connect(headerFilter.outputMatch, tracerConsole.input);
    flow.connect(headerFilter.outputNoMatch, errorConsole.input);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    props.put("AddRemoveMessageHeader.Header name", "MyHeader");
    props.put("AddRemoveMessageHeader.Header value", "something");
    props.put("AddRemoveMessageHeader.mode", "Add");
    // let's set a filter that will certainly not match
    props.put("HeaderFilter.Header name", "AnotherHeader");

    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
      .expectMsgSentCount(constant, 1L)
      .expectMsgSentCount(addRemoveMessageHeader, 1L)
      .expectMsgSentCount(headerFilter.outputMatch, 0L)
      .expectMsgSentCount(headerFilter.outputNoMatch, 1L)
      .expectMsgReceiptCount(tracerConsole, 0L)
      .expectMsgReceiptCount(errorConsole, 1L)
      .assertFlow(flow);
  }
  
  /**
   * This test illustrates the chaining of each delay when only 1 work thread
   * is available to execute all work steps. 
   * The total model execution time thus becomes of the order of 3*(3+3+3),
   * i.e. each of the 3 src msg needs to pass through 3 consecutive work steps, each one taking 3s.
   */
  public void testChainedDelaysET1Thread() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    __testChainedDelays(new ETDirector(flow,"director"), props);
  }
  
  /**
   * This test illustrates the chaining of each delay when only 1 work thread
   * is available to execute all work steps. 
   * The total model execution time thus becomes of the order of 3*(3+3+3),
   * i.e. each of the 3 src msg needs to pass through 3 consecutive work steps, each one taking 3s.
   */
  public void testChainedDelaysET2Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "2");
    props.put("director.Dispatch timeout(ms)", "1000");
    __testChainedDelays(new ETDirector(flow,"director"), props);
  }
  
  public void testChainedDelaysET3Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "1000");
    ETDirector d = new ETDirector(flow,"director");
    __testChainedDelays(d, props);
    for (Event event : d.getEventHistory()) {
      System.out.println(event);
    }
  }
  
  /**
   * This test illustrates the "factory chain" advantage of the PN domain,
   * where each actor has its own thread.
   * This leads to all 3 "worker" actors (the delays) to be able to work (spend time) concurrently.
   */
  public void testChainedDelaysPN() throws Exception {
    __testChainedDelays(new Director(flow,"director"), new HashMap<String, String>());
  }

  public void __testChainedDelays(ptolemy.actor.Director d, Map<String, String> paramOverrides) throws Exception {
    flow.setDirector(d);
    
    TextSource src = new TextSource(flow, "src");
    DelayWithExecutionTrace delay1 = new DelayWithExecutionTrace(flow, "delay1");
    DelayWithExecutionTrace delay2 = new DelayWithExecutionTrace(flow, "delay2");
    DelayWithExecutionTrace delay3 = new DelayWithExecutionTrace(flow, "delay3");
    MessageHistoryStack sink = new MessageHistoryStack(flow, "sink");

    flow.connect(src, delay1);
    flow.connect(delay1, delay2);
    flow.connect(delay2, delay3);
    flow.connect(delay3, sink);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("src.values", "pol,pel,pingo");
    props.put("delay1.time(s)", "3");
    props.put("delay2.time(s)", "3");
    props.put("delay3.time(s)", "3");
    props.put("delay1.Buffer time (ms)", "10");
    props.put("delay2.Buffer time (ms)", "10");
    props.put("delay3.Buffer time (ms)", "10");
    props.putAll(paramOverrides);
    
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgReceiptCount(sink, 3L)
    .assertFlow(flow)
    ;
  }

  public void testConcurrentInputsOnDelayET3Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "1000");
    ETDirector d = new ETDirector(flow,"director");
    __testConcurrentInputsOnDelay(d, props);
    for (Event event : d.getEventHistory()) {
      System.out.println(event);
    }
  }
  
  public void testConcurrentInputsOnDelayET4Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "4");
    props.put("director.Dispatch timeout(ms)", "1000");
    ETDirector d = new ETDirector(flow,"director");
    __testConcurrentInputsOnDelay(d, props);
    for (Event event : d.getEventHistory()) {
      System.out.println(event);
    }
  }
  
  public void testConcurrentInputsOnDelayPN() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    __testConcurrentInputsOnDelay(new Director(flow,"director"), props);
  }
  
  public void __testConcurrentInputsOnDelay(ptolemy.actor.Director d, Map<String, String> paramOverrides) throws Exception {
    flow.setDirector(d);
    
    TextSource src1 = new TextSource(flow, "src1");
    TextSource src2 = new TextSource(flow, "src2");
    TextSource src3 = new TextSource(flow, "src3");
    DelayWithExecutionTrace delay1 = new DelayWithExecutionTrace(flow, "delay1");
    DelayWithExecutionTrace delay2 = new DelayWithExecutionTrace(flow, "delay2");
    DelayWithExecutionTrace delay3 = new DelayWithExecutionTrace(flow, "delay3");
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
    props.put("delay1.time(s)", "3");
    props.put("delay2.time(s)", "3");
    props.put("delay3.time(s)", "3");
    props.put("delay1.Buffer time (ms)", "10");
    props.put("delay2.Buffer time (ms)", "10");
    props.put("delay3.Buffer time (ms)", "10");
    props.putAll(paramOverrides);
    
    flowMgr.executeBlockingLocally(flow,props);

    new FlowStatisticsAssertion()
    .expectMsgReceiptCount(sink, 3L)
    .assertFlow(flow)
    ;
  }

  /**
   * A more chaotic delay model, with two parallel branches with delay actors,
   * ending up in their own sinks.
   */
  public void testChainedAndParallelDelaysPN() throws Exception {
    __testChainedAndParallelDelays(new Director(flow,"director"), new HashMap<String, String>());
  }

  public void testChainedAndParallelDelaysET3Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "1000");
    __testChainedAndParallelDelays(new ETDirector(flow,"director"), props);
  }
  
  public void testChainedAndParallelDelaysET5Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "5");
    props.put("director.Dispatch timeout(ms)", "1000");
    __testChainedAndParallelDelays(new ETDirector(flow,"director"), props);
  }

  public void __testChainedAndParallelDelays(ptolemy.actor.Director d, Map<String, String> paramOverrides) throws Exception {
    flow.setDirector(d);
    
    TextSource src = new TextSource(flow, "src");
    DelayWithExecutionTrace delay1 = new DelayWithExecutionTrace(flow, "delay1");
    DelayWithExecutionTrace delay_branch1_1 = new DelayWithExecutionTrace(flow, "delay1_1");
    DelayWithExecutionTrace delay_branch1_2 = new DelayWithExecutionTrace(flow, "delay1_2");
    DelayWithExecutionTrace delay_branch2_1 = new DelayWithExecutionTrace(flow, "delay2_1");
    DelayWithExecutionTrace delay_branch2_2 = new DelayWithExecutionTrace(flow, "delay2_2");
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
    props.put("delay1.time(s)", "3");
    props.put("delay1_1.time(s)", "3");
    props.put("delay1_2.time(s)", "3");
    props.put("delay2_1.time(s)", "3");
    props.put("delay2_2.time(s)", "3");
    props.put("delay1.Buffer time (ms)", "10");
    props.put("delay1_1.Buffer time (ms)", "10");
    props.put("delay1_2.Buffer time (ms)", "10");
    props.put("delay2_1.Buffer time (ms)", "10");
    props.put("delay2_2.Buffer time (ms)", "10");
    props.putAll(paramOverrides);
    
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgReceiptCount(sink1, 3L)
    .expectMsgReceiptCount(sink2, 3L)
    .assertFlow(flow)
    ;
  }

  public void testReadMoml() throws Exception {
    Reader in = new InputStreamReader(getClass().getResourceAsStream("/test.xml"));
    Flow f = FlowManager.readMoml(in);
    Map<String, String> props = new HashMap<String, String>();
    props.put("constant1.value", "\"howdy madurodam\"");
    props.put("console1.Chop output at #chars", "200");
    flowMgr.executeBlockingLocally(f,props);
    System.out.println("Finished");
  }
}
