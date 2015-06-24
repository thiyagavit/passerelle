package fr.soleil.bossanova.gui.action;

import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.view.batchViewer.BatchViewerControler;
import fr.soleil.bossanova.model.Step;
import fr.soleil.bossanova.resources.Icons;

public class CopyStepsAction extends AbstractMultipleSelectionAction {



	public CopyStepsAction(BossaNovaSequencerImpl sequencer, JTable table) {
		super(sequencer, table);
		// This is an instance initializer; it is executed just after the
		// constructor of the superclass is invoked

		// The following values are completely optional
		putValue(Action.NAME, "Copy");
		// Set tool tip text
		putValue(Action.SHORT_DESCRIPTION, "Copy the current step");

		// This text is not directly used by any Swing component;
		// however, this text could be used in a help system
		putValue(Action.LONG_DESCRIPTION, "Copy the current step");

		// Set an icon
		// Icon icon = new ImageIcon("icon.gif");
		putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.list.copy"));

		// Set a mnemonic character. In most look and feels, this causes the
		// specified character to be underlined This indicates that if the
		// component
		// using this action has the focus and In some look and feels, this
		// causes
		// the specified character in the label to be underlined and
		putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_C);

		// Set an accelerator key; this value is used by menu items
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));

	}
	@Override
	protected void doSpecificPreTreatment(){
		BatchViewerControler.cleanStepsToCopy();
	}
	public void doSpecificUnitaryActionOnRow(int row) {
		Step step = sequencer.getBatch().getStep(row);
		if (step != null) {
			BatchViewerControler.addStepToCopy(step);
		}
	}
}
