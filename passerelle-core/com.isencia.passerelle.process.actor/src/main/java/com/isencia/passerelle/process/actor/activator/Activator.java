package com.isencia.passerelle.process.actor.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.project.repository.api.RepositoryService;
import com.isencia.passerelle.validation.version.VersionSpecification;

public class Activator implements BundleActivator {
  private static Activator defaultInstance;

  private ServiceRegistration apSvcReg;
  private BundleActivator testFragmentActivator;

  private ServiceTracker repoSvcTracker;

  public void start(BundleContext context) throws Exception {
    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), new ModelElementClassProvider() {
      public Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
        return (Class<? extends NamedObj>) this.getClass().getClassLoader().loadClass(className);
      }
    }, null);

    repoSvcTracker = new ServiceTracker(context, RepositoryService.class.getName(), null);
    repoSvcTracker.open();

    defaultInstance = this;
    try {
      Class<? extends BundleActivator> svcTester = (Class<? extends BundleActivator>) Class.forName("com.isencia.passerelle.process.actor.activator.TestFragmentActivator");
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
    defaultInstance = null;
  }

  public static Activator getDefault() {
    return defaultInstance;
  }

  public RepositoryService getRepositoryService() {
    try {
      if (repoSvcTracker == null) {
        return null;
      }

      return (RepositoryService) repoSvcTracker.waitForService(3000);
    } catch (InterruptedException ex) {
      return null;
    }
  }
}
