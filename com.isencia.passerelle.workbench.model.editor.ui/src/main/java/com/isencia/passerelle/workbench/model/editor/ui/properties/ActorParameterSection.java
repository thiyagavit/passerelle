package com.isencia.passerelle.workbench.model.editor.ui.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AdvancedPropertySection;

public class ActorParameterSection extends AdvancedPropertySection {

	public ActorParameterSection() {
		super();

	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		
		super.setInput(part, selection);
	}

}
