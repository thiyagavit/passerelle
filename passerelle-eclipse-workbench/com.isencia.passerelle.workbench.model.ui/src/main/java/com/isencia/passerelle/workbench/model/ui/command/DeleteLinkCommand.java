package com.isencia.passerelle.workbench.model.ui.command;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;

import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.editor.common.model.LinkHolder;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class DeleteLinkCommand extends Command implements IRefreshConnections {

  private Link link;
  private CompositeEntity parent;
  private LinkHolder linkHolder;
  /**
   * @param linkHolder the linkHolder to set
   */
  public void setLinkHolder(LinkHolder linkHolder) {
    this.linkHolder = linkHolder;
  }
  
  public void setParent(CompositeEntity p) {
    parent = p;
  }

  public DeleteLinkCommand(CompositeEntity p, Link link) {
    this();
    this.link = link;
    this.parent = p;
  }

  public DeleteLinkCommand() {
    super("DeleteLink");
  }

  public void execute() {
    doExecute();
  }

  protected void doExecute() {
    // Perform Change in a ChangeRequest so that all Listeners are notified
    parent.requestChange(new ModelChangeRequest(this.getClass(), link, "delete") {
      @SuppressWarnings("unchecked")
      @Override
      protected void _execute() throws Exception {
        try {

          if (link.getHead() instanceof Vertex && link.getTail() instanceof Vertex) {
            unlinkRelation(link.getRelation(), (NamedObj) link.getHead());

          } else {
            if (!(link.getHead() instanceof Vertex)) {
              unlinkRelation(link.getRelation(), (NamedObj) link.getHead());
            }
            if (!(link.getTail() instanceof Vertex)) {
              unlinkRelation(link.getRelation(), (NamedObj) link.getTail());
            }
          }
          linkHolder.removeLink(link);
        } catch (Exception e) {

        }
      }
    });

  }

  public void redo() {
    doExecute();
  }

  public void undo() {
    // Perform Change in a ChangeRequest so that all Listeners are notified
    parent.requestChange(new ModelChangeRequest(this.getClass(), link.getRelation(), "undo-delete") {
      @Override
      protected void _execute() throws Exception {

        if (link.getHead() instanceof Vertex && link.getTail() instanceof Vertex) {
          ((TypedIORelation) ((Vertex) link.getHead()).getContainer()).link(((TypedIORelation) ((Vertex) link.getTail()).getContainer()));

        } else {
          if (!(link.getHead() instanceof Vertex)) {
            ((IOPort) link.getHead()).link(link.getRelation());

          }
          if (!(link.getTail() instanceof Vertex)) {
            ((IOPort) link.getTail()).link(link.getRelation());
          }
        }
      }
    });

  }

  private void unlinkRelation(ComponentRelation connection, NamedObj namedObj) throws IllegalActionException {
    List linkedPortList = connection.linkedObjectsList();
    connection.unlinkAll();
    for (Iterator<Object> iterator = linkedPortList.iterator(); iterator.hasNext();) {
      Object temp = (Object) iterator.next();
      if (temp instanceof Port && !temp.equals(namedObj)) {
        ((Port) temp).link(connection);
      }
      if (temp instanceof TypedIORelation && (!(namedObj instanceof Vertex) || (!(((Vertex) namedObj).getContainer().equals(temp))))) {
        ((Relation) temp).link(connection);
      }

    }
  }
}