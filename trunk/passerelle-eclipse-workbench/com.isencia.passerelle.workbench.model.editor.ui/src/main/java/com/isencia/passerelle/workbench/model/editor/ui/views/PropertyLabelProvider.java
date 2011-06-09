package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import ptolemy.kernel.util.Attribute;

public class PropertyLabelProvider extends ColumnLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element == null) return "";
		if (element instanceof String) return "Name";
		final Attribute att = (Attribute)element;
		return att.getDisplayName();
	}

}
