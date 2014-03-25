package fr.soleil.bossanova.gui.view.batchViewer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Action;

import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.gui.ScreenManager;

public class RowKeySelectionAdapter extends KeyAdapter {

	public RowKeySelectionAdapter(/* Bossanova2 application*/) {
		super();
		//this.application = application;
	}

	public void keyPressed(KeyEvent e){
		// COPY OR PASTE ACTIONS
		if (e.isControlDown()) {
			// COPY
			if (e.getKeyCode() == KeyEvent.VK_C) {
				Action copyAction = ScreenManager.getCopyStepsAction();
				if (copyAction != null){
					copyAction.actionPerformed(null);
				}
			}
			//PASTE
			if (e.getKeyCode() == KeyEvent.VK_V) {
				Action pasteAction = ScreenManager.getPasteStepsAction();
				if (pasteAction != null){
					pasteAction.actionPerformed(null);
				}
			}
			// Bug 17627
			// CUT
            if (e.getKeyCode() == KeyEvent.VK_X) {
                Action cutAction = ScreenManager.getCutStepsAction();
                if (cutAction != null){
                    cutAction.actionPerformed(null);
                }
            }
		}
	}
	public void keyReleased(KeyEvent e) {
	    BossaNovaData.getSingleton().getApplication().getMainScreen().changeStep();
	}
}
