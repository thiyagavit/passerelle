package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import ptolemy.kernel.util.Location;
import com.isencia.passerelle.actor.general.Const;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.graphiti.util.DiagramFlowRepository;
import com.isencia.passerelle.workbench.model.editor.graphiti.util.DiagramUtils;

public class ActorCreateFeatureFromPaletteItemDefinition extends AbstractCreateFeature {

  private static final String TITLE = "Create actor";

  private static final String USER_QUESTION = "Enter new actor name";

  public ActorCreateFeatureFromPaletteItemDefinition(IFeatureProvider fp) {
    // set name and description of the creation feature
    super(fp, "Actor", "Create Actor");
  }

  public boolean canCreate(ICreateContext context) {
    return context.getTargetContainer() instanceof Diagram;
  }

  public Object[] create(ICreateContext context) {
    // ask user for actor name
    String actorName = DiagramUtils.askString(TITLE, USER_QUESTION, "");
    if (actorName == null || actorName.trim().length() == 0) {
      return EMPTY;
    }

    return create(context, actorName);
  }

  public Object[] create(ICreateContext context, String actorName) {
    try {
      Diagram d = getFeatureProvider().getDiagramTypeProvider().getDiagram();
      Flow flow = DiagramFlowRepository.getFlowForDiagram(d);

      // create actor
      Const constActor = new Const(flow, actorName);
      double[] locations = new double[] {context.getX(), context.getY()};
      new Location(constActor, "_location").setLocation(locations);

      // do the add
      addGraphicalRepresentation(context, constActor);

      // return newly created business object(s)
      return new Object[] { constActor };
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
