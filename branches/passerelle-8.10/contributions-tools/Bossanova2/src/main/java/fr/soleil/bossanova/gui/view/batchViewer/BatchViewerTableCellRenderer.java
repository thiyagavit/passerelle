package fr.soleil.bossanova.gui.view.batchViewer;

import java.awt.Color;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.GUIConstants;
import fr.soleil.bossanova.gui.components.BossanovaTableCellRenderer;
import fr.soleil.bossanova.model.Batch;
import fr.soleil.bossanova.model.Step;

public class BatchViewerTableCellRenderer extends BossanovaTableCellRenderer {


	private BossaNovaSequencerImpl sequencer;

	//---------------------------------------------------------------------------------------------
	// CONSTRUCTOR
	//---------------------------------------------------------------------------------------------
	public BatchViewerTableCellRenderer(BossaNovaSequencerImpl sequencer) {
		super();
		this.sequencer = sequencer;
	}
	@Override
	protected Color getDefaultBackgroundColor(int row, boolean isSelected) {
		Color result = super.getDefaultBackgroundColor(row, isSelected);
		if (sequencer.getCurrentRunningStepIndex() == row) {
			result = GUIConstants.RUNNING_COLOR;
		}
		Batch batch = sequencer.getBatch();
		if (batch != null){
			Step step = batch.getStep(row);
			if (step != null){
				if (!step.isEnable()){
					result = GUIConstants.DISABLE_COLOR;
				}
				if (step.isOnFault()){
					result = GUIConstants.FAULT_COLOR;
				}
			}
		}
		return result;
	}

}
