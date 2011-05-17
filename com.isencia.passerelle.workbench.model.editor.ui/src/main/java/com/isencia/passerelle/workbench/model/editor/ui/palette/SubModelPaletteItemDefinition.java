package com.isencia.passerelle.workbench.model.editor.ui.palette;

import org.eclipse.jface.resource.ImageDescriptor;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;

public class SubModelPaletteItemDefinition extends PaletteItemDefinition {
	public static ImageDescriptor IMAGE_SUBMODEL = Activator
			.getImageDescriptor("icons/flow.png");
	private Flow flow;

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public SubModelPaletteItemDefinition(Flow flow, PaletteGroup group,
			String id, String name) {
		super(group, id, name, Flow.class);
		setIcon(IMAGE_SUBMODEL);
		setFlow(flow);

	}

}
