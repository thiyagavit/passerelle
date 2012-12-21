package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.infonode.docking.View;

public class ShowViewAction extends AbstractAction {

	private View view;

	public ShowViewAction(View view) {
		super("Show " + view.getName(), view.getIcon());
		this.view = view;
	}

	public void actionPerformed(ActionEvent e) {
		if (view != null) {
			view.setVisible(true);
			view.getComponent().setVisible(true);
			view.restore();
		}
	}

}
