package fr.soleil.bossanova.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import net.infonode.docking.RootWindow;


public class BossanovaWindowListener extends WindowAdapter {

	private RootWindow window;
	
	public BossanovaWindowListener(RootWindow window){
		this.window = window;
	}
	
	public void windowClosing(WindowEvent e) {
		ScreenManager.saveWindowLayoutPreferences(window);
		// Bug 17628
		ScreenManager.saveUnsavedBatch(window);
	}
}
