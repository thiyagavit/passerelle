package fr.soleil.bossanova.gui.action;

import javax.swing.AbstractAction;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;


public abstract class AbstractBossanovaAction extends AbstractAction {

	protected BossaNovaSequencerImpl sequencer;

	public AbstractBossanovaAction(BossaNovaSequencerImpl sequencer){
		this.sequencer = sequencer;
	}
}
