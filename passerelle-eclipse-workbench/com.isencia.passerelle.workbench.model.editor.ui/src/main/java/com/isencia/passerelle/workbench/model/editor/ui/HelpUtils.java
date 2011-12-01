package com.isencia.passerelle.workbench.model.editor.ui;

import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemFactory;

public class HelpUtils {
	public static final String HELP_BUNDLE_ID = "com.isencia.passerelle.actor.conf";
// TODO enable again when we decide to use context specific help
//	public static String getContextId(Object element) {
//		if (element instanceof String) {
//			return HELP_BUNDLE_ID + ".name";
//		} else if (element instanceof Variable) {
//			return getContextIdOfVariable((Variable) element);
//		}
//		return null;
//	}

	public static String getContextId(Object element) {
		if (element instanceof String) {
			return HELP_BUNDLE_ID + ".name";
			
		} else if (element instanceof ActorEditPart) {
			
			final NamedObj parent = ((ActorEditPart) element).getEntity();
			final String actorName = parent.getClass().getName();
			String path = "/"
					+ PaletteItemFactory.getInstance().getBuildId(actorName)
					+ "/html/" + actorName + ".html";
			return path;
			
		} else if (element instanceof Variable) {
			
			final NamedObj parent  = ((Variable) element).getContainer();
			final String actorName = parent.getClass().getName();
			String path = "/"
				+ PaletteItemFactory.getInstance().getBuildId(actorName)
				+ "/html/" + actorName + "_attributes.html";
		    return path;

		}
		return null;
	}

	public static String getContextIdOfVariable(Variable param) {

		Attribute attr = (Attribute) param;
		if (param.getContainer() != null) {
			String helpBundle = HELP_BUNDLE_ID;
			String actorName = param.getContainer().getClass().getName()
					.replace(".", "_");
			return helpBundle + "." + actorName + "_" + attr.getName();
		}
		return "";
	}
}
