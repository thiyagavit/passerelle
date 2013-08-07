package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.context.IDoubleClickContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;

public class PasserelleToolBehaviorProvider extends DefaultToolBehaviorProvider {

  public PasserelleToolBehaviorProvider(IDiagramTypeProvider dtp) {
    super(dtp);
  }

  @Override
  public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
    ICustomFeature customFeature = new ActorConfigureFeature((PasserelleDiagramFeatureProvider) getFeatureProvider());
    // canExecute() tests especially if the context contains an actor
    if (customFeature.canExecute(context)) {
      return customFeature;
    } else {
      return super.getDoubleClickFeature(context);
    }
  }
}
