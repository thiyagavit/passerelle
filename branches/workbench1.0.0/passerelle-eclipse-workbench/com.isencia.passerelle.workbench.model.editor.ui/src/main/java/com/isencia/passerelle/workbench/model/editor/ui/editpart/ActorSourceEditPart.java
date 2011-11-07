package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;

import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;

public class ActorSourceEditPart extends ActorEditPart {
	
	public final static Color ACTOR_BACKGROUND_COLOR = new Color(null,138,226,52);

	@Override
	protected IFigure createFigure() {
		ActorFigure createFigure = (ActorFigure) super.createFigure();
		if (createFigure.getColor().equals(createFigure.getDefaultColor()))
			createFigure.setBackgroundColor(ACTOR_BACKGROUND_COLOR);
		return createFigure;
	}

}
