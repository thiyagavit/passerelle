package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.ui.palette.PaletteCustomizer;
import org.eclipse.gef.ui.palette.PaletteMessages;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.customize.PaletteDrawerFactory;
import org.eclipse.gef.ui.palette.customize.PaletteSeparatorFactory;
import org.eclipse.gef.ui.palette.customize.PaletteStackFactory;
import org.eclipse.swt.widgets.Shell;

import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class PasserellePaletteCustomizer extends PaletteCustomizer {
	public PasserellePaletteCustomizer(PaletteViewer paletteViewer) {
		super();
		this.paletteViewer = paletteViewer;
		this.paletteViewer.setCustomizer(this);
	}

	PaletteViewer paletteViewer;

	@Override
	public List getNewEntryFactories() {
		List list = new ArrayList(4);
		list.add(new PaletteSeparatorFactory());
		list.add(new PaletteStackFactory());
		list.add(new PaletteDrawerFactory() {

			@Override
			protected PaletteEntry createNewEntry(Shell shell) {

				PaletteContainer createNewEntry = PaletteBuilder
						.createFavoriteContainer(PaletteMessages.NEW_DRAWER_LABEL);

				PaletteBuilder.addFavoriteGroup(createNewEntry.getLabel(),
						createNewEntry);
				return createNewEntry;
			}

		});
		return list;
	}

	@Override
	public void revertToSaved() {

	}

	@Override
	public void save() {
		PaletteBuilder.synchFavorites(paletteViewer);
	}

}
