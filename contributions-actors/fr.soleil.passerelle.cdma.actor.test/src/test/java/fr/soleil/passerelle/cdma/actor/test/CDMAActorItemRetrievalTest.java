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
import fr.soleil.passerelle.cdma.actor.CDMAArrayValueDumper;
import fr.soleil.passerelle.cdma.actor.CDMADataItemSelector;
import fr.soleil.passerelle.cdma.actor.CDMADataSetReader;
import fr.soleil.passerelle.cdma.actor.CDMADataSetSlicer;

/**
 * Basic tests for actors that access CDMA files and items.
 * 
 * @author erwin
 *
 */
public class CDMAActorItemRetrievalTest extends TestCase {
  
  private static final String ERROR_SINK = "errorSink";
  private static final String SINK = "sink";
  private static final String NXS_READER = "NxsReader";
  private static final String SLICER = "slicer";

  private static final String ERROR_SINK_INPUT = ERROR_SINK+".input";
  private static final String SINK_INPUT = SINK+".input";
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

  protected Flow _buildAndRunBasicItemRetrievalFlow(String testName, String itemName, boolean useLogicalLookup) throws Exception {
    Flow flow = new Flow(testName,null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow,"director"));
    
    CDMADataSetReader source = new CDMADataSetReader(flow,NXS_READER);
    CDMADataItemSelector itemSelector = new CDMADataItemSelector(flow, "ItemSelector");
    DevNullActor sink = new DevNullActor(flow, SINK);
    DevNullActor errorSink = new DevNullActor(flow, ERROR_SINK);
    
    flow.connect(source, itemSelector);
    flow.connect(itemSelector, sink);
    flow.connect(itemSelector.errorPort, errorSink.input);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("NxsReader.DataSet URI", "C:/data/workspaces/CDMA-plain/fr.soleil.passerelle.cdma.actor.test/CDMA_samples/FlyscanSwing_2011-04-11_15-44-11.nxs");
    props.put("ItemSelector.Item name",itemName);
    props.put("ItemSelector.Logical selection mode",Boolean.toString(useLogicalLookup));
    flowMgr.executeBlockingLocally(flow,props);
    
    return flow;
  }
  
  protected Flow _buildAndRunBasicItemRetrievalAndDumpFlow(String testName, String itemName, boolean useLogicalLookup) throws Exception {
    Flow flow = new Flow(testName,null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow,"director"));
    
    CDMADataSetReader source = new CDMADataSetReader(flow,NXS_READER);
    CDMADataItemSelector itemSelector = new CDMADataItemSelector(flow, "ItemSelector");
    CDMADataSetSlicer slicer = new CDMADataSetSlicer(flow, SLICER);
    CDMAArrayValueDumper arrayValueDumper = new CDMAArrayValueDumper(flow, "ArrayDumper");
    DevNullActor sink = new DevNullActor(flow, SINK);
    DevNullActor errorSink = new DevNullActor(flow, ERROR_SINK);
    
    flow.connect(source, itemSelector);
    flow.connect(itemSelector, slicer);
    flow.connect(slicer, arrayValueDumper);
    flow.connect(arrayValueDumper.output, slicer.nextPort);
    flow.connect(slicer.endPort, sink.input);
    flow.connect(itemSelector.errorPort, errorSink.input);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("NxsReader.DataSet URI", "C:/data/workspaces/passerelle-edm-branch-1_3/fr.soleil.passerelle.cdma.actor.test/CDMA_samples/FlyscanSwing_2011-04-11_15-44-11.nxs");
    props.put("ItemSelector.Item name",itemName);
    props.put("ItemSelector.Logical selection mode",Boolean.toString(useLogicalLookup));
    flowMgr.executeBlockingLocally(flow,props);
    
    return flow;
  }
  
  protected Flow _buildAndRunBasicItemRetrievalFlowOlof(String testName, String itemName, boolean useLogicalLookup) throws Exception {
    Flow flow = new Flow(testName,null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow,"director"));
    
    CDMADataSetReader source = new CDMADataSetReader(flow,NXS_READER);
    CDMADataItemSelector itemSelector = new CDMADataItemSelector(flow, "ItemSelector");
    DevNullActor sink = new DevNullActor(flow, SINK);
    DevNullActor errorSink = new DevNullActor(flow, ERROR_SINK);
    
    flow.connect(source, itemSelector);
    flow.connect(itemSelector, sink);
    flow.connect(itemSelector.errorPort, errorSink.input);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("NxsReader.DataSet URI", "C:/data/workspaces/CDMA-plain/fr.soleil.passerelle.cdma.actor.test/CDMA_samples/RhA1000.nxs");
    props.put("ItemSelector.Item name",itemName);
    props.put("ItemSelector.Logical selection mode",Boolean.toString(useLogicalLookup));
    flowMgr.executeBlockingLocally(flow,props);
    
    return flow;
  }
  
