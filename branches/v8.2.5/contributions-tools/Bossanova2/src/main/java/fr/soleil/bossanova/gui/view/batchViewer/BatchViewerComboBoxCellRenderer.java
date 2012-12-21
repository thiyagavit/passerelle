package fr.soleil.bossanova.gui.view.batchViewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;

public class BatchViewerComboBoxCellRenderer extends BatchViewerTableCellRenderer {

	//-----------------------------------------------------------------------------------
	// CONSTRUCTOR
	//-----------------------------------------------------------------------------------
	public BatchViewerComboBoxCellRenderer(BossaNovaSequencerImpl sequencer) {
		super(sequencer);
	}
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (comp instanceof JLabel){
			JLabel label = (JLabel) comp ;
			String labelValue = label.getText();
			boolean bold = false;
			if(labelValue == null || labelValue.trim().equals(""))
			{
				labelValue = "<Please select a Step>";
				bold = true;
			}
			
			JComboBox combo = new JComboBox(new Object[]{labelValue});
			Color backColor = getDefaultBackgroundColor(row, isSelected);
			
			combo.setBackground(backColor);
			if(bold)
			{
				Font font = combo.getFont().deriveFont(Font.BOLD + Font.ITALIC);
				combo.setFont(font);
				combo.setForeground(Color.BLUE);
			}
			return combo;
		}
		
		return comp;
	}
}
	
