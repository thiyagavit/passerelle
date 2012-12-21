package fr.soleil.bossanova.gui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;




public class BossanovaBooleanTableCellRenderer extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if(comp instanceof JLabel)
		{
			JCheckBox checkBox = new JCheckBox();
			// if the editor is centered the renderer must be centered too...
			checkBox.setHorizontalAlignment(JCheckBox.CENTER);
			if(value instanceof Boolean)
			{
				checkBox.setSelected((Boolean)value);
			}
			
			Color backColor = getDefaultBackgroundColor(row, isSelected);
			checkBox.setBackground(backColor);
			return checkBox;
		}
		return comp;
	}
	protected Color getDefaultBackgroundColor(int row, boolean isSelected) {
		Color result = Color.WHITE;
		if (row % 2 == 0) {
			result = BossanovaTableCellRenderer.specificBlue;
		}
		if (isSelected) {
			result = result.darker();
		}
		return result;
	}
}