//  public void testPhysicalPathToItemOlof() throws Exception {
//    new FlowStatisticsAssertion()
//    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
//    .expectMsgReceiptCount(SINK_INPUT, 1L)
//    .expectMsgReceiptCount(ERROR_SINK_INPUT, 0L)
//    .assertFlow(
//    _buildAndRunBasicItemRetrievalFlowOlof("testPhysicalPathToItemOlof","Result/Raw data/Raw data",false)
//    );
//  }
//
//  
//  public void testPhysicalPathToItem() throws Exception {
//    new FlowStatisticsAssertion()
//    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
//    .expectMsgReceiptCount(SINK_INPUT, 1L)
//    .expectMsgReceiptCount(ERROR_SINK_INPUT, 0L)
//    .assertFlow(
//    _buildAndRunBasicItemRetrievalFlow("testPhysicalPathToItem","Flyscan_01_8p8mgml/scan_data/channel1",false)
//    );
//  }
//
//  public void testLogicalPathToItem() throws Exception {
//    new FlowStatisticsAssertion()
//    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
//    .expectMsgReceiptCount(SINK_INPUT, 1L)
//    .expectMsgReceiptCount(ERROR_SINK_INPUT, 0L)
//    .assertFlow(
//    _buildAndRunBasicItemRetrievalFlow("testLogicalPathToItem","scan:data:images",true)
//    );
//  }

  public void testLogicalPathToItemAndDump() throws Exception {
    new FlowStatisticsAssertion()
    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
    .expectMsgReceiptCount(SINK_INPUT, 1L)
    .expectMsgReceiptCount(ERROR_SINK_INPUT,0L)
    .assertFlow(
    _buildAndRunBasicItemRetrievalAndDumpFlow("testLogicalPathToItem","scan:data:images",true)
    );
  }

//  public void testPhysicalPathToItemNotFound() throws Exception {
//    new FlowStatisticsAssertion()
//    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
//    .expectMsgReceiptCount(SINK_INPUT, 0L)
//    .expectMsgReceiptCount(ERROR_SINK_INPUT, 1L)
//    .assertFlow(
//        // skip a group level
//    _buildAndRunBasicItemRetrievalFlow("testPhysicalPathToItemNotFound","Flyscan_01_8p8mgml/channel1",false)
//    );
//  }
//
//  public void testLogicalPathToItemNotFound() throws Exception {
//    new FlowStatisticsAssertion()
//    .expectMsgSentCount(NXS_READER_OUTPUT, 1L)
//    .expectMsgReceiptCount(SINK_INPUT, 0L)
//    .expectMsgReceiptCount(ERROR_SINK_INPUT, 1L)
//    .assertFlow(
//    _buildAndRunBasicItemRetrievalFlow("testLogicalPathToItemNotFound","scan:data:somethingThatDoesNotExist",true)
//    );
//  }
}
