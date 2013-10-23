package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.JTable;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;

public abstract class AbstractMultipleSelectionAction extends AbstractBossanovaAction {

	private JTable table;
	
	public AbstractMultipleSelectionAction(BossaNovaSequencerImpl sequencer, JTable table) {
		super(sequencer);
		this.table = table;
	}

	public void actionPerformed(ActionEvent event) {
		doSpecificPreTreatment();
		int[] rowsToTreat = table.getSelectedRows();
		for (int i = rowsToTreat.length - 1; i >= 0; i--) {
			int row = rowsToTreat[i];
			doSpecificUnitaryActionOnRow(row);
		}
	}
	public void setTable(JTable table){
		this.table = table;
	}
	public JTable getTable(){
		return this.table;
	}
	protected void doSpecificPreTreatment(){
		
	}
	protected abstract void doSpecificUnitaryActionOnRow(int row);
}
