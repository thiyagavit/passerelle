package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RootEditPart;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;

import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.editor.common.model.LinkHolder;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundIOFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundInputFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundOutputFigure;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

/**
 * <code>PortEditPart</code> is the EditPart for the Port model objects
 * 
 * @author Dirk Jacobs
 */
public class PortEditPart extends ActorEditPart {
  private boolean isInput;

  public PortEditPart(boolean isInput) {
    super();
    this.isInput = isInput;
  }

  @Override
  protected IFigure createFigure() {
    if (isInput)
      return new CompoundInputFigure(((IOPort) getModel()).getName(), getModel().getClass());
    else
      return new CompoundOutputFigure(((IOPort) getModel()).getName(), getModel().getClass());
  }

  public CompoundIOFigure getComponentFigure() {
    return (CompoundIOFigure) getFigure();
  }

  @Override
  protected List getModelSourceConnections() {
    if (isInput) {
      return getPortSourceConnections();
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  protected List getModelTargetConnections() {
    if (!isInput) {
      return getPortTargetConnections();
    }
    return Collections.EMPTY_LIST;
  }

  protected List getPortSourceConnections() {

    return (getDiagram().getLinkHolder().getLinks(getModel()));
  }

  protected List getPortTargetConnections() {

    return (getDiagram().getLinkHolder().getLinks(getModel()));
  }

  public Port getSourcePort(ConnectionAnchor anchor) {
    getLogger().trace("Get Source port  based on anchor");
    IOPort port = ((TypedIOPort) getModel());
    if (port.isInput()) {
      return port;
    }
    return null;

  }

  public Port getTargetPort(ConnectionAnchor anchor) {
    getLogger().trace("Get Target port  based on anchor");

    IOPort port = ((TypedIOPort) getModel());
    if (!port.isInput()) {
      return port;
    }
    return null;
  }

  public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connEditPart) {
    return getComponentFigure().getConnectionAnchor(CompoundInputFigure.INPUT_PORT_NAME);
  }

  public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connEditPart) {
    return getComponentFigure().getConnectionAnchor(CompoundOutputFigure.OUTPUT_PORT_NAME);

  }

}
