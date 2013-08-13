package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.ui.IWorkbenchPart;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.workbench.model.editor.ui.properties.ActorDialog;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public class ActorConfigureFeature extends AbstractCustomFeature {

  private boolean hasDoneChanges = false;

  public ActorConfigureFeature(PasserelleDiagramFeatureProvider fp) {
    super(fp);
  }
  
  @Override
  public PasserelleDiagramFeatureProvider getFeatureProvider() {
    return (PasserelleDiagramFeatureProvider) super.getFeatureProvider();
  }

  @Override
  public String getName() {
    return "Configure";
  }

  @Override
  public String getDescription() {
    return "Configure an actor or director";
  }

  @Override
  public boolean canExecute(ICustomContext context) {
    boolean ret = false;
    PictogramElement[] pes = context.getPictogramElements();
    if (pes != null && pes.length == 1) {
      String boCategory = Graphiti.getPeService().getPropertyValue(pes[0], "__BO_CATEGORY");
      if ("ACTOR".equals(boCategory) || "DIRECTOR".equals(boCategory)) {
        ret = true;
      }
    }
    return ret;
  }

  @Override
  public void execute(ICustomContext context) {
    PictogramElement[] pes = context.getPictogramElements();
    if (pes != null && pes.length == 1) {
      Object bo = getBusinessObjectForPictogramElement(pes[0]);
      if (bo instanceof NamedObj) {
        NamedObj modelElement = (NamedObj) bo;
         final ActorAttributesView view = (ActorAttributesView)EclipseUtils.getActivePage().findView(ActorAttributesView.ID);
         ActorDialog dialog = new ActorDialog((IWorkbenchPart)getDiagramEditor(), modelElement);
         dialog.open();
//         this.updatePictogramElement(pes[0]);
          this.hasDoneChanges = true;

        // String currentName = actor.getName();
        // // ask user for a new class name
        // String newName = DiagramUtils.askString(getName(), getDescription(), currentName);
        // if (newName != null && !newName.equals(currentName)) {
        // this.hasDoneChanges = true;
        // try {
        // actor.setName(newName);
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // this.updatePictogramElement(pes[0]);
        // }
      }
    }
  }

  @Override
  public boolean hasDoneChanges() {
    return this.hasDoneChanges;
  }
}