package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.view.batchViewer.BatchViewerControler;
import fr.soleil.bossanova.model.Step;
import fr.soleil.bossanova.resources.Icons;

public class PasteStepsAction extends AbstractBossanovaAction {

	private JTable table;

	public PasteStepsAction(BossaNovaSequencerImpl sequencer, JTable table) {
		super(sequencer);
		this.table = table;
		// This is an instance initializer; it is executed just after the
		// constructor of the superclass is invoked

		// The following values are completely optional
		putValue(Action.NAME, "Paste");
		// Set tool tip text
		putValue(Action.SHORT_DESCRIPTION, "Paste the copied step");

		// This text is not directly used by any Swing component;
		// however, this text could be used in a help system
		putValue(Action.LONG_DESCRIPTION, "Paste the copied step");

		// Set an icon
		// Icon icon = new ImageIcon("icon.gif");
		putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.list.paste"));

		// Set a mnemonic character. In most look and feels, this causes the
		// specified character to be underlined This indicates that if the
		// component
		// using this action has the focus and In some look and feels, this
		// causes
		// the specified character in the label to be underlined and
		putValue(Action.MNEMONIC_KEY,java.awt.event.KeyEvent.VK_V);

		// Set an accelerator key; this value is used by menu items
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));
	}

	public void actionPerformed(ActionEvent e) {
		int rowToInsert = (table.getSelectedRow() >= 0) ? table.getSelectedRow() : table.getRowCount();
		List<Step> stepsToInsert = BatchViewerControler.getStepsToCopy();
		for (Iterator<Step> iterator = stepsToInsert.iterator(); iterator.hasNext();) {
			Step stepToInsert = iterator.next();
			sequencer.insertStepAt(stepToInsert, rowToInsert);
		}
	}
}
