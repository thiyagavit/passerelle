package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import ptolemy.actor.CompositeActor;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;

public class OutlinePartFactory implements EditPartFactory {
	private PasserelleModelMultiPageEditor editor;

	public OutlinePartFactory(PasserelleModelMultiPageEditor editor) {
		super();
		this.editor = editor;
	}

	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof CompositeActor)
			return new OutlineContainerEditPart(context, model,editor);
		return new OutlineEditPart(model);
	}

}
