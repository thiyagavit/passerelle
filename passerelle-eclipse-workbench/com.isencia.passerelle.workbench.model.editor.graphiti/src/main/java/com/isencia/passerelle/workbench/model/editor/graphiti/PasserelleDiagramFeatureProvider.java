/**
 * 
 */
package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
import ptolemy.kernel.Relation;
import com.isencia.passerelle.actor.Actor;

/**
 * @author delerw
 */
public class PasserelleDiagramFeatureProvider extends DefaultFeatureProvider {

  PasserelleIndependenceSolver passerelleIndependenceSolver;

  /**
   * @param dtp
   */
  public PasserelleDiagramFeatureProvider(IDiagramTypeProvider dtp) {
    super(dtp);
    passerelleIndependenceSolver = new PasserelleIndependenceSolver();
    setIndependenceSolver(passerelleIndependenceSolver);
  }

  @Override
  public IAddFeature getAddFeature(IAddContext context) {
    if (context.getNewObject() instanceof Actor) {
      return new ActorAddFeature(this);
    } else if (context.getNewObject() instanceof Relation) {
      return new ConnectionAddFeature(this);
    }
    return super.getAddFeature(context);
  }

  @Override
  public ICreateFeature[] getCreateFeatures() {
    return new ICreateFeature[] { new ActorCreateFeature(this) };
  }
  
  @Override
  public ICreateConnectionFeature[] getCreateConnectionFeatures() {
     return new ICreateConnectionFeature[] { 
         new ConnectionCreateFeature(this) };
  } 

  @Override
  public IFeature[] getDragAndDropFeatures(IPictogramElementContext context) {
      // simply return all create connection features
      return getCreateConnectionFeatures();
  } 
}
