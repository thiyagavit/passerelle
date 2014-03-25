package fr.soleil.passerelle.actor.test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import com.isencia.passerelle.testsupport.actor.Forwarder;
import com.isencia.passerelle.testsupport.actor.TextSource;
import fr.soleil.passerelle.actor.flow5.ForLoop;

public class ForLoopActorTest extends TestCase {

  /**
   * This test model runs a loop 2 times as 
   * the loop actor's start port is triggered twice.
   */
  public void testNewLoopWithPortCfgFromMOML() throws Exception {
    Reader in = new InputStreamReader(getClass().getResourceAsStream("/testNewLoopWithPortCfg.moml"));
    Flow f = FlowManager.readMoml(in);
    Map<String, String> props = new HashMap<String, String>();
    FlowManager flowMgr = new FlowManager();
    flowMgr.executeBlockingLocally(f, props);

    new FlowStatisticsAssertion()
      .expectMsgSentCount("TwoTriggers.output", 2L)
      .expectMsgSentCount("StartValue.output", 1L)
      .expectMsgSentCount("EndValue.output", 1L)
      .expectMsgSentCount("StepWidth.output", 1L)
      .expectMsgSentCount("ForLoopWithPortCfg.end", 2L)
      .expectMsgSentCount("ForLoopWithPortCfg.output", 6L)
      .expectMsgReceiptCount("DevNull.input", 2L)
      .assertFlow(f);
  }

  public void testNewForLoop() throws Exception {
    Flow flow = new Flow("testNewLoopWithPortCfg",null);
    ETDirector director = new ETDirector(flow,"director");
    flow.setDirector(director);
    
    TextSource oneTrigger = new TextSource(flow,"OneTrigger");
    ForLoop forLoop = new ForLoop(flow, "ForLoop");
    Forwarder forwarder = new Forwarder(flow, "Forwarder");
    DevNullActor devNull = new DevNullActor(flow, "DevNull");
    
    flow.connect(oneTrigger.output, forLoop.startPort);
    flow.connect(forLoop.outputPort, forwarder.input);
    flow.connect(forwarder.output, forLoop.nextPort);
    flow.connect(forLoop.endPort, devNull.input);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("OneTrigger.values", "hello");
    props.put("director.Dispatch timeout(ms)", "100");
    props.put("ForLoop."+ForLoop.START_VALUE_PARAM_NAME, "0");
    props.put("ForLoop."+ForLoop.END_VALUE_PARAM_NAME, "3");
    props.put("ForLoop."+ForLoop.STEP_WIDTH_PARAM_NAME, "1");

    FlowManager flowMgr = new FlowManager();
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount(oneTrigger, 1L)
    .expectMsgReceiptCount(devNull, 1L)
    .expectActorIterationCount(forLoop, 4L)
    .expectMsgReceiptCount(forwarder, 4L)
    .expectMsgReceiptCount(forLoop.nextPort, 4L)
    .assertFlow(flow);
  }

}
