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
package fr.soleil.passerelle.extensions.test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;

/**
 * Some sample unit tests for Passerelle flow executions with example actors
 * 
 * @author erwin
 *
 */
public class SoleilExtensionsTest extends TestCase {
//	private Flow flow;
	private FlowManager flowMgr;

	protected void setUp() throws Exception {
//		flow = new Flow("unit test",null);
		flowMgr = new FlowManager();
	}

  public void testSynchronizingScriptActor() throws Exception {
    Reader in = new InputStreamReader(getClass().getResourceAsStream("/jython.moml"));
    Flow f = FlowManager.readMoml(in);
    Map<String, String> props = new HashMap<String, String>();
    flowMgr.executeBlockingLocally(f,props);
    // now check if all went as expected
    new FlowStatisticsAssertion()
    .expectMsgReceiptCount("Tracer.input", 1L)
    .expectMsgReceiptCount("Tracer_2.input", 0L)
    .assertFlow(f);
  }
}
