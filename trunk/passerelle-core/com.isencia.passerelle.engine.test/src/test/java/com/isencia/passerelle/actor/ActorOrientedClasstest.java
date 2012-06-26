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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.engine.activator.TestFragmentActivator;
import com.isencia.passerelle.ext.ActorOrientedClassProvider;
import com.isencia.passerelle.ext.impl.TestAocProvider;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;

public class ActorOrientedClasstest extends TestCase {

  public void testParsingHelloSubModel() throws Exception {
    URL submodelResource = this.getClass().getResource("/helloSubModel.moml");
    MoMLParser parser = new MoMLParser();
    Flow e = (Flow) parser.parse(null, submodelResource);
    assertNotNull("did not load an entity",e);
    assertTrue("the thing we loaded is not a class",e.isClassDefinition());
  }
  
  public void testRegisteringProviderAndParsingParentModel() throws Exception {
    TestAocProvider aocProvider = new TestAocProvider();
    
    @SuppressWarnings("rawtypes")
    ServiceRegistration serviceRegistration = TestFragmentActivator.getInstance().getBundleContext().registerService(ActorOrientedClassProvider.class.getName(), aocProvider, null);
    
    URL submodelResource = this.getClass().getResource("/testWithSubModel.moml");
    MoMLParser parser = new MoMLParser();
    Flow flow = (Flow) parser.parse(null, submodelResource);
    
    serviceRegistration.unregister();
    
    FlowManager flowMgr = new FlowManager();

    Map<String, String> props = new HashMap<String, String>();
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount("Source.output", 1L)
    .expectMsgReceiptCount("Sink.input", 1L)
    .expectActorIterationCount("helloSubModel.Constant", 1L)
    .assertFlow(flow);
  }
  
}
