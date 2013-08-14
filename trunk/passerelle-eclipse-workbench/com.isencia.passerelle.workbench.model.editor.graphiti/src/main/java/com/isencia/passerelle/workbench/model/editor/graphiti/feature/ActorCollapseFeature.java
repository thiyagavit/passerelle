package com.isencia.passerelle.workbench.model.editor.graphiti.feature;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.platform.IPlatformImageConstants;
import ptolemy.kernel.util.NamedObj;

public class ActorCollapseFeature extends AbstractCustomFeature {

  public ActorCollapseFeature(IFeatureProvider fp) {
    super(fp);
  }

  @Override
  public void execute(ICustomContext context) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean canExecute(ICustomContext context) {
    boolean ret = false;
    PictogramElement[] pes = context.getPictogramElements();
    if (pes != null && pes.length == 1) {
      Object bo = getBusinessObjectForPictogramElement(pes[0]);
      if (bo instanceof NamedObj) {
        ret = true;
      }
    }
    return ret;
  }

  @Override
  public String getName() {
    return "Collapse";
  }

  @Override
  public String getDescription() {
    return "Collapse Figure";
  }

  @Override
  public String getImageId() {
    return IPlatformImageConstants.IMG_EDIT_COLLAPSE;
  }

  @Override
  public boolean isAvailable(IContext context) {
    return true;
  }
}
