/**
 *
 */
package fr.soleil.bossanova.gui.view.batchViewer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import fr.soleil.bossanova.bossaNovaData.BossaNovaData;

public class RowMouseSelectionAdapter extends MouseAdapter {

	//private Bossanova2 application;
	
	public RowMouseSelectionAdapter(/* Bossanova2 application*/){
		super();
		//this.application = application;
	}
	public void mouseReleased(MouseEvent e) {
	    BossaNovaData.getSingleton().getApplication().getMainScreen().changeStep();
	}
}