package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;
import ptolemy.kernel.Relation;

public class ConnectionAddFeature extends AbstractAddFeature {

  private static final IColorConstant CONNECTION_FOREGROUND = new ColorConstant(98, 131, 167);

  public ConnectionAddFeature(IFeatureProvider fp) {
    super(fp);
  }

  public PictogramElement add(IAddContext context) {
    IAddConnectionContext addConContext = (IAddConnectionContext) context;
    Relation addedRelation = (Relation) context.getNewObject();
    IPeCreateService peCreateService = Graphiti.getPeCreateService();

    // CONNECTION WITH POLYLINE
    Connection connection = peCreateService.createFreeFormConnection(getDiagram());
    connection.setStart(addConContext.getSourceAnchor());
    connection.setEnd(addConContext.getTargetAnchor());

    IGaService gaService = Graphiti.getGaService();
    Polyline polyline = gaService.createPolyline(connection);
    polyline.setLineWidth(2);
    polyline.setForeground(manageColor(CONNECTION_FOREGROUND));

    // create link and wire it
    link(connection, addedRelation);

    return connection;
  }

  public boolean canAdd(IAddContext context) {
    if (context instanceof IAddConnectionContext && context.getNewObject() instanceof Relation) {
      return true;
    }
    return false;
  }
}
