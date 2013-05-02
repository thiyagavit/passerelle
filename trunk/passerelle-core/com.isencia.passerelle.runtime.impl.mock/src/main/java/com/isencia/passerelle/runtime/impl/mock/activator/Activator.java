package com.isencia.passerelle.runtime.impl.mock.activator;

import java.io.File;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.runtime.impl.mock.FlowRepositoryServiceImpl;
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
    repoSvcReg = (ServiceRegistration<FlowRepositoryService>) context.registerService(FlowRepositoryService.class.getName(), repoSvc, null);

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
