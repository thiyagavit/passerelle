package com.isencia.passerelle.workbench.model.editor.ui.dnd;

import org.eclipse.core.resources.IResource;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.actor.io.FileReader;

/**
 * This factory should be overridden with user defined actors for different file types.
 * @author gerring
 *
 */
public class DefaultDropClassFactory implements IDropClassFactory {

	@Override
	public Class<? extends NamedObj> getClassForPath(IResource source, String filePath) {
		return FileReader.class;
	}

}
