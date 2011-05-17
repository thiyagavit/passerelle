package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.ui.palette.PaletteCustomizer;
import org.eclipse.gef.ui.palette.PaletteMessages;
import org.eclipse.gef.ui.palette.customize.DrawerEntryPage;
import org.eclipse.gef.ui.palette.customize.EntryPage;
import org.eclipse.gef.ui.palette.customize.PaletteDrawerFactory;
import org.eclipse.gef.ui.palette.customize.PaletteSeparatorFactory;
import org.eclipse.gef.ui.palette.customize.PaletteStackFactory;
import org.eclipse.swt.widgets.Shell;

import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PasserelleEntryPage;

public class PasserellePaletteCustomizer extends PaletteCustomizer {
	@Override
	public List getNewEntryFactories() {
		List list = new ArrayList(4);
		list.add(new PaletteSeparatorFactory());
		list.add(new PaletteStackFactory());
		list.add(new PaletteDrawerFactory() {

			@Override
			protected PaletteEntry createNewEntry(Shell shell) {

				PaletteEntry createNewEntry = PaletteBuilder.createFavoriteContainer(PaletteMessages.NEW_DRAWER_LABEL);
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

//	public EntryPage getPropertiesPage(PaletteEntry entry) {
//		if (entry instanceof PaletteDrawer) {
//			return new DrawerEntryPage();
//		}
//		return new PasserelleEntryPage();
//	}

	@Override
	public void save() {
		PaletteBuilder.synchFavorites();
	}

}
