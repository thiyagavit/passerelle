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
package com.isencia.passerelle.process.actor.test;

import java.util.HashMap;
import java.util.Map;
import ptolemy.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import junit.framework.TestCase;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.process.actor.MockTaskResultActor;
import com.isencia.passerelle.process.actor.flow.Fork;
import com.isencia.passerelle.process.actor.flow.Join;
import com.isencia.passerelle.process.actor.flow.StartActor;
import com.isencia.passerelle.process.actor.trial.ContextTracerConsole;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;

public class ProcessActorTest extends TestCase {

  public void testForkJoin5() throws Exception {
    int branchCount = 5;
    _testForkJoin(branchCount);
  }

  public void testForkJoin100() throws Exception {
    int branchCount = 100;
    _testForkJoin(branchCount);
  }

  protected void _testForkJoin(int branchCount) throws IllegalActionException, NameDuplicationException, FlowAlreadyExecutingException, PasserelleException {
    Flow flow = new Flow("testForkJoin1",null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow,"director"));
    
    StartActor start = new StartActor(flow,"start");
    Fork fork = new Fork(flow, "fork");
    Join join = new Join(flow, "join");
    ContextTracerConsole sink = new ContextTracerConsole(flow, "sink");
    flow.connect(start, fork);
    flow.connect(join, sink);

    Map<String, String> props = new HashMap<String, String>();

    String[] portNames = new String[branchCount];
    portNames[0] = "p0";
    StringBuilder portNamesBldr = new StringBuilder(portNames[0]);
    for(int i = 1; i<branchCount;++i) {
      portNames[i] = "p"+i;
      portNamesBldr.append(","+portNames[i]);
    }
    
    fork.outputPortNamesParameter.setToken(portNamesBldr.toString());
    
    MockTaskResultActor[] taskActors = new MockTaskResultActor[branchCount];
    for(int i = 0; i<branchCount;++i) {
      String actorName = "task"+i;
      String portName = portNames[i];
      taskActors[i] = new MockTaskResultActor(flow, actorName);
      flow.connect((IOPort)fork.getPort(portName), taskActors[i].input);
      flow.connect(taskActors[i],join);
      props.put(actorName+".Result items", "hello=world");
      props.put(actorName+".time(s)", "1");
    }
    
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount(start, 1L)
    .expectMsgReceiptCount(join, (long) branchCount)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
  }

}
