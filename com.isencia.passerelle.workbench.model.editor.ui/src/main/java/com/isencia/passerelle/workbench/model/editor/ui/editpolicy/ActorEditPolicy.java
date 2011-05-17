package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Actor;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.AbstractNodeEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.VertexEditPart;
import com.isencia.passerelle.workbench.model.ui.command.CreateConnectionCommand;

/**
 * <code>ActorEditPolicy</code> creates commands for connection initiation and
 * completion. It uses the default feedback provided by the
 * <code><b>GraphicalNodeEditPolicy</b></code> base class
 * 
 * @author Dirk Jacobs
 * 
 */
public class ActorEditPolicy extends GraphicalNodeEditPolicy {

	private final static Logger logger = LoggerFactory
			.getLogger(ActorEditPolicy.class);
	private CreateConnectionCommand CreateConnectionCommand;
	private PasserelleModelMultiPageEditor editor;

	public PasserelleModelMultiPageEditor getEditor() {
		return editor;
	}

	private CreateConnectionCommand getCreateConnectionCommand() {
		if (CreateConnectionCommand == null) {
			return CreateConnectionCommand = new CreateConnectionCommand(editor);
		}
		return CreateConnectionCommand;
	}

	public ActorEditPolicy(PasserelleModelMultiPageEditor editor) {
		super();
		this.editor = editor;
	}

	public Logger getLogger() {
		return logger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
	 * getConnectionCreateCommand
	 * (org.eclipse.gef.requests.CreateConnectionRequest)
	 */
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		AbstractNodeEditPart editPart = getActorEditPart();
		if (getLogger().isDebugEnabled())
			getLogger().trace(
					"getConnectionCreateCommand for editPart : " + editPart);
		NamedObj port = null;
		if (editPart instanceof VertexEditPart) {
			port = (NamedObj) editPart.getModel();
		} else {
			ConnectionAnchor anchor = editPart
					.getSourceConnectionAnchor(request);
			port = (ComponentPort) ((ActorEditPart)editPart).getSourcePort(anchor);
		}
		if (port != null) {
			CreateConnectionCommand command = getCreateConnectionCommand();
			command.setSource(port);
			request.setStartCommand(command);
			return command;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
	 * getConnectionCompleteCommand
	 * (org.eclipse.gef.requests.CreateConnectionRequest)
	 */
	protected Command getConnectionCompleteCommand(
			CreateConnectionRequest request) {
		AbstractNodeEditPart editPart = getActorEditPart();
		if (getLogger().isDebugEnabled())
			getLogger().debug(
					"getConnectionCompleteCommand for editPart : " + editPart);
		NamedObj port = null;
		if (editPart instanceof VertexEditPart) {
			port = (NamedObj) editPart.getModel();
		} else {
			ConnectionAnchor anchor = editPart
					.getTargetConnectionAnchor(request);
			port = (ComponentPort) ((ActorEditPart)editPart).getTargetPort(anchor);
		}

		if (port != null) {
			CreateConnectionCommand command = (CreateConnectionCommand) request
					.getStartCommand();
			command.setTarget(port);
			return command;
		}
		return null;
	}

	protected Actor getActorModel() {
		return (Actor) getHost().getModel();
	}

	protected AbstractNodeEditPart getActorEditPart() {
		return (AbstractNodeEditPart) getHost();
	}

	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		return null;
	}

	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		return null;
	}

}
