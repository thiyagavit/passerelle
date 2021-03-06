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
package fr.soleil.passerelle.actor.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.impl.DefaultModelElementClassProvider;
import fr.soleil.passerelle.actor.flow5.ForLoop;
import fr.soleil.passerelle.actor.flow5.ForLoopWithPortCfg;
import fr.soleil.passerelle.actor.flow5.SetParameter;
import fr.soleil.passerelle.actor.flow5.ValueListSplitter;

/**
 * Registers the ActorProvider as an OSGi service.
 * 
 * @author erwin
 *
 */
public class Activator implements BundleActivator {
  @SuppressWarnings("rawtypes")
  private ServiceRegistration apSvcReg;
  private BundleActivator testFragmentActivator;
  
  public void start(BundleContext context) throws Exception {
    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), 
        new DefaultModelElementClassProvider(
            ForLoop.class,
            ForLoopWithPortCfg.class,
            ValueListSplitter.class,
            SetParameter.class
            ), 
        null);
    
    try {
      Class<? extends BundleActivator> svcTester = (Class<? extends BundleActivator>) Class.forName("fr.soleil.passerelle.actor.activator.TestFragmentActivator");
      testFragmentActivator = svcTester.newInstance();
      testFragmentActivator.start(context);
    } catch (ClassNotFoundException e) {
      // ignore, means the test fragment is not present...
      // it's a dirty way to find out, but don't know how to discover fragment contribution in a better way...
    }
  }
  public void stop(BundleContext context) throws Exception {
    if (testFragmentActivator != null) {
      testFragmentActivator.stop(context);
    }
    apSvcReg.unregister();
  }
}
