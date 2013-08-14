package com.isencia.passerelle.workbench.model.editor.graphiti.feature;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Shape;
import com.isencia.passerelle.workbench.model.ui.command.SetConstraintCommand;
import ptolemy.kernel.util.NamedObj;

/**
 * We want to replicate basic graphical model info in the MOML, so when moving an element, it's location must be updated in the MOML as well.
 * 
 * @author erwin
 */
public class ModelElementMoveFeature extends DefaultMoveShapeFeature {

  public ModelElementMoveFeature(IFeatureProvider fp) {
    super(fp);
  }

  @Override
  protected void postMoveShape(IMoveShapeContext context) {
    super.postMoveShape(context);
    Shape s = context.getShape();
    Object bo = getBusinessObjectForPictogramElement(s);
    if(bo instanceof NamedObj) {
      NamedObj modelElement = (NamedObj)bo;
      SetConstraintCommand cmd = new SetConstraintCommand();
      cmd.setModel(modelElement);
      cmd.setLocation(new double[]{context.getX(), context.getY()});
      if(cmd.canExecute()) {
        cmd.execute();
      }
    } else {
      super.postMoveShape(context);
    }
  }
}
