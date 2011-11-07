package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PartInitException;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.palette.SubModelPaletteItemDefinition;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public class EditSubmodelAction extends Action {
	private final String icon = "icons/edit.gif";
	private SubModelPaletteItemDefinition definition;

	public EditSubmodelAction(SubModelPaletteItemDefinition definition) {
		super();
		setId("EditSubModel");
		setText("Edit Submodel");
		this.definition = definition;
		Activator.getImageDescriptor(icon);
		setHoverImageDescriptor(Activator.getImageDescriptor(icon));
		setImageDescriptor(Activator.getImageDescriptor(icon));
		setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
		setEnabled(checkEnabled());
	}

	protected boolean checkEnabled() {

		return definition.getPath() != null && definition.getWorkSpace() != null;
	}

	@Override
	public void run() {
		if (definition != null) {
			try {

				EclipseUtils.openEditor(new SubModelFile(new Path(definition.getPath()),definition.getWorkSpace()));
			} catch (PartInitException e) {

			}


		}
	}

}
