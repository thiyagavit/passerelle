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
import junit.framework.TestCase;
import ptolemy.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.process.actor.DelimitedResultLineGenerator;
import com.isencia.passerelle.process.actor.TaskResultActor;
import com.isencia.passerelle.process.actor.RequestSource;
import com.isencia.passerelle.process.actor.flow.Fork;
import com.isencia.passerelle.process.actor.flow.Join;
import com.isencia.passerelle.process.actor.trial.ContextTracerConsole;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;

public class ProcessActorTest extends TestCase {

//  public void testForkJoin5Branches_1s() throws Exception {
//    _testForkJoin(5,"1");
//  }
//
//  public void testForkJoin5Branches_0s() throws Exception {
//    _testForkJoin(5,"0");
//  }

  public void testForkJoin5Branches_1_5s() throws Exception {
    _testForkJoin(5,"1","1","2","3","5");
  }

//  public void testForkJoin9Branches_1s() throws Exception {
//    _testForkJoin(9,"1");
//  }
//
//  public void testForkJoin9Branches_0s() throws Exception {
//    _testForkJoin(9,"0");
//  }
  
//  public void testForkJoin100Branches_1s() throws Exception {
//    int branchCount = 100;
//    _testForkJoin(branchCount,"1");
//  }
//
//  public void testForkJoin100Branches_0s() throws Exception {
//    int branchCount = 100;
//    _testForkJoin(branchCount,"0");
//  }

  protected void _testForkJoin(int branchCount, String... taskTimes) throws IllegalActionException, NameDuplicationException, FlowAlreadyExecutingException, PasserelleException {
    Flow flow = new Flow("testForkJoin1",null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow,"director"));
    
    RequestSource start = new RequestSource(flow,"start");
    Fork fork = new Fork(flow, "fork");
    Join join = new Join(flow, "join");
    DelimitedResultLineGenerator lineGen = new DelimitedResultLineGenerator(flow, "lineGen");
    ContextTracerConsole sink = new ContextTracerConsole(flow, "sink");
    flow.connect(start, fork);
    flow.connect(join, lineGen);
    flow.connect(lineGen, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("lineGen.result item names", "requestID,task0_says,goodbye");
    
    String[] portNames = new String[branchCount];
    portNames[0] = "p0";
    StringBuilder portNamesBldr = new StringBuilder(portNames[0]);
    for(int i = 1; i<branchCount;++i) {
      portNames[i] = "p"+i;
      portNamesBldr.append(","+portNames[i]);
    }
    
    fork.outputPortNamesParameter.setToken(portNamesBldr.toString());
    
    TaskResultActor[] taskActors = new TaskResultActor[branchCount];
    for(int i = 0; i<branchCount;++i) {
      String actorName = "task"+i;
      String portName = portNames[i];
      taskActors[i] = new TaskResultActor(flow, actorName);
      flow.connect((IOPort)fork.getPort(portName), taskActors[i].input);
      flow.connect(taskActors[i],join);
      props.put(actorName+".Result items", "task"+i+"_says=hello world"+i);
      int ttIndex = Math.min(taskTimes.length - 1, i);
      props.put(actorName+".time(s)", taskTimes[ttIndex]);
    }
    
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount(start, 1L)
    .expectMsgReceiptCount(join, (long) branchCount)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
  }

}
