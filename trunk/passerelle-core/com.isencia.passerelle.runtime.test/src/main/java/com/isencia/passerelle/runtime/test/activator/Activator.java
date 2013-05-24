/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.runtime.test.activator;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;

public class Activator implements BundleActivator {
  
  static final String SERVICE_FILTER = "(&("+Constants.OBJECTCLASS+"="+FlowRepositoryService.class.getName()+"))";

	private static BundleContext context;
  private static Activator instance;
  
  private ServiceTracker<Object, Object> flowReposSvcTracker;

  private List<FlowRepositoryService> flowReposSvcs = new ArrayList<FlowRepositoryService>();

  private ServiceRegistration<?> testCmdProvider;

  public void start(BundleContext bundleContext) throws Exception {
    Activator.context = bundleContext;
    Activator.instance = this;
    Filter filter = context.createFilter(SERVICE_FILTER);
    flowReposSvcTracker = new ServiceTracker<Object, Object>(bundleContext, filter, createSvcTrackerCustomizer());
    flowReposSvcTracker.open();
    
    testCmdProvider = context.registerService(CommandProvider.class.getName(), new TestRunner(), null);

  }

  public void stop(BundleContext bundleContext) throws Exception {
    testCmdProvider.unregister();
    flowReposSvcTracker.close();
    Activator.context = null;
    Activator.instance = null;
  }
  
  public static Activator getInstance() {
    return instance;
  }
  
  public List<FlowRepositoryService> getFlowReposSvc() {
    return flowReposSvcs;
  }

  private ServiceTrackerCustomizer<Object, Object> createSvcTrackerCustomizer() {
    return new ServiceTrackerCustomizer<Object, Object>() {
      public void removedService(ServiceReference<Object> ref, Object svc) {
        synchronized (Activator.this) {
          if (!Activator.this.flowReposSvcs.contains(svc)) {
            return;
          } else {
            Activator.this.flowReposSvcs.remove(svc);
          }
          context.ungetService(ref);
        }
      }

      public void modifiedService(ServiceReference<Object> ref, Object svc) {
      }

      public Object addingService(ServiceReference<Object> ref) {
        Object svc = context.getService(ref);
        synchronized (Activator.this) {
          if (!Activator.this.flowReposSvcs.contains(svc)) {
           Activator.this.flowReposSvcs.add((FlowRepositoryService) svc);
          }
        }
        return svc;
      }
    };
  }
}
