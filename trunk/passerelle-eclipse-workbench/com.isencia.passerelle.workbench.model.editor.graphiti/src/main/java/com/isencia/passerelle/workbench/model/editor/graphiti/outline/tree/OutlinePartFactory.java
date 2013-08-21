package com.isencia.passerelle.workbench.model.editor.graphiti.outline.tree;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.NamedObj;


public class OutlinePartFactory implements EditPartFactory {
	private Set<OutlineEditPart> parts = new HashSet<OutlineEditPart>();

  public Set<OutlineEditPart> getParts() {
		return parts;
	}

	public EditPart createEditPart(EditPart context, Object model) {
		OutlineEditPart editPart = null;
		if (model instanceof CompositeActor) {
			editPart = new OutlineContainerEditPart(context, (CompositeActor) model);
		} else if(model instanceof NamedObj){
		  editPart = new OutlineEditPart((NamedObj) model);
		}
		parts.add(editPart);
		return editPart;
	}

}
