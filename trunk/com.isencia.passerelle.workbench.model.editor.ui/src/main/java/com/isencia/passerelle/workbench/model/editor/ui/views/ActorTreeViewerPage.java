/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.DeleteSubmodelAction;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteGroup;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemFactory;

/**
 * This class represents the tree view page of the data view
 * 
 */
public class ActorTreeViewerPage extends ActorPalettePage {

	private ActionRegistry actionRegistry;

	public ActionRegistry getActionRegistry() {
		return actionRegistry;
	}

	public ActorTreeViewerPage(ActionRegistry actionRegistry) {
		super();
		this.actionRegistry = actionRegistry;
	}

	/**
	 * Creates the tree view
	 * 
	 * @param parent
	 *            the parent
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		TreeViewer treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		return treeViewer;
	}

	/**
	 * Initializes the data view page.
	 */
	protected void initPage() {
		createContextMenus();
		getTreeViewer().getTree().addMouseTrackListener(
				new MouseTrackAdapter() {

					public void mouseHover(MouseEvent event) {
						Widget widget = event.widget;
						if (widget == getTreeViewer().getTree()) {
							Point pt = new Point(event.x, event.y);
							TreeItem item = getTreeViewer().getTree().getItem(
									pt);
							getTreeViewer().getTree().setToolTipText(
									getTooltip(item));
						}
					}
				});
	}

	private void createContextMenus() {
		MenuManager menuManager = new ActorTreeMenuProvider(getTreeViewer());

		Menu menu = menuManager.createContextMenu(getTreeViewer().getControl());

		getTreeViewer().getControl().setMenu(menu);
		getSite().registerContextMenu("#Pop up", menuManager, //$NON-NLS-1$
				getSite().getSelectionProvider());//$NON-NLS-1$ 
	}

	private String getTooltip(TreeItem item) {
		if (item != null) {
			Object object = item.getData();
			StringBuffer tooltip = new StringBuffer();
			if (object instanceof PaletteItemDefinition) {
				return ((PaletteItemDefinition) object).getName();
			}
			if (object instanceof PaletteGroup) {
				return ((PaletteGroup) object).getName();
			}
			return "";
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Configures the tree viewer.
	 */
	protected void configTreeViewer() {
		ActorTreeProvider provider = new ActorTreeProvider();

		getTreeViewer().setContentProvider(provider);
		getTreeViewer().setLabelProvider(provider);

		initRoot();

		// add inline renaming support

		// Adds drag and drop support
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		Transfer[] transfers = new Transfer[] { TemplateTransfer.getInstance() };
		transfers = new Transfer[] { TemplateTransfer.getInstance() };
		DragTargetListener dragListener = new DragTargetListener(
				getTreeViewer());
		getTreeViewer().addDragSupport(ops, transfers, dragListener);

	}

	/**
	 * Initializes the root of the view
	 * 
	 */
	private void initRoot() {
		PaletteItemFactory builder = PaletteItemFactory.get();
		getTreeViewer().setInput(builder.getPaletteGroups().toArray());
	}

	/**
	 * The <code>Page</code> implementation of this <code>IPage</code> method
	 * disposes of this page's control (if it has one and it has not already
	 * been disposed). Disposes the visitor of the element
	 */
	public void dispose() {
		super.dispose();
	}

}