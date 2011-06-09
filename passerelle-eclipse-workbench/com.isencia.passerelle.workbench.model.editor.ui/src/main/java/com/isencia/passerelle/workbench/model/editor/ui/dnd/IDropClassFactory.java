package com.isencia.passerelle.workbench.model.editor.ui.dnd;

import org.eclipse.core.resources.IResource;

import ptolemy.kernel.util.NamedObj;

public interface IDropClassFactory {

	public Class<? extends NamedObj> getClassForPath(final IResource selected, final String filePath);
}
