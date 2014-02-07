package com.isencia.passerelle.actor.forkjoin.test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import ptolemy.data.StringToken;
import junit.framework.TestCase;
import com.isencia.passerelle.actor.forkjoin.Join;
import com.isencia.passerelle.actor.general.TracerConsole;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import com.isencia.passerelle.testsupport.actor.MapBasedRouter;
import com.isencia.passerelle.testsupport.actor.MapModifier;
import com.isencia.passerelle.testsupport.actor.MapSource;

public class ForkJoinTest extends TestCase {
  
  // test a simple ForkJoin case with 2 branches : check branch isolation & correct joining
  // a Map is sent in, with one entry
  // branch 1 modifies the entry value and checks if it is correctly seen, after a 1s delay, via a router
  // branch 2 adds an extra entry and checks that it has its own private Map copy,
  // still with unchanged original entry
  // then a join is done and routers check that the extra entry from branch 2 is correctly found, 
  // as well as the modified value from branch1
  public void testForkJoin1() throws Exception {
    Flow flow = new Flow("testForkJoin1", null);
    Director d = new Director(flow,"director");
    MapSource src = new MapSource(flow, "src");
    
    TestForkForMaps fork = new TestForkForMaps(flow, "fork");
    fork.outputPortCfgExt.outputPortNamesParameter.setToken(new StringToken("1,2"));
    
    MapModifier branch1Modifier = new MapModifier(flow, "branch1Modifier");
    Delay branch1Delay = new Delay(flow,"branch1Delay");
    MapBasedRouter branch1Router = new MapBasedRouter(flow, "branch1Router");
    branch1Router.outputPortCfgExt.outputPortNamesParameter.setToken(new StringToken("1,2"));

    MapModifier branch2Modifier = new MapModifier(flow, "branch2Modifier");
    Delay branch2Delay = new Delay(flow,"branch2Delay");
    MapBasedRouter branch2Router = new MapBasedRouter(flow, "branch2Router");
    branch2Router.outputPortCfgExt.outputPortNamesParameter.setToken(new StringToken("1,2"));
    
    Join join = new Join(flow,"join");
    
    MapBasedRouter router = new MapBasedRouter(flow, "router");
    router.outputPortCfgExt.outputPortNamesParameter.setToken(new StringToken("1,2,3"));
    MapBasedRouter router2 = new MapBasedRouter(flow, "router2");
    router2.outputPortCfgExt.outputPortNamesParameter.setToken(new StringToken("1,2,3"));
    
    DevNullActor sinkNOK = new DevNullActor(flow, "problem");
    DevNullActor sinkOK = new DevNullActor(flow, "ok");
    
    flow.connect(src, fork);
    flow.connect((Port)fork.getPort("1"), branch1Modifier.input);
    flow.connect(branch1Modifier,branch1Delay);
    flow.connect(branch1Delay, branch1Router);
    flow.connect((Port)branch1Router.getPort("1"), join.mergeInput);
    flow.connect((Port)branch1Router.getPort("2"), sinkNOK.input);
    
    flow.connect((Port)fork.getPort("2"), branch2Modifier.input);
    flow.connect(branch2Modifier,branch2Delay);
    flow.connect(branch2Delay, branch2Router);
    flow.connect((Port)branch2Router.getPort("2"), join.mergeInput);
    flow.connect((Port)branch2Router.getPort("1"), sinkNOK.input);
    
    flow.connect(join, router);
    flow.connect((Port)router.getPort("1"), sinkNOK.input);
    flow.connect((Port)router.getPort("2"), sinkNOK.input);
    flow.connect((Port)router.getPort("3"), router2.input);
    flow.connect((Port)router2.getPort("1"), sinkOK.input);
    flow.connect((Port)router2.getPort("2"), sinkNOK.input);
    flow.connect((Port)router2.getPort("3"), sinkNOK.input);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("src.entries", "pol=2");
    props.put("fork.maxRetentionTime", "2");
    props.put("branch1Modifier.header name", "pol");
    props.put("branch1Modifier.header value", "1");
    props.put("branch1Modifier.mode", "Modify");
    props.put("branch1Router.key", "pol");
    props.put("branch2Modifier.header name", "pingo");
    props.put("branch2Modifier.header value", "3");
    props.put("branch2Modifier.mode", "Add");
    props.put("branch2Router.key", "pol");
    props.put("router.key", "pingo");
    props.put("router2.key", "pol");
    new FlowManager().executeBlockingLocally(flow, props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount(src, 1L)
    .expectMsgReceiptCount(sinkNOK, 0L)
    .expectMsgReceiptCount(sinkOK, 1L)
    .assertFlow(flow);
  }

  public void testForkJoinInSubModel() throws Exception {
    Reader in = new InputStreamReader(getClass().getResourceAsStream("/testSubModelForkJoin.moml"));
    Flow flow = FlowManager.readMoml(in);
    
    MapSource src = (MapSource) flow.getEntity("RequestSource");
    TracerConsole sink = (TracerConsole) flow.getEntity("Tracer Console");
    Join join = (Join) flow.getEntity("ForkJoinSubModel.Join");
    
    Map<String, String> props = new HashMap<String, String>();
    new FlowManager().executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount(src, 1L)
    .expectMsgReceiptCount(join, 3L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
  }

}
