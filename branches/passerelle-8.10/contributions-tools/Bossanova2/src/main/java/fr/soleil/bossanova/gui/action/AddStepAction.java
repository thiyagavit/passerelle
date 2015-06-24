/*
 * Created on 10 juin 2005
 * with Eclipse
 */
package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import com.isencia.passerelle.hmi.state.StateMachine;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.MainScreen;
import fr.soleil.bossanova.resources.Icons;

@SuppressWarnings("serial")
public class AddStepAction extends AbstractBossanovaAction {

	/**
     *
     */
	public AddStepAction(BossaNovaSequencerImpl sequencer) {
		super(sequencer);

		// This is an instance initializer; it is executed just after the
		// constructor of the superclass is invoked

		// The following values are completely optional
		putValue(Action.NAME, "Add");
		// Set tool tip text
		putValue(Action.SHORT_DESCRIPTION, "Add step to the current batch");

		// This text is not directly used by any Swing component;
		// however, this text could be used in a help system
		putValue(Action.LONG_DESCRIPTION, "Add step to the current batch");

		// Set an icon
		// Icon icon = new ImageIcon("icon.gif");
		putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.list.add"));

		// Set a mnemonic character. In most look and feels, this causes the
		// specified character to be underlined This indicates that if the
		// component
		// using this action has the focus and In some look and feels, this
		// causes
		// the specified character in the label to be underlined and
		putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_A);

		// Set an accelerator key; this value is used by menu items
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl shift A"));

	}

	public void actionPerformed(ActionEvent e) {
		sequencer.addEmptyStep("");
		StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);

		// Select the new step.
		BossaNovaData.getSingleton().getApplication().setSelectedStep(0,sequencer.getBatch().getStep(0));

		MainScreen mainScreen = BossaNovaData.getSingleton().getApplication().getMainScreen();
		if (mainScreen != null) {
			mainScreen.enableStepButton();
		}
	}
}