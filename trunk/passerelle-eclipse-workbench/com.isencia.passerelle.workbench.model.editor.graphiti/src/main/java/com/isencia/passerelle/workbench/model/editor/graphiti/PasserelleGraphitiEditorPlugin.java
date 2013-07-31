package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class PasserelleGraphitiEditorPlugin extends AbstractUIPlugin {

  private static PasserelleGraphitiEditorPlugin pluginInstance;
  
  public PasserelleGraphitiEditorPlugin() {
    pluginInstance = this;
  }
  
  public static PasserelleGraphitiEditorPlugin getDefault() {
    return pluginInstance;
  }

  /**
   * Returns the Plugin-ID.
   * 
   * @return The Plugin-ID.
   */
  public static String getID() {
    return getDefault().getBundle().getSymbolicName();
  }

}
