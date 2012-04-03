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
package com.isencia.passerelle.actor;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import com.isencia.passerelle.testsupport.actor.MockWorker;

public class ActorApiTest extends TestCase {
  private Flow flow;
  private FlowManager flowMgr;

  protected void setUp() throws Exception {
    flow = new Flow("unit test",null);
    flowMgr = new FlowManager();
  }

  /**
   * A unit test for a plain HelloPasserelle model.
   * 
   * @throws Exception
   */
  public void testHelloPasserelle() throws Exception {
    flow.setDirector(new Director(flow,"director"));
    
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    
    flow.connect(source, sink);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
  }
  
  public void testFlowWithValidationErrorAndValidation() throws Exception {
    Director director = new Director(flow,"director");
    director.validateInitializationParam.setExpression("true");
    flow.setDirector(director);
    
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    MockWorker firstWorker = new MockWorker(flow, "firstWorker");
    MockWorker secondWorker = new MockWorker(flow, "secondWorker");
    MockWorker thirdWorker = new MockWorker(flow, "thirdWorker");
    InitializationValidator validationError = new InitializationValidator(flow, "validationError");
    InitializationValidator validationOk = new InitializationValidator(flow, "validationOk");
    
    flow.connect(source, firstWorker);
    flow.connect(firstWorker, secondWorker);
    flow.connect(secondWorker, validationOk);
    flow.connect(firstWorker, validationError);
    flow.connect(validationError, thirdWorker);
    flow.connect(thirdWorker, sink);
    flow.connect(validationOk, sink);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    props.put("validationError.Must generate validation error", "true");
    props.put("validationError.Validation error message", "something's wrong here");
    try {
      flowMgr.executeBlockingLocally(flow,props);
    } catch (ValidationException e) {
      // this is expected
    }
    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 0L)
    .expectActorIterationCount(source, 0L)
    .expectActorIterationCount(sink, 0L)
    .expectActorIterationCount(firstWorker, 0L)
    .expectActorIterationCount(secondWorker, 0L)
    .expectActorIterationCount(thirdWorker, 0L)
    .expectActorIterationCount(validationError, 0L)
    .expectActorIterationCount(validationOk, 0L)
    .assertFlow(flow);
  }

  public void testFlowWithValidationErrorButNoValidation() throws Exception {
    Director director = new Director(flow,"director");
    director.validateInitializationParam.setExpression("false");
    flow.setDirector(director);
    
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    MockWorker firstWorker = new MockWorker(flow, "firstWorker");
    MockWorker secondWorker = new MockWorker(flow, "secondWorker");
    MockWorker thirdWorker = new MockWorker(flow, "thirdWorker");
    InitializationValidator validationError = new InitializationValidator(flow, "validationError");
    InitializationValidator validationOk = new InitializationValidator(flow, "validationOk");
    
    flow.connect(source, firstWorker);
    flow.connect(firstWorker, secondWorker);
    flow.connect(secondWorker, validationOk);
    flow.connect(firstWorker, validationError);
    flow.connect(validationError, thirdWorker);
    flow.connect(thirdWorker, sink);
    flow.connect(validationOk, sink);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    props.put("validationError.Must generate validation error", "true");
    props.put("validationError.Validation error message", "something's wrong here");
    try {
      flowMgr.executeBlockingLocally(flow,props);
    } catch (ValidationException e) {
      // this is expected
    }
    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    // strange, but due to uncontrolled parallel branching, the msg arrives twice at the sink!
    .expectMsgReceiptCount(sink, 2L)
    .expectActorIterationCount(source, 1L)
    .expectActorIterationCount(sink, 2L)
    .expectActorIterationCount(firstWorker, 1L)
    .expectActorIterationCount(secondWorker, 1L)
    .expectActorIterationCount(thirdWorker, 1L)
    .expectActorIterationCount(validationError, 1L)
    .expectActorIterationCount(validationOk, 1L)
    .assertFlow(flow);
  }

  public void testFlowWithoutValidationError() throws Exception {
    Director director = new Director(flow,"director");
    director.validateInitializationParam.setExpression("true");
    flow.setDirector(director);
    
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    MockWorker firstWorker = new MockWorker(flow, "firstWorker");
    MockWorker secondWorker = new MockWorker(flow, "secondWorker");
    MockWorker thirdWorker = new MockWorker(flow, "thirdWorker");
    InitializationValidator validationError = new InitializationValidator(flow, "validationError");
    InitializationValidator validationOk = new InitializationValidator(flow, "validationOk");
    
    flow.connect(source, firstWorker);
    flow.connect(firstWorker, secondWorker);
    flow.connect(secondWorker, validationOk);
    flow.connect(firstWorker, validationError);
    flow.connect(validationError, thirdWorker);
    flow.connect(thirdWorker, sink);
    flow.connect(validationOk, sink);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    props.put("validationError.Must generate validation error", "false");
    props.put("validationError.Validation error message", "something's wrong here");
    flowMgr.executeBlockingLocally(flow,props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    // strange, but due to uncontrolled parallel branching, the msg arrives twice at the sink!
    .expectMsgReceiptCount(sink, 2L)
    .expectActorIterationCount(source, 1L)
    .expectActorIterationCount(sink, 2L)
    .expectActorIterationCount(firstWorker, 1L)
    .expectActorIterationCount(secondWorker, 1L)
    .expectActorIterationCount(thirdWorker, 1L)
    .expectActorIterationCount(validationError, 1L)
    .expectActorIterationCount(validationOk, 1L)
    .assertFlow(flow);
  }
  
}
