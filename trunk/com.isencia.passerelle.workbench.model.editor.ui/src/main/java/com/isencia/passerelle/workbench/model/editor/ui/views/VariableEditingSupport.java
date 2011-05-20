package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;

import com.isencia.passerelle.workbench.model.editor.ui.Constants;
import com.isencia.passerelle.workbench.model.editor.ui.properties.CellEditorAttribute;
import com.isencia.passerelle.workbench.model.editor.ui.properties.EntityPropertySource;

/**
 * Editing support for parameter column.
 * 
 * @author gerring
 * 
 */
public class VariableEditingSupport extends EditingSupport {

	private static Logger logger = LoggerFactory
			.getLogger(VariableEditingSupport.class);

	private ActorAttributesView actorAttributesView;

	public VariableEditingSupport(ActorAttributesView part, ColumnViewer viewer) {
		super(viewer);
		this.actorAttributesView = part;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {

		final PropertyDescriptor desc;
		String contextId = null;
		if (element instanceof CellEditorAttribute) {
			return ((CellEditorAttribute) element).createCellEditor(getViewer()
					.getControl());
		} else if (element instanceof String) {
			contextId = Constants.HELP_BUNDLE_ID + ".name";
			desc = new TextPropertyDescriptor(VariableEditingSupport.class
					.getName()
					+ ".nameText", "Name");
		} else {
			contextId = showHelpSelectedParameter((Variable) element);
			desc = EntityPropertySource
					.getPropertyDescriptor((Variable) element);
		}

		CellEditor createPropertyEditor = desc
				.createPropertyEditor((Composite) getViewer().getControl());
		if (contextId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
					createPropertyEditor.getControl(), contextId);
			PlatformUI.getWorkbench().getHelpSystem().displayDynamicHelp();
		}
		return createPropertyEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {

		if (element instanceof String)
			return actorAttributesView.getActorName();

		final Variable param = (Variable) element;
		try {
			if (!param.isStringMode() && param.getToken() != null
					&& param.getToken() instanceof BooleanToken) {
				return ((BooleanToken) param.getToken()).booleanValue();
			}
		} catch (Exception ne) {
			logger.error("Cannot set read token from " + param.getName(), ne);
		}
		return param.getExpression();
	}

	public String showHelpSelectedParameter(Variable param) {

		Attribute attr = (Attribute) param;
		if (param.getContainer() != null) {
			String helpBundle = Constants.HELP_BUNDLE_ID;
			String actorName = param.getContainer().getClass().getName()
					.replace(".", "_");
			return helpBundle + "." + actorName + "_" + attr.getName();
		}
		return "";
	}

	@Override
	protected void setValue(Object element, Object value) {

		try {
			if (element instanceof String) {
				// Also uses a Command so that is undoable
				actorAttributesView.setActorName((String) value);

			} else {
				actorAttributesView.setAttributeValue(element, value);
			}

		} catch (Exception ne) {
			logger.error("Cannot set variable value " + value, ne);
		}
	}

}
