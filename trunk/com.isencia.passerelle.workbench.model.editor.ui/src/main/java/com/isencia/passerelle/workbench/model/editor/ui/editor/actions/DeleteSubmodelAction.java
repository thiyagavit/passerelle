package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.jface.action.Action;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.palette.SubModelPaletteItemDefinition;

public class DeleteSubmodelAction extends Action {
	private final String icon = "icons/delete.gif";
	private SubModelPaletteItemDefinition definition;

	public DeleteSubmodelAction(SubModelPaletteItemDefinition definition) {
		super();
		setId("DeleteSubModel");
		setText("Delete Submodel");
		this.definition = definition;
		Activator.getImageDescriptor(icon);
		setHoverImageDescriptor(Activator.getImageDescriptor(icon));
		setImageDescriptor(Activator.getImageDescriptor(icon));
		setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
		setEnabled(checkEnabled());
	}

	protected boolean checkEnabled() {

		return true;
	}

	@Override
	public void run() {
		if (definition != null) {
			Flow flowToDelete = definition.getFlow();
			System.out.println(flowToDelete.getName());
		}
	}

}
