package com.isencia.passerelle.workbench.model.editor.ui;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.isencia.passerelle.editor.common.model.MomlClassRegistry;
import com.isencia.passerelle.ext.ActorOrientedClassProvider;
import com.isencia.passerelle.project.repository.api.RepositoryService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "com.isencia.passerelle.workbench.model.editor.ui";

  // The shared instance
  private static Activator plugin;
  private ServiceTracker repoSvcTracker;
  private ServiceRegistration submodelSvcReg;

  /**
   * The constructor
   */
  public Activator() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    repoSvcTracker = new ServiceTracker(context, RepositoryService.class.getName(), null);
    repoSvcTracker.open();

    MomlClassRegistry.setService(new MomlClassService());

    submodelSvcReg = context.registerService(ActorOrientedClassProvider.class.getName(), new SubmodelProvider(), null);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
   */
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
    repoSvcTracker.close();
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in relative path
   * 
   * @param path
   *          the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return getImageDescriptor(PLUGIN_ID, path);
  }

  public static ImageDescriptor getImageDescriptor(String plugin, String path) {
    return imageDescriptorFromPlugin(plugin, path);
  }

  public RepositoryService getRepositoryService() {
    try {
      RepositoryService repositoryService = (RepositoryService) (repoSvcTracker != null ? repoSvcTracker.waitForService(3000) : null);
      IPreferenceStore store = Activator.getDefault().getPreferenceStore();
      String submodelPath = store.getString(RepositoryService.SUBMODEL_ROOT);
      if (submodelPath == null || submodelPath.trim().equals("")) {
        submodelPath = System.getProperty(RepositoryService.SUBMODEL_ROOT, "C:/temp/submodel-repository");
        store.setValue(RepositoryService.SUBMODEL_ROOT, submodelPath);

      }
      File folder = new File(getPreferenceStore().getString(RepositoryService.SUBMODEL_ROOT));
      if (!folder.exists()) {
        folder.mkdirs();
      }
      repositoryService.setSubmodelFolder(folder);
      return repositoryService;
    } catch (InterruptedException e) {
      return null;
    }
  }
}
