package com.isencia.passerelle.runtime.repos.impl.filesystem.activator;

import java.io.File;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.runtime.repos.impl.filesystem.FlowRepositoryServiceImpl;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;

public class Activator implements BundleActivator {

  private FlowRepositoryService repoSvc;
  private ServiceRegistration<FlowRepositoryService> repoSvcReg;
  private static Activator plugin;

  public void start(BundleContext context) throws Exception {
    File userHome              = new File(System.getProperty("user.home"));
    File defaultRootFolderPath = new File(userHome, ".passerelle/passerelle-repository");
    String rootFolderPath      = System.getProperty("com.isencia.passerelle.repository.root", defaultRootFolderPath.getAbsolutePath());
    repoSvc = new FlowRepositoryServiceImpl(rootFolderPath);
    Hashtable<String, String> svcProps = new Hashtable<String, String>();
    svcProps.put("type", "FILE");
    repoSvcReg = (ServiceRegistration<FlowRepositoryService>) context.registerService(FlowRepositoryService.class.getName(), repoSvc, svcProps);

    plugin = this;
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
