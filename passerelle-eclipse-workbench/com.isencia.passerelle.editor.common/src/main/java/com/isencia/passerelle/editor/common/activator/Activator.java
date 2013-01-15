package com.isencia.passerelle.editor.common.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.isencia.passerelle.ext.ActorOrientedClassProvider;

public class Activator implements BundleActivator {
  private ActorOrientedClassProviderTracker repoSvcTracker;
  private static Activator plugin;
  private ServiceRegistration factoryServiceRegistration;

  public static Activator getDefault() {
    return plugin;
  }

  public Activator() {
  }

  public void start(BundleContext context) throws Exception {

    plugin = this;
    repoSvcTracker = new ActorOrientedClassProviderTracker(context);
    repoSvcTracker.open();

  }

  public void stop(BundleContext context) throws Exception {

  }

  public ActorOrientedClassProvider getActorOrientedClassProvider() {
    // TODO use waitforservice
    return repoSvcTracker != null ? repoSvcTracker.repoService : null;
  }

  private static class ActorOrientedClassProviderTracker extends ServiceTracker {

    private ActorOrientedClassProvider repoService;

    public ActorOrientedClassProviderTracker(BundleContext context) {
      super(context, ActorOrientedClassProvider.class.getName(), null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
      repoService = (ActorOrientedClassProvider) super.addingService(reference);
      return repoService;
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
      super.removedService(reference, service);
      repoService = null;
    }
  }
}
