package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class ReConnectLinkCommand extends Command {

  private DeleteLinkCommand deleteLinkCommand;
  private CreateConnectionCommand createLinkCommand;

  @Override
  public boolean canExecute() {
    // TODO Auto-generated method stub
    return (((source != null && target != null) || link != null) && (newTarget != null || newSource != null));
  }

  private static final Logger logger = LoggerFactory.getLogger(ReConnectLinkCommand.class);
  private IPasserelleMultiPageEditor editor;

  private Link link;

  public Link getLink() {
    return link;
  }

  public void setLink(Link vertexLink) {
    this.link = vertexLink;
  }

  protected NamedObj source;

  public IPasserelleMultiPageEditor getEditor() {
    return editor;
  }

  public void setEditor(IPasserelleMultiPageEditor editor) {
    this.editor = editor;
  }

  public NamedObj getSource() {
    return (NamedObj) link.getHead();
  }

  public NamedObj getTarget() {
    return (NamedObj) link.getTail();
  }

  protected NamedObj newSource;

  public NamedObj getNewSource() {
    return newSource;
  }

  public void setNewSource(NamedObj newSource) {
    this.newSource = newSource;
  }

  protected NamedObj newTarget;

  public NamedObj getNewTarget() {
    return newTarget;
  }

  public void setNewTarget(NamedObj newTarget) {
    this.newTarget = newTarget;
  }

  protected NamedObj target;
  private CompositeEntity parent;

  public ReConnectLinkCommand() {
    super("Reconnect");
  }

  public Logger getLogger() {
    return logger;
  }

  public void execute() {
    doExecute();
  }

  private Command delCommand;
  private CreateConnectionCommand conCommand;

  protected void doExecute() {
    // Perform Change in a ChangeRequest so that all Listeners are notified

    parent.requestChange(new ModelChangeRequest(this.getClass(), link, "reconnect") {
      @SuppressWarnings("unchecked")
      @Override
      protected void _execute() throws Exception {
        if (newSource != null || newTarget != null) {
          deleteLinkCommand = new DeleteLinkCommand(parent, link);
          deleteLinkCommand.doExecute();
          createLinkCommand = new CreateConnectionCommand(editor);
          if (newSource != null){
            createLinkCommand.setTarget(getTarget());
            createLinkCommand.setSource(newSource);
          }else{
            createLinkCommand.setTarget(newTarget);
            createLinkCommand.setSource((NamedObj) link.getHead());
          }
          createLinkCommand.doExecute();
        }
      }
    });

  }

  public void redo() {
    doExecute();
  }

  public void setParent(CompositeEntity p) {
    parent = p;
  }

  public void undo() {
    createLinkCommand.undo();
    deleteLinkCommand.undo();
  }

}
