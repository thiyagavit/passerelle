package com.isencia.passerelle.workbench.model.editor.graphiti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.ErrorPort;

public class ActorAddFeature extends AbstractAddShapeFeature {

  private static final IColorConstant ACTOR_TEXT_FOREGROUND = IColorConstant.BLACK;

  private static final IColorConstant ACTOR_FOREGROUND = new ColorConstant(98, 131, 167);
  private static final IColorConstant ACTOR_BACKGROUND = new ColorConstant(187, 218, 247);
  private static final IColorConstant PORT_FOREGROUND = IColorConstant.BLACK;
  private static final IColorConstant PORT_BACKGROUND = IColorConstant.WHITE;
  private static final IColorConstant ERRORPORT_BACKGROUND = IColorConstant.RED;
  private static final IColorConstant CONTROLPORT_BACKGROUND = IColorConstant.BLUE;
  
  private static final Map<Class<? extends Port>, IColorConstant> portColours;
  
  static {
    portColours = new HashMap<Class<? extends Port>, IColorConstant>();
    portColours.put(ErrorPort.class, ERRORPORT_BACKGROUND);
    portColours.put(ControlPort.class, CONTROLPORT_BACKGROUND);
  }

  public ActorAddFeature(IFeatureProvider fp) {
    super(fp);
  }

  public boolean canAdd(IAddContext context) {
    // check if user wants to add an actor
    if (context.getNewObject() instanceof Actor) {
      // check if user wants to add to a diagram
      if (context.getTargetContainer() instanceof Diagram) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public PictogramElement add(IAddContext context) {
    Actor addedActor = (Actor) context.getNewObject();
    Diagram targetDiagram = (Diagram) context.getTargetContainer();

    // CONTAINER SHAPE WITH ROUNDED RECTANGLE
    IPeCreateService peCreateService = Graphiti.getPeCreateService();
    ContainerShape containerShape = peCreateService.createContainerShape(targetDiagram, true);

    // define a default size for the shape
    int width = 100;
    int height = 50;
    IGaService gaService = Graphiti.getGaService();
    RoundedRectangle roundedRectangle; // need to access it later

    {
      // create and set graphics algorithm
      roundedRectangle = gaService.createRoundedRectangle(containerShape, 5, 5);
      roundedRectangle.setForeground(manageColor(ACTOR_FOREGROUND));
      roundedRectangle.setBackground(manageColor(ACTOR_BACKGROUND));
      roundedRectangle.setLineWidth(2);
      gaService.setLocationAndSize(roundedRectangle, context.getX(), context.getY(), width, height);

      // if added Class has no resource we add it to the resource
      // of the diagram
      // in a real scenario the business model would have its own resource
      // if (addedActor.eResource() == null) {
      // getDiagram().eResource().getContents().add(addedActor);
      // }
      // create link and wire it
      link(containerShape, addedActor);
    }

    // SHAPE WITH LINE
    {
      // create shape for line
      Shape shape = peCreateService.createShape(containerShape, false);

      // create and set graphics algorithm
      Polyline polyline = gaService.createPolyline(shape, new int[] { 0, 20, width, 20 });
      polyline.setForeground(manageColor(ACTOR_FOREGROUND));
      polyline.setLineWidth(2);
    }

    // SHAPE WITH TEXT
    {
      // create shape for text
      Shape shape = peCreateService.createShape(containerShape, false);

      // create and set text graphics algorithm
      Text text = gaService.createText(shape, addedActor.getName());
      text.setForeground(manageColor(ACTOR_TEXT_FOREGROUND));
      text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
      // vertical alignment has as default value "center"
      text.setFont(gaService.manageDefaultFont(getDiagram(), false, true));
      gaService.setLocationAndSize(text, 0, 0, width, 20);

      // create link and wire it
      link(shape, addedActor);

      // add output port anchor
      int pIndex = 0;
      for (Port p : (List<Port>) addedActor.outputPortList()) {
        BoxRelativeAnchor anchor = peCreateService.createBoxRelativeAnchor(containerShape);
        anchor.setRelativeWidth(1);
        anchor.setRelativeHeight(0.2*(pIndex++));
        anchor.setReferencedGraphicsAlgorithm(roundedRectangle);
        link(anchor,p);
        // assign a rectangle graphics algorithm for the box relative anchor
        // note, that the rectangle is inside the border of the rectangle shape
        final Rectangle rectangle = gaService.createPlainRectangle(anchor);
        rectangle.setForeground(manageColor(PORT_FOREGROUND));
        IColorConstant portColour = portColours.get(p.getClass());
        portColour = portColour!=null ? portColour : PORT_BACKGROUND;
        rectangle.setBackground(manageColor(portColour));
        rectangle.setLineWidth(2);
        gaService.setLocationAndSize(rectangle, -12, -6, 12, 12);
      }
      pIndex = 0;
      for (Port p : (List<Port>) addedActor.inputPortList()) {
        BoxRelativeAnchor anchor = peCreateService.createBoxRelativeAnchor(containerShape);
        anchor.setRelativeWidth(0);
        anchor.setRelativeHeight(0.2*(pIndex++));
        anchor.setUseAnchorLocationAsConnectionEndpoint(true);
        anchor.setReferencedGraphicsAlgorithm(roundedRectangle);
        link(anchor,p);
        // assign a rectangle graphics algorithm for the box relative anchor
        // note, that the rectangle is inside the border of the rectangle shape
        final Rectangle rectangle = gaService.createPlainRectangle(anchor);
        rectangle.setForeground(manageColor(PORT_FOREGROUND));
        IColorConstant portColour = portColours.get(p.getClass());
        portColour = portColour!=null ? portColour : PORT_BACKGROUND;
        rectangle.setBackground(manageColor(portColour));
        rectangle.setLineWidth(2);
        gaService.setLocationAndSize(rectangle, 0, -6, 12, 12);
      }
      layoutPictogramElement(containerShape);
    }

    return containerShape;
  }
}