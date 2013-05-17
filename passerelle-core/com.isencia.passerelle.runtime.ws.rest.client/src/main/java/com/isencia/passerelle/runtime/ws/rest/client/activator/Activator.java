package com.isencia.passerelle.runtime.ws.rest.client.activator;

import java.util.Arrays;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.ws.rest.client.FlowRepositoryServiceRESTClient;

public class Activator implements BundleActivator {

  private FlowRepositoryServiceRESTClient repoSvc;
  private ServiceRegistration<FlowRepositoryService> repoSvcReg;
  private static Activator plugin;

  public void start(BundleContext context) throws Exception {
    String debugStr      = System.getProperty("com.isencia.passerelle.runtime.ws.rest.client.debug", "false");
    String resourceRootURL      = System.getProperty("com.isencia.passerelle.runtime.ws.rest.client.resourceURL", "http://localhost/rest/flows");
    Hashtable<String, String> svcProps = new Hashtable<String, String>();
    svcProps.put("debug", debugStr);
    svcProps.put("resourceURL", resourceRootURL);
    svcProps.put("type", "REST");
    
    repoSvc = new FlowRepositoryServiceRESTClient();
    repoSvc.init(svcProps);
    
    repoSvcReg = (ServiceRegistration<FlowRepositoryService>) 
        context.registerService(FlowRepositoryService.class.getName(), repoSvc, svcProps);

    plugin = this;
    
    
    String[] allFlowCodes = repoSvc.getAllFlowCodes();
    System.out.println("Flow codes :"+Arrays.toString(allFlowCodes));
    FlowHandle flow = repoSvc.getActiveFlow(allFlowCodes[0]);
    System.out.println("Active flow :"+flow);
    flow = repoSvc.getMostRecentFlow(allFlowCodes[0]);
    System.out.println("Most recent flow :"+flow);
    flow = repoSvc.getMostRecentFlow("polleke");
    System.out.println("Most recent flow :"+flow);
  }

  public void stop(BundleContext context) throws Exception {
    repoSvcReg.unregister();
    repoSvc = null;
  }
  
  public FlowRepositoryService getRepositoryService() {
    return repoSvc;
  }
  
  public static Activator getDefault() {
    return plugin;
  }
}