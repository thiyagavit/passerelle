package fr.soleil.bossanova.gui.view.batchViewer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;

public class BatchViewerIntegerTableCellRenderer extends BatchViewerTableCellRenderer {

	//-----------------------------------------------------------------------------------
	// CONSTRUCTOR
	//-----------------------------------------------------------------------------------
	public BatchViewerIntegerTableCellRenderer(BossaNovaSequencerImpl sequencer) {
		super(sequencer);
	}
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (comp instanceof JLabel){
			JLabel label = (JLabel) comp;
			label.setHorizontalAlignment(JLabel.RIGHT);
		}
		return comp;
	}
}
