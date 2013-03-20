package com.isencia.passerelle.actor.advanced.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.validation.version.VersionSpecification;

public class Activator implements BundleActivator {


  private ServiceRegistration apSvcReg;
  
  public void start(BundleContext context) throws Exception {
    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), new ModelElementClassProvider() {
      public Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
        return (Class<? extends NamedObj>) this.getClass().getClassLoader().loadClass(className);
      }
    }, null);
  }

  public void stop(BundleContext context) throws Exception {
    apSvcReg.unregister();
    
  }

}
