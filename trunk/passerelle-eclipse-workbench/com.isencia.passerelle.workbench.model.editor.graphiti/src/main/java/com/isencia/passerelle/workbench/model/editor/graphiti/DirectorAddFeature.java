package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;
import ptolemy.actor.Director;
import ptolemy.kernel.util.NamedObj;

public class DirectorAddFeature extends AbstractAddShapeFeature {

  private static final int SHAPE_X_OFFSET = 8;
  private static final IColorConstant DIRECTOR_NAME_FOREGROUND = IColorConstant.BLACK;

  private static final IColorConstant DIRECTOR_FOREGROUND = new ColorConstant(98, 131, 167);
  private static final IColorConstant DIRECTOR_BACKGROUND = IColorConstant.ORANGE;

  public DirectorAddFeature(IFeatureProvider fp) {
    super(fp);
  }
  
  protected void link(PictogramElement pe, Object businessObject, String category) {
    super.link(pe, businessObject);
    // add property on the graphical model element, identifying the associated passerelle model element
    // so we can easily distinguish and identify them later on for updates etc
    if(businessObject instanceof NamedObj) {
      Graphiti.getPeService().setPropertyValue(pe, "__BO_NAME", ((NamedObj)businessObject).getName());
    }
    Graphiti.getPeService().setPropertyValue(pe, "__BO_CATEGORY", category);
    Graphiti.getPeService().setPropertyValue(pe, "__BO_CLASS", businessObject.getClass().getName());
  }

  public boolean canAdd(IAddContext context) {
    // check if user wants to add an actor
    if (context.getNewObject() instanceof Director) {
      // check if user wants to add to a diagram
      if (context.getTargetContainer() instanceof Diagram) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public PictogramElement add(IAddContext context) {
    Director addedDirector = (Director) context.getNewObject();
    Diagram targetDiagram = (Diagram) context.getTargetContainer();
    int xLocation = context.getX();
    int yLocation = context.getY();

    // CONTAINER SHAPE WITH ROUNDED RECTANGLE
    IPeCreateService peCreateService = Graphiti.getPeCreateService();
    ContainerShape containerShape = peCreateService.createContainerShape(targetDiagram, true);

    // define a default size for the shape
    int width = 100;
    int height = 60;
    IGaService gaService = Graphiti.getGaService();
    
    Rectangle invisibleRectangle; // need to access it later

    {
      invisibleRectangle = gaService.createInvisibleRectangle(containerShape);
      gaService.setLocationAndSize(invisibleRectangle, xLocation, yLocation, width + 15, height);

      // create and set graphics algorithm
      RoundedRectangle roundedRectangle = gaService.createRoundedRectangle(invisibleRectangle, 5, 5);
      roundedRectangle.setForeground(manageColor(DIRECTOR_FOREGROUND));
      roundedRectangle.setBackground(manageColor(DIRECTOR_BACKGROUND));
      roundedRectangle.setLineWidth(2);
      gaService.setLocationAndSize(roundedRectangle, SHAPE_X_OFFSET, 0, width, height);

      // if added Class has no resource we add it to the resource
      // of the diagram
      // in a real scenario the business model would have its own resource
      // if (addedActor.eResource() == null) {
      // getDiagram().eResource().getContents().add(addedActor);
      // }
      // create link and wire it
      link(containerShape, addedDirector, "DIRECTOR");
    }

    // SHAPE WITH LINE
    {
      // create shape for line
      Shape shape = peCreateService.createShape(containerShape, false);

      // create and set graphics algorithm
      Polyline polyline = gaService.createPolyline(shape, new int[] { SHAPE_X_OFFSET, 20, SHAPE_X_OFFSET+width, 20 });
      polyline.setForeground(manageColor(DIRECTOR_FOREGROUND));
      polyline.setLineWidth(2);
    }

    // SHAPE WITH actor name as TEXT
    {
      // create shape for text
      Shape shape = peCreateService.createShape(containerShape, false);

      // create and set text graphics algorithm
      Text text = gaService.createText(shape, addedDirector.getName());
      text.setForeground(manageColor(DIRECTOR_NAME_FOREGROUND));
      text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
      // vertical alignment has as default value "center"
      text.setFont(gaService.manageDefaultFont(getDiagram(), false, true));
      gaService.setLocationAndSize(text, SHAPE_X_OFFSET, 0, width, 20);

      // create link and wire it
      link(shape, addedDirector, "DIRECTOR");
    }
    
    // don't show director params in graphical model

    layoutPictogramElement(containerShape);
    return containerShape;
  }
}