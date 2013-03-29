package com.isencia.passerelle.clone;

import java.util.HashMap;
import java.util.Map;
import ptolemy.kernel.util.Attribute;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowDefinitionAssertion;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import junit.framework.TestCase;

public class CloneTest extends TestCase {
  
  public void testFlowClone() throws Exception {
    Flow flow = new Flow("testHelloPasserelle",null);
    Director director = new Director(flow,"director");
    flow.setDirector(director);
    Attribute expertModeParameter = director.getAttribute(DirectorAdapter.EXPERTMODE_PARAM);
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, sink);
    
    Flow clone = (Flow) flow.clone();
    
    new FlowDefinitionAssertion()
      .expectActor(source.getFullName())
      .expectActor(sink.getFullName())
      .expectParameter(expertModeParameter.getFullName())
      .expectRelation(source.output.getFullName(), sink.input.getFullName())
      .assertFlow(flow)
      .assertFlow(clone);
  }

  public void testFlowCloneExecution() throws Exception {
    Flow flow = new Flow("testHelloPasserelle",null);
    flow.setDirector(new Director(flow,"director"));
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, sink);
    
    Flow clone = (Flow) flow.clone();
    
    FlowManager flowMgr = new FlowManager();

    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");

    flowMgr.executeBlockingLocally(clone,props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(clone);
  }

  public void testFlowClone100Times() throws Exception {
    Flow flow = new Flow("testHelloPasserelle",null);
    Director director = new Director(flow,"director");
    flow.setDirector(director);
    Attribute expertModeParameter = director.getAttribute(DirectorAdapter.EXPERTMODE_PARAM);
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, sink);
    
    Flow clone = null;
    for(int i=0; i<100; ++i) {
      clone = (Flow) flow.clone();
    }
    
    new FlowDefinitionAssertion()
      .expectActor(source.getFullName())
      .expectActor(sink.getFullName())
      .expectParameter(expertModeParameter.getFullName())
      .expectRelation(source.output.getFullName(), sink.input.getFullName())
      .assertFlow(flow)
      .assertFlow(clone);
  }

  public void testFlowCloneExecution100Times() throws Exception {
    Flow flow = new Flow("testHelloPasserelle",null);
    flow.setDirector(new Director(flow,"director"));
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, sink);
    
    Flow clone = null;
    for(int i=0; i<100; ++i) {
      clone = (Flow) flow.clone();
    }
    
    FlowManager flowMgr = new FlowManager();

    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");

    flowMgr.executeBlockingLocally(clone,props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(clone);
  }

}
