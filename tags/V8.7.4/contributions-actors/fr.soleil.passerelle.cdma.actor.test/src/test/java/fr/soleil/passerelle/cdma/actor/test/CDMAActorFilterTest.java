/* Copyright 2013 - Synchrotron Soleil

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
package fr.soleil.passerelle.cdma.actor.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import junit.framework.TestCase;
import org.cdma.Factory;
import org.cdma.interfaces.IDatasource;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import fr.soleil.passerelle.cdma.actor.CDMADataItemSelector;
import fr.soleil.passerelle.cdma.actor.CDMADataSetReader;
import fr.soleil.passerelle.cdma.actor.CDMAShapeFilter;

/**
 * Basic tests for actors that access CDMA files and items.
 * 
 * @author erwin
 *
 */
public class CDMAActorFilterTest extends TestCase {
  
  private static final String NOMATCH_SINK = "noMatchSink" +
  		"";
  private static final String MATCH_SINK = "matchSink";
  private static final String NXS_READER = "NxsReader";

  private static final String NOMATCH_SINK_INPUT = NOMATCH_SINK+".input";
  private static final String MATCH_SINK_INPUT = MATCH_SINK+".input";
  private static final String NXS_READER_OUTPUT = NXS_READER+".output";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Factory.getLogger().setLevel(Level.WARNING);
    Factory.setDictionariesFolder("CDMA_Dictionaries");
    Factory.setActiveView("DATA_REDUCTION");
  }
  
  public void testCdmaFactory() {
    List<IDatasource> list = Factory.getDatasources();
    assertNotNull("CDMA plugins/datasources not found", list);
    assertEquals("Wrong number of CDMA plugins/datasources found", 1, list.size());
    assertEquals("SoleilNexus plugin not found", "SoleilNeXus", list.get(0).getFactoryName());
  }

  protected Flow _buildAndRunFilterFlow(String testName, String itemName, boolean useLogicalLookup, String requiredShape) throws Exception {
    Flow flow = new Flow(testName,null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow,"director"));
    
    CDMADataSetReader source = new CDMADataSetReader(flow,NXS_READER);
    CDMADataItemSelector itemSelector = new CDMADataItemSelector(flow, "ItemSelector");
    CDMAShapeFilter filter = new CDMAShapeFilter(flow, "Filter");
    DevNullActor matchSink = new DevNullActor(flow, MATCH_SINK);
    DevNullActor noMatchSink = new DevNullActor(flow, NOMATCH_SINK);
    
    flow.connect(source, itemSelector);
    flow.connect(itemSelector, filter);
    flow.connect(filter.outputNoMatch, noMatchSink.input);
    flow.connect(filter.outputMatch, matchSink.input);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("NxsReader.DataSet URI", "C:/data/workspaces/CDMA-plain/fr.soleil.passerelle.cdma.actor.test/CDMA_samples/FlyscanSwing_2011-04-11_15-44-11.nxs");
    props.put("ItemSelector.Item name",itemName);
    props.put("ItemSelector.Logical selection mode",Boolean.toString(useLogicalLookup));
    props.put("Filter.Required shape",requiredShape);
    flowMgr.executeBlockingLocally(flow,props);
    
    return flow;
  }
  
  public void testPhysicalAllWildCards() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 1L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 0L)
    .assertFlow(
        _buildAndRunFilterFlow("testPhysicalAllWildCards","Flyscan_01_8p8mgml/scan_data/channel1",false,"*,*")
    );
  }

  public void testLogicalAllWildCards() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 1L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 0L)
    .assertFlow(
        _buildAndRunFilterFlow("testLogicalAllWildCards","scan:data:images",true,"*,*,*")
    );
  }
  
  public void testPhysicalMixedShapeSpec1() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 1L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 0L)
    .assertFlow(
        _buildAndRunFilterFlow("testPhysicalMixedShapeSpec1","Flyscan_01_8p8mgml/scan_data/channel1",false,"10,*")
    );
  }

  public void testPhysicalMixedShapeSpec2() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 1L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 0L)
    .assertFlow(
        _buildAndRunFilterFlow("testPhysicalMixedShapeSpec2","Flyscan_01_8p8mgml/scan_data/channel1",false,"*,39215")
    );
  }

  public void testPhysicalFullShapeSpec() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 1L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 0L)
    .assertFlow(
        _buildAndRunFilterFlow("testPhysicalFullShapeSpec","Flyscan_01_8p8mgml/scan_data/channel1",false,"10,39215")
    );
  }

  public void testPhysicalMixedShapeSpecNoMatch() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 0L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 1L)
    .assertFlow(
        _buildAndRunFilterFlow("testPhysicalMixedShapeSpecNoMatch","Flyscan_01_8p8mgml/scan_data/channel1",false,"*,39215,2")
    );
  }

  public void testPhysicalFullShapeSpecNoMatch() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 0L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 1L)
    .assertFlow(
        _buildAndRunFilterFlow("testPhysicalFullShapeSpecNoMatch","Flyscan_01_8p8mgml/scan_data/channel1",false,"10,39215,2")
    );
  }

  public void testPhysicalEmptyShapeSpec() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 1L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 0L)
    .assertFlow(
        _buildAndRunFilterFlow("testPhysicalEmptyShapeSpec","Flyscan_01_8p8mgml/scan_data/channel1",false,"")
    );
  }

  public void testLogicalMixedShapeSpec1() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 1L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 0L)
    .assertFlow(
        _buildAndRunFilterFlow("testLogicalMixedShapeSpec1","scan:data:images",true,"4,10,*")
    );
  }

  public void testLogicalMixedShapeSpec2() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 1L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 0L)
    .assertFlow(
        _buildAndRunFilterFlow("testLogicalMixedShapeSpec2","scan:data:images",true,"*,10,39215")
    );
  }

  public void testLogicalFullShapeSpec() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 1L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 0L)
    .assertFlow(
        _buildAndRunFilterFlow("testLogicalFullShapeSpec","scan:data:images",true,"4,10,39215")
    );
  }

  public void testLogicalMixedShapeSpecNoMatch() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 0L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 1L)
    .assertFlow(
        _buildAndRunFilterFlow("testLogicalMixedShapeSpecNoMatch","scan:data:images",true,"2,*,39215")
    );
  }

  public void testLogicalFullShapeSpecNoMatch() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 0L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 1L)
    .assertFlow(
        _buildAndRunFilterFlow("testLogicalFullShapeSpecNoMatch","scan:data:images",true,"10,39215,2")
    );
  }

  public void testLogicalEmptyShapeSpec() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(MATCH_SINK_INPUT, 1L)
    .expectMsgReceiptCount(NOMATCH_SINK_INPUT, 0L)
    .assertFlow(
        _buildAndRunFilterFlow("testLogicalEmptyShapeSpec","scan:data:images",true,"")
    );
  }
  
  public void testWrongDataObjectError() throws Exception {
    Flow flow = new Flow("testWrongDataObjectError",null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow,"director"));
    
    // no item selector, so the filter will receive a full DataSet, which doesn't have shape
    CDMADataSetReader source = new CDMADataSetReader(flow,NXS_READER);
    CDMAShapeFilter filter = new CDMAShapeFilter(flow, "Filter");
    DevNullActor matchSink = new DevNullActor(flow, MATCH_SINK);
    DevNullActor noMatchSink = new DevNullActor(flow, NOMATCH_SINK);
    DevNullActor errorSink = new DevNullActor(flow, "errorSink");
    
    flow.connect(source, filter);
    flow.connect(filter.outputNoMatch, noMatchSink.input);
    flow.connect(filter.outputMatch, matchSink.input);
    flow.connect(filter.errorPort, errorSink.input);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("NxsReader.DataSet URI", "C:/data/workspaces/CDMA-plain/fr.soleil.passerelle.cdma.actor.test/CDMA_samples/FlyscanSwing_2011-04-11_15-44-11.nxs");
    props.put("Filter.Required shape","3,4");
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    .expectMsgReceiptCount(errorSink, 1L)
    .expectMsgReceiptCount(matchSink, 0L)
    .expectMsgReceiptCount(noMatchSink, 0L)
    .assertFlow(flow);
  }

}
