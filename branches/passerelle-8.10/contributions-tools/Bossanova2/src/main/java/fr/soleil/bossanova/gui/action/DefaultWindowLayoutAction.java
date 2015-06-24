/*
 * Created on 10 juin 2005
 * with Eclipse
 */
package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import fr.soleil.bossanova.gui.MainScreen;
import fr.soleil.bossanova.gui.ScreenManager;
import fr.soleil.bossanova.resources.Icons;

public class DefaultWindowLayoutAction extends AbstractAction {

	private MainScreen mainScreen;
	
	public DefaultWindowLayoutAction(MainScreen mainScreen) {
		super("Default Layout", Icons.getIcon("bossanova.layout.default"));
		this.mainScreen = mainScreen;
		// This is an instance initializer; it is executed just after the
		// constructor of the superclass is invoked

		// Set tool tip text
		putValue(Action.SHORT_DESCRIPTION, "Restore default window");

		// This text is not directly used by any Swing component;
		// however, this text could be used in a help system
		putValue(Action.LONG_DESCRIPTION, "Restore default window");

		// Set an icon
		// Icon icon = new ImageIcon("icon.gif");
		// putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.list.add"));

		// Set a mnemonic character. In most look and feels, this causes the
		// specified character to be underlined This indicates that if the
		// component
		// using this action has the focus and In some look and feels, this
		// causes
		// the specified character in the label to be underlined and
		putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_R);

		// Set an accelerator key; this value is used by menu items
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl shift R"));

	}

	public void actionPerformed(ActionEvent e) {
		ScreenManager.loadDefaultWindowLayoutPreferences(mainScreen.getRootWindow(),  mainScreen.getDefaultLayout());
	}
}