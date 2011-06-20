package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.gef.commands.Command;

import ptolemy.data.expr.Node;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class RenameCommand extends Command {
	private NamedObj model;
	private String oldName;
	private String newName;

	public void execute() {
		this.oldName = model.getName();
		try {
			this.model.setName(newName);
		} catch (IllegalActionException e) {
			
		} catch (NameDuplicationException e) {
			
		}
	}

	public void setModel(NamedObj model) {
		this.model = (NamedObj) model;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public void undo() {
		try {
			this.model.setName(oldName);
		} catch (IllegalActionException e) {
			
		} catch (NameDuplicationException e) {
			
		}
	}
}
