package com.isencia.passerelle.process.actor.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.validation.version.VersionSpecification;

public class Activator implements BundleActivator {
  private static Activator defaultInstance;

  private ServiceRegistration apSvcReg;
  
  public void start(BundleContext context) {

    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), new ModelElementClassProvider() {
      public Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
        return (Class<? extends NamedObj>) this.getClass().getClassLoader().loadClass(className);
      }
    }, null);
    defaultInstance = this;
  }

  public void stop(BundleContext context) {
    apSvcReg.unregister();

    defaultInstance = null;
  }

  public static Activator getDefault() {
    return defaultInstance;
  }

  public static void initOutSideOSGi() {
    new Activator().start(null);
  }
}
