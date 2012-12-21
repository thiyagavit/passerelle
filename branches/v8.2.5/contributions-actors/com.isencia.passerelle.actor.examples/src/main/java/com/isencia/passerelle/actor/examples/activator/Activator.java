package com.isencia.passerelle.actor.examples.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.ext.ModelElementClassProvider;

public class Activator implements BundleActivator {

  @SuppressWarnings("rawtypes")
  private ServiceRegistration apSvcReg;
  
  public void start(BundleContext context) throws Exception {
    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), new ActorProvider(), null);
  }

  public void stop(BundleContext context) throws Exception {
    apSvcReg.unregister();
  }

}
