package com.isencia.passerelle.workbench.model.editor.ui.editor;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.AlignmentRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightRetargetAction;
import org.eclipse.gef.ui.actions.MatchWidthRetargetAction;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;

public class PasserelleActionBarContributor extends ActionBarContributor {

	/**
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#buildActions()
	 */
	protected void buildActions() {
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());

		addRetargetAction(new AlignmentRetargetAction(PositionConstants.LEFT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.CENTER));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.RIGHT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.TOP));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.MIDDLE));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.BOTTOM));

		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());

		addRetargetAction(new MatchWidthRetargetAction());
		addRetargetAction(new MatchHeightRetargetAction());

		addRetargetAction(new RetargetAction(
				GEFActionConstants.TOGGLE_RULER_VISIBILITY,
				GEFMessages.ToggleRulerVisibility_Label, IAction.AS_CHECK_BOX));

		addRetargetAction(new RetargetAction(
				GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY,
				GEFMessages.ToggleSnapToGeometry_Label, IAction.AS_CHECK_BOX));

		addRetargetAction(new RetargetAction(
				GEFActionConstants.TOGGLE_GRID_VISIBILITY,
				GEFMessages.ToggleGrid_Label, IAction.AS_CHECK_BOX));
		IWorkbenchWindow iww = getPage().getWorkbenchWindow();
		addRetargetAction(new RetargetAction(ActionFactory.CLOSE
				.getId(), "Close editor"));
		addRetargetAction(new RetargetAction(ActionFactory.EXPORT
				.getId(), "Create submodel"));
		addRetargetAction((RetargetAction) ActionFactory.COPY.create(iww));
		addRetargetAction((RetargetAction) ActionFactory.CUT.create(iww));
		addRetargetAction((RetargetAction) ActionFactory.PASTE.create(iww));
	}

	/**
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#declareGlobalActionKeys()
	 */
	protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.PRINT.getId());
		addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(IToolBarManager)
	 */
	public void contributeToToolBar(IToolBarManager toolbarManager) {
		toolbarManager.add(new Separator());
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_LEFT));
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_CENTER));
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_RIGHT));
		toolbarManager.add(new Separator());
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_TOP));
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_BOTTOM));

		toolbarManager.add(new Separator());
		toolbarManager.add(getAction(GEFActionConstants.MATCH_WIDTH));
		toolbarManager.add(getAction(GEFActionConstants.MATCH_HEIGHT));

		toolbarManager.add(new Separator());
		toolbarManager.add(getAction(GEFActionConstants.ZOOM_IN));
		toolbarManager.add(getAction(GEFActionConstants.ZOOM_OUT));
		String[] zoomStrings = new String[] { ZoomManager.FIT_ALL,
				ZoomManager.FIT_HEIGHT, ZoomManager.FIT_WIDTH };
		toolbarManager
				.add(new ZoomComboContributionItem(getPage(), zoomStrings));

	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menubar) {
		super.contributeToMenu(menubar);
		MenuManager viewMenu = new MenuManager("Diagram");
		viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
		viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
		viewMenu.add(new Separator());
		viewMenu.add(getAction(GEFActionConstants.TOGGLE_RULER_VISIBILITY));
		viewMenu.add(getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
		viewMenu.add(getAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY));
		viewMenu.add(new Separator());
		viewMenu.add(getAction(GEFActionConstants.MATCH_WIDTH));
		viewMenu.add(getAction(GEFActionConstants.MATCH_HEIGHT));
		menubar.insertAfter(IWorkbenchActionConstants.M_EDIT, viewMenu);
		if (menubar.find(IWorkbenchActionConstants.GROUP_EDITOR) == null) {
			menubar
					.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
		}

	}

}
