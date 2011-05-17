package com.isencia.passerelle.workbench.model.editor.ui.editor;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.requests.CreationFactory;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemFactory;

public class PaletteMouseListener implements MouseMotionListener {
	private DrawerEditPart drawerFigure;

	public PaletteMouseListener(DrawerEditPart drawerFigure) {
		super();
		this.drawerFigure = drawerFigure;
	}

	@Override
	public void mouseDragged(MouseEvent me) {
//		addFavorite();

	}

	@Override
	public void mouseEntered(MouseEvent me) {
		addFavorite();
	}

	private void addFavorite() {
		PaletteItemFactory paletteItemFactory = PaletteItemFactory.get();
		CreationFactory config = paletteItemFactory.getSelectedItem();
		if (config != null) {
			Class type = (Class) config.getObjectType();
			drawerFigure.getDrawer().getLabel();
			if (type.equals(Flow.class)) {
				paletteItemFactory.addFavorite(((Flow) config.getNewObject())
						.getName(), (PaletteContainer) PaletteBuilder
						.getFavoriteGroup(drawerFigure.getDrawer().getLabel()));

			} else {
				paletteItemFactory.addFavorite(type.getName(),
						(PaletteContainer) PaletteBuilder
								.getFavoriteGroup(drawerFigure.getDrawer()
										.getLabel()));
			}
			paletteItemFactory.setSelectedItem(null);
		}
	}

	@Override
	public void mouseExited(MouseEvent me) {
//		addFavorite();

	}

	@Override
	public void mouseHover(MouseEvent me) {
//		addFavorite();

	}

	@Override
	public void mouseMoved(MouseEvent me) {
//		addFavorite();

	}

}
