/**
 * 
 */
package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
import ptolemy.actor.Director;
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
  public PasserelleDiagramFeatureProvider(PasserelleDiagramTypeProvider dtp) {
    super(dtp);
    setIndependenceSolver(dtp.getIndependenceSolver());
  }
  
  public PasserelleIndependenceSolver getPasserelleIndependenceSolver() {
    return (PasserelleIndependenceSolver) getIndependenceSolver();
  }
  
  @Override
  public IAddFeature getAddFeature(IAddContext context) {
    if (context.getNewObject() instanceof Actor) {
      return new ActorAddFeature(this);
    } else if (context.getNewObject() instanceof Relation) {
      return new ConnectionAddFeature(this);
    } else if (context.getNewObject() instanceof Director) {
      return new DirectorAddFeature(this);
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
  public IUpdateFeature getUpdateFeature(IUpdateContext context) {
     String boCategory = Graphiti.getPeService().getPropertyValue(context.getPictogramElement(), "__BO_CATEGORY");
     if ("ACTOR".equals(boCategory)) {
             return new ActorUpdateFeature(this);
     }
     return super.getUpdateFeature(context);
   } 

  @Override
  public IFeature[] getDragAndDropFeatures(IPictogramElementContext context) {
      // simply return all create connection features
      return getCreateConnectionFeatures();
  } 
  
  @Override
  public ICustomFeature[] getCustomFeatures(ICustomContext context) {
      return new ICustomFeature[] { new ActorConfigureFeature(this) };
  } 
}
