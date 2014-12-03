package com.isencia.passerelle.actor.examples.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.actor.examples.AddRemoveMessageHeader;
import com.isencia.passerelle.actor.examples.DelayWithExecutionTrace;
import com.isencia.passerelle.actor.examples.Forwarder;
import com.isencia.passerelle.actor.examples.HeaderFilter;
import com.isencia.passerelle.actor.examples.HelloPasserelle;
import com.isencia.passerelle.actor.examples.MultiInputsTracerConsole;
import com.isencia.passerelle.actor.examples.RandomMessageRouter;
import com.isencia.passerelle.actor.examples.TextSource;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.impl.DefaultModelElementClassProvider;

public class Activator implements BundleActivator {

  @SuppressWarnings("rawtypes")
  private ServiceRegistration apSvcReg;
  
  @SuppressWarnings("unchecked")
  public void start(BundleContext context) throws Exception {
    // pre Passerelle v8.4
//    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), new ActorProvider(), null);
    
    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), 
        new DefaultModelElementClassProvider(
            AddRemoveMessageHeader.class,
            DelayWithExecutionTrace.class,
            Forwarder.class,
            HeaderFilter.class,
            HelloPasserelle.class,
            MultiInputsTracerConsole.class,
            RandomMessageRouter.class,
            TextSource.class
            ), 
        null);
  }

  public void stop(BundleContext context) throws Exception {
    apSvcReg.unregister();
  }

}
