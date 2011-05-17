package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemFactory;

public class DropFavouriteListener extends DropTargetAdapter {
	private PaletteViewer paletteViewer;
	public DropFavouriteListener(PaletteViewer paletteViewer) {
		super();
		this.paletteViewer = paletteViewer;
	}

	public void drop(DropTargetEvent event) {
	
		
		PaletteItemFactory paletteItemFactory = PaletteItemFactory.get();
		paletteItemFactory.setSelectedItem(config);

		
	}

	private CreationFactory config;

	@Override
	public void dragEnter(DropTargetEvent event) {
		// TODO Auto-generated method stub
		super.dragEnter(event);
	}

	@Override
	public void dragLeave(DropTargetEvent event) {
		// TODO Auto-generated method stub
		super.dragLeave(event);
	}

	@Override
	public void dragOperationChanged(DropTargetEvent event) {
		// TODO Auto-generated method stub
		super.dragOperationChanged(event);
	}

	@Override
	public void dragOver(DropTargetEvent event) {
		// TODO Auto-generated method stub
		super.dragOver(event);
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
		// TODO Auto-generated method stub
		CreationFactory fact = getFactory(TemplateTransfer.getInstance()
				.getTemplate());
		if (fact != null) {
			config = fact;
		}
	}

	protected CreationFactory getFactory(Object template) {
		if (template instanceof CreationFactory)
			return ((CreationFactory) template);
		else
			return null;
	}

}