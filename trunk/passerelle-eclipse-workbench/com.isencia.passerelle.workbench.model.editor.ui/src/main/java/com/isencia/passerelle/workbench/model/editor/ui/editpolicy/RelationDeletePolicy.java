package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;

import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.workbench.model.ui.command.DeleteLinkCommand;

public class RelationDeletePolicy extends org.eclipse.gef.editpolicies.ConnectionEditPolicy {

  protected Command getDeleteCommand(GroupRequest request) {
    DeleteLinkCommand deleteCmd = new DeleteLinkCommand((CompositeEntity) getHost().getRoot().getContents().getModel(), (Link) getHost().getModel());

    return deleteCmd;
  }

}
