/*
 * Created on 10 juin 2005 with Eclipse
 */
package fr.soleil.bossanova.gui.action;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.resources.Icons;

public class MoveUpStepAction extends AbstractMultipleSelectionAction {

	public MoveUpStepAction(BossaNovaSequencerImpl sequencer, JTable table) {
		super(sequencer, table);
		// This is an instance initializer; it is executed just after the
		// constructor of the superclass is invoked
		// The following values are completely optional
		putValue(Action.NAME, "Move up");
		// Set tool tip text
		putValue(Action.SHORT_DESCRIPTION, "Move up Step");
		// This text is not directly used by any Swing component;
		// however, this text could be used in a help system
		putValue(Action.LONG_DESCRIPTION, "Move up Step");
		// Set an icon
		putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.list.move.up"));
		// Set a mnemonic character. In most look and feels, this causes the
		// specified character to be underlined This indicates that if the component
		// using this action has the focus and In some look and feels, this causes
		// the specified character in the label to be underlined and
		putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_U);
		// Set an accelerator key; this value is used by menu items
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK
				+ InputEvent.CTRL_DOWN_MASK));
	}
    public void doSpecificUnitaryActionOnRow(int row) {
		if (row >= 0) {
			sequencer.moveStepUp(row);
		}
		getTable().getSelectionModel().setSelectionInterval(row - 1 , row - 1);
    }
}