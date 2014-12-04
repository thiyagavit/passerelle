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
package eu.esrf.passerelle.python.service.activator;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.isencia.passerelle.edm.backend.service.common.AsynchronousService;

import eu.esrf.passerelle.python.api.EsrfRepositoryService;
import eu.esrf.passerelle.python.service.impl.PythonAdapter;

public class Activator implements BundleActivator {

  private static BundleContext context;
  private static Activator defaultInstance;

  private ServiceRegistration<AsynchronousService> service;
  private ServiceTracker repoSvcTracker;

  public static final Activator getDefaultInstance() {
    return defaultInstance;
  }

  static BundleContext getContext() {
    return context;
  }

  public void start(BundleContext bundleContext) throws Exception {
    defaultInstance = this;

    Activator.context = bundleContext;

    Hashtable<String, String> props = new Hashtable<String, String>();
    props.put("serviceName", PythonAdapter.SERVICE_NAME);
    service = context.registerService(AsynchronousService.class, new AsynchronousService(PythonAdapter.RESOURCE_NAME, new PythonAdapter()), props);

    repoSvcTracker = new ServiceTracker(bundleContext, EsrfRepositoryService.class.getName(), null);
    repoSvcTracker.open();
  }

  public void stop(BundleContext bundleContext) throws Exception {
    repoSvcTracker.close();
    service.unregister();
    service = null;
    Activator.context = null;
    defaultInstance = null;
  }

  public EsrfRepositoryService getRepositoryService() {
    try {
      if (repoSvcTracker == null) {
        return null;
      }

      return (EsrfRepositoryService) repoSvcTracker.waitForService(3000);
    } catch (InterruptedException ex) {
      return null;
    }
  }
}
