package com.isencia.passerelle.runtime.ws.rest.server.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;

public class Activator implements BundleActivator {

  static final String SERVICE_FILTER = "(&("+Constants.OBJECTCLASS+"="+FlowRepositoryService.class.getName()+")(type=FILE))";
  
  private static BundleContext context;
  private static Activator instance;
  
  private ServiceTracker<Object, Object> flowReposSvcTracker;

  private FlowRepositoryService flowReposSvc;

  public void start(BundleContext bundleContext) throws Exception {
    Activator.context = bundleContext;
    Activator.instance = this;
    Filter filter = context.createFilter(SERVICE_FILTER);
    flowReposSvcTracker = new ServiceTracker<Object, Object>(bundleContext, filter, createSvcTrackerCustomizer());
    flowReposSvcTracker.open();
  }

  public void stop(BundleContext bundleContext) throws Exception {
    flowReposSvcTracker.close();
    Activator.context = null;
    Activator.instance = null;
  }
  
  public static Activator getInstance() {
    return instance;
  }
  
  public FlowRepositoryService getFlowReposSvc() {
    return flowReposSvc;
  }

  private ServiceTrackerCustomizer<Object, Object> createSvcTrackerCustomizer() {
    return new ServiceTrackerCustomizer<Object, Object>() {
      public void removedService(ServiceReference<Object> ref, Object svc) {
        synchronized (Activator.this) {
          if (svc != Activator.this.flowReposSvc) {
            return;
          } else {
            Activator.this.flowReposSvc = null;
          }
          context.ungetService(ref);
        }
      }

      public void modifiedService(ServiceReference<Object> ref, Object svc) {
      }

      public Object addingService(ServiceReference<Object> ref) {
        Object svc = context.getService(ref);
        synchronized (Activator.this) {
          if (Activator.this.flowReposSvc == null) {
            Activator.this.flowReposSvc = (FlowRepositoryService) svc;
          }
        }
        return svc;
      }
    };
  }
}
