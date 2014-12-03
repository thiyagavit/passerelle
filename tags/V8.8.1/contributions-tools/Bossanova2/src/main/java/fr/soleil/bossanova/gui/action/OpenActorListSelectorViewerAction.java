package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import fr.soleil.bossanova.gui.view.actorListSelectorViewer.ActorListSelectorViewer;
import fr.soleil.bossanova.resources.Icons;

public class OpenActorListSelectorViewerAction extends AbstractAction {
	public OpenActorListSelectorViewerAction() {
		super("Actor Selector", Icons.getIcon("bossanova.openActorListSelector"));
	}

	public void actionPerformed(ActionEvent e) {
		ActorListSelectorViewer viewer = new ActorListSelectorViewer();
		viewer.setVisible(true);
	}
}
