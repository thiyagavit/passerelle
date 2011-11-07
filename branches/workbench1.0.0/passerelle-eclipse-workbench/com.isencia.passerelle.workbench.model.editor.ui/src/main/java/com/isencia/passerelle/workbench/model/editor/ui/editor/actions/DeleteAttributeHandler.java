package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public class DeleteAttributeHandler extends AbstractAttributeHandler {

	private static Logger logger = LoggerFactory
			.getLogger(AbstractHandler.class);

	protected void doAction() {

		final PasserelleModelMultiPageEditor ed = (PasserelleModelMultiPageEditor) EclipseUtils
				.getActivePage().getActiveEditor();
		if (ed == null)
			return;

		final ActorAttributesView attView = (ActorAttributesView) EclipseUtils
				.getActivePage().getActivePart();
		try {
			attView.deleteSelectedParameter();
		} catch (IllegalActionException e) {
			logger.error("Cannot delete parameter ", e);
		}
	}

}
