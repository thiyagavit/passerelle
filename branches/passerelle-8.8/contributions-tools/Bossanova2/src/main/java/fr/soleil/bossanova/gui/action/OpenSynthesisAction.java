package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import fr.soleil.bossanova.controller.GenerateBatchSynthesis;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.view.synthesisViewer.SynthesisViewer;
import fr.soleil.bossanova.model.Batch;
import fr.soleil.bossanova.resources.Icons;

public class OpenSynthesisAction extends AbstractBossanovaAction {
	private SynthesisViewer synthesis = null;

	public OpenSynthesisAction(BossaNovaSequencerImpl sequencer) {
		super(sequencer);
		// This is an instance initializer; it is executed just after the
		// constructor of the superclass is invoked

		// The following values are completely optional
		putValue(Action.NAME, "Display synthesis report");
		// Set tool tip text
		putValue(Action.SHORT_DESCRIPTION, "Display the synthesis report");

		// This text is not directly used by any Swing component;
		// however, this text could be used in a help system
		putValue(Action.LONG_DESCRIPTION, "Display the synthesis report of the current batch");

		// Set an icon
		putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.synthesis.view"));

		// Set a mnemonic character. In most look and feels, this causes the
		// specified character to be underlined This indicates that if the
		// component
		// using this action has the focus and In some look and feels, this
		// causes
		// the specified character in the label to be underlined and
		// putValue(Action.MNEMONIC_KEY, new
		// Integer(java.awt.event.KeyEvent.VK_N));

		// Set an accelerator key; this value is used by menu items
		// putValue(Action.ACCELERATOR_KEY,
		// KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.SHIFT_DOWN_MASK+InputEvent.CTRL_DOWN_MASK));
		synthesis = new SynthesisViewer();
		synthesis.setSize(600, 600);

	}

	public void actionPerformed(ActionEvent e) {
		Batch batch = sequencer.getBatch();
		String url = null;
		try {
			url = GenerateBatchSynthesis.getSynthesis(batch);
		} catch (Exception ex) {
			ex.printStackTrace();
			url = ex.getMessage();
		}
		synthesis.setSynthesisText(url);
		synthesis.setVisible(true);
	}
}
