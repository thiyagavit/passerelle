package com.isencia.passerelle.workbench.model.editor.ui;

import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import com.isencia.passerelle.project.repository.api.RepositoryService;
import com.isencia.passerelle.workbench.util.DialogUtils;

public class PasserellePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
  public static final String SUBMODEL_DRILLDOWN = "com.isencia.passerelle.submodel.drilldown";
  private String submodelPath;
  private DirectoryFieldEditor subModelRoot;

  public PasserellePreferencePage() {
    super();
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
    setDescription("Preferences for the workflow editor and for running the workflow.");
  }

  @Override
  protected void createFieldEditors() {
    final BooleanFieldEditor expert = new BooleanFieldEditor(PreferenceConstants.EXPERT, "Expert mode", getFieldEditorParent());
    addField(expert);

    subModelRoot = new DirectoryFieldEditor(RepositoryService.SUBMODEL_ROOT, "Submodel Root", getFieldEditorParent());
    addField(subModelRoot);
    final BooleanFieldEditor submodelDrillDown = new BooleanFieldEditor(SUBMODEL_DRILLDOWN, "Open submodel in separate editor", getFieldEditorParent());
    addField(submodelDrillDown);
  }

  public void init(IWorkbench workbench) {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    submodelPath = store.getString(RepositoryService.SUBMODEL_ROOT);
    if (submodelPath == null || submodelPath.trim().equals("")) {
      File userHome = new File(System.getProperty("user.home"));
      File defaultSubmodelPath = new File(userHome, ".passerelle/submodel-repository");
      submodelPath = System.getProperty(RepositoryService.SUBMODEL_ROOT, defaultSubmodelPath.getAbsolutePath());
      store.setValue(RepositoryService.SUBMODEL_ROOT, submodelPath);
    }
  }

  @Override
  public boolean performOk() {
    boolean result = super.performOk();
    if (result) {
      String newSubmodelRoot = Activator.getDefault().getPreferenceStore().getString(RepositoryService.SUBMODEL_ROOT);
      if (submodelPath != null && !submodelPath.equalsIgnoreCase(newSubmodelRoot)) {
        MessageBox dialog = new MessageBox(getFieldEditorParent().getShell(), SWT.ICON_WARNING | SWT.OK);
        dialog.setText("Submodel configuration change");
        dialog.setMessage("You must restart the workbench after changing the submodel root!");
        DialogUtils.centerDialog(getShell(), dialog.getParent());
        dialog.open();
      }
    }
    return result;
  }
  
  @Override
  public void setValid(boolean b) {
    super.setValid(b);
    if(!b) {
      if (StringUtils.isNotEmpty(subModelRoot.getErrorMessage())) {
        MessageBox dialog = new MessageBox(getFieldEditorParent().getShell(), SWT.ICON_ERROR | SWT.OK);
        dialog.setText("Submodel configuration error");
        dialog.setMessage("Submodel Root " + subModelRoot.getErrorMessage());
        DialogUtils.centerDialog(getShell(), dialog.getParent());
        dialog.open();
      }       
    }
  }
}
