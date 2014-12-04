/* Copyright 2014 - iSencia Belgium NV - ESRF

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
package eu.esrf.passerelle.python.actor.activator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.edm.backend.actor.common.AsynchronousActorActivator;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.impl.DefaultModelElementClassProvider;

import eu.esrf.passerelle.python.actor.PythonActor;

/**
 * Remark : depends on a base class that is not yet in open source.
 * When we migrate the ESRF Python actor to the latest Passerelle EDM, this will change.
 * 
 * @author erwindl
 *
 */
public class Activator extends AsynchronousActorActivator {

  public final static String PYTHON_SERVICE_NAME = "eu.esrf.services.python.v2.6";
  private final static String[] SERVICE_NAMES = new String[] { PYTHON_SERVICE_NAME };

  private static Activator defaultInstance;

  @Override
  public String[] getAsynchronousServiceNames() {
    return SERVICE_NAMES;
  }

  private ServiceRegistration<ModelElementClassProvider> apSvcReg;

  public void start(BundleContext context) {
    super.start(context);

    apSvcReg = context.registerService(ModelElementClassProvider.class, 
        new DefaultModelElementClassProvider(PythonActor.class), null);
    defaultInstance = this;
  }

  public void stop(BundleContext context) {
    super.stop(context);
    apSvcReg.unregister();

    defaultInstance = null;
  }

  public static Activator getDefault() {
    return defaultInstance;
  }

  public static void initOutsideOSGi() {
    new Activator().start(null);
  }
}