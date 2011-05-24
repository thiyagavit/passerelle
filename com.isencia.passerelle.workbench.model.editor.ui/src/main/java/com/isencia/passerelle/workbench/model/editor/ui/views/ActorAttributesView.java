package com.isencia.passerelle.workbench.model.editor.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.actor.gui.PasserelleConfigurer;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.Constants;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.DeleteAttributeHandler;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.DirectorEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.properties.ActorGeneralSection;
import com.isencia.passerelle.workbench.model.editor.ui.properties.NamedObjComparator;
import com.isencia.passerelle.workbench.model.ui.command.AttributeCommand;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

/**
 * Optional replacement for PropertiesView which renders the actor properties
 * more simply but with more customizable rules.
 * 
 * @author gerring
 * 
 */
public class ActorAttributesView extends ViewPart implements
		ISelectionListener, CommandStackEventListener {

	@Override
	public void setSite(IWorkbenchPartSite site) {
		// TODO Auto-generated method stub
		super.setSite(site);
	}

	public void setActor(NamedObj actor) {
		this.actor = actor;
	}

	private static Logger logger = LoggerFactory
			.getLogger(ActorAttributesView.class);

	public static final String ID = "com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView"; //$NON-NLS-1$

	private TableViewer viewer;
	private NamedObj actor;
	private boolean addedListener = false;
	private IWorkbenchPart part;

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof PasserelleModelMultiPageEditor) {
			this.part = part;
			if (updateSelection(selection))
				return;
			clear();
		}
	}

	protected boolean updateSelection(final ISelection selection) {

		if (!(selection instanceof StructuredSelection))
			return false;

		final Object sel = ((StructuredSelection) selection).getFirstElement();

		if (sel instanceof ActorEditPart || sel instanceof DirectorEditPart) {
			this.actor = sel instanceof ActorEditPart ? (NamedObj) ((ActorEditPart) sel)
					.getActor()
					: (NamedObj) ((DirectorEditPart) sel).getDirectorModel();

			if (!addedListener
					&& part instanceof PasserelleModelMultiPageEditor) {
				// ((PasserelleModelMultiPageEditor)part).getEditor().getEditDomain().getCommandStack().addCommandStackEventListener(this);
				// addedListener = true;
			}
			if (actor != null) {
				final List<Parameter> parameterList = new ArrayList<Parameter>(
						7);
				Iterator<Parameter> parameterIterator = actor.attributeList(
						Parameter.class).iterator();
				while (parameterIterator.hasNext()) {
					Parameter parameter = (Parameter) parameterIterator.next();
					if (PasserelleConfigurer.isVisible(actor, parameter)) {
						parameterList.add(parameter);
					}
				}

				createTableModel(parameterList);
			}
			return true;
		}

		return false;
	}

	private void createTableModel(final List<Parameter> parameterList) {

		if (parameterList != null)
			Collections.sort(parameterList, new NamedObjComparator());
		try {
			viewer.setContentProvider(new IStructuredContentProvider() {
				@Override
				public void dispose() {

				}

				@Override
				public void inputChanged(Viewer viewer, Object oldInput,
						Object newInput) {
				}

				@Override
				public Object[] getElements(Object inputElement) {
					if (parameterList == null || parameterList.isEmpty())
						return new Parameter[] {};
					final List<Object> ret = new ArrayList<Object>(
							parameterList.size() + 1);
					ret.add(actor.getName());
					ret.addAll(parameterList);
					return ret.toArray(new Object[ret.size()]);
				}
			});

			viewer.setInput(new Object());
			viewer.refresh();
		} catch (Exception e) {

		}
	}

	public void clear() {
		this.actor = null;
		if (part != null && part instanceof PasserelleModelMultiPageEditor) {
			((PasserelleModelMultiPageEditor) part).getEditor().getEditDomain()
					.getCommandStack().removeCommandStackEventListener(this);
		}
		this.part = null;
		this.addedListener = false;
		createTableModel(null);
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		this.viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);

		createColumns(viewer);
		viewer.setUseHashlookup(true);
		viewer.setColumnProperties(new String[] { "Property", "Value" });

		createActions();
		createPopupMenu();
		if (getSite() != null)
			getSite().getWorkbenchWindow().getSelectionService()
					.addSelectionListener(this);

		viewer.getTable().addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.F1) {
					// showHelpSelectedParameter();
				}
				if (e.character == SWT.DEL) {
					try {
						deleteSelectedParameter();
					} catch (IllegalActionException e1) {
						logger.error("Cannot delete ", e1);
					}
				}
			}
		});

		try {
			this.part = EclipseUtils.getActivePage().getActiveEditor();
			updateSelection(EclipseUtils.getActivePage().getSelection());

		} catch (Throwable ignored) {
			// There might not be a selection or page.
		}
	}

	private void createColumns(TableViewer viewer) {

		final TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT,
				0);

		name.getColumn().setText("Property");
		name.getColumn().setWidth(200);
		name.setLabelProvider(new PropertyLabelProvider());

		final TableViewerColumn value = new TableViewerColumn(viewer, SWT.LEFT,
				1);

		value.getColumn().setText("Value");
		value.getColumn().setWidth(700);
		value.setLabelProvider(new VariableLabelProvider(this));
		value.setEditingSupport(new VariableEditingSupport(this, viewer));
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the menu.
	 */
	private void createPopupMenu() {
		MenuManager menuMan = new MenuManager();
		menuMan.add(new Action("Delete Attribute", Activator
				.getImageDescriptor("icons/delete_attribute.gif")) {
			public void run() {
				(new DeleteAttributeHandler()).run(null);
			}
		});

		viewer.getControl().setMenu(
				menuMan.createContextMenu(viewer.getControl()));
	}

	@Override
	public void setFocus() {
		if (!viewer.getTable().isDisposed()) {
			viewer.getTable().setFocus();
		}
	}

	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(this);
		if (part != null && part instanceof PasserelleModelMultiPageEditor) {
			((PasserelleModelMultiPageEditor) part).getEditor().getEditDomain()
					.getCommandStack().removeCommandStackEventListener(this);
		}
		super.dispose();
	}

	@Override
	public void stackChanged(CommandStackEvent event) {
		viewer.refresh();
	}

	public String getActorName() {
		return actor.getName();
	}

	public Class getActorClass() {
		if (actor == null) {
			return null;
		}
		return actor.getClass();
	}

	public void setActorName(final String name) {
		try {
			ActorGeneralSection.setName(actor, name);
		} catch (Exception ne) {
			logger.error("Cannot set name as " + name, ne);
		}
	}

	public void deleteSelectedParameter() throws IllegalActionException {

		final ISelection sel = viewer.getSelection();
		if (sel != null && sel instanceof StructuredSelection) {
			final StructuredSelection s = (StructuredSelection) sel;
			final Object o = s.getFirstElement();
			if (o instanceof String)
				return; // Cannot delete name
			if (o instanceof Attribute) {
				setAttributeValue(o, null);
			}
		}
	}

	public void setAttributeValue(Object element, Object value)
			throws IllegalActionException {

		final PasserelleModelMultiPageEditor ed = (PasserelleModelMultiPageEditor) this.part;
		final AttributeCommand cmd = new AttributeCommand(viewer, element,
				value);
		ed.getEditor().getEditDomain().getCommandStack().execute(cmd);
		ed.refreshActions();

	}

}
