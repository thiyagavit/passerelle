package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BatchManager;


public class NewBatchAction extends AbstractAction {

	public void actionPerformed(ActionEvent e) {
		BatchManager.createNewBatch();
		BossaNovaData.getSingleton().getApplication().getMainScreen().disableStepButton();
	}

}
