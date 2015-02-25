package fr.soleil.bossanova.gui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class BossanovaTableCellRenderer extends DefaultTableCellRenderer {

	public static final Color specificBlue = new Color(217,230,242);
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Color backColor = getDefaultBackgroundColor(row, isSelected);

		setBackground(backColor);
		if (value != null){
			setToolTipText(value.toString());
		}
		return comp;
	}
	protected Color getDefaultBackgroundColor(int row, boolean isSelected) {
		Color result = Color.WHITE;
		if (row % 2 == 0) {
			result = specificBlue;
		}
		if (isSelected) {
			result = result.darker();
		}
		return result;
	}
}
