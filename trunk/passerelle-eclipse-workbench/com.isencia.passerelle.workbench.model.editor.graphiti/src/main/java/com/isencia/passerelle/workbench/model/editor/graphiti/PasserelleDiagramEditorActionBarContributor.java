package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.ui.editor.DiagramEditorActionBarContributor;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.RunAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.StopAction;

public class PasserelleDiagramEditorActionBarContributor extends DiagramEditorActionBarContributor {

  @Override
  protected void buildActions() {
    super.buildActions();
  }
  
  @Override
  public void contributeToToolBar(IToolBarManager toolbarManager) {
    toolbarManager.add(new RunAction());
    toolbarManager.add(new StopAction());

    toolbarManager.add(new Separator());
    super.contributeToToolBar(toolbarManager);
  }
}
