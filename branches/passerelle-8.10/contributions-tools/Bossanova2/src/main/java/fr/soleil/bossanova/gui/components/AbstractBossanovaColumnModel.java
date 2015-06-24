package fr.soleil.bossanova.gui.components;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


public class AbstractBossanovaColumnModel  extends DefaultTableColumnModel {
	protected void addColumn(String colName, int colIndex, int colSize, TableCellRenderer cellRenderer,
			TableCellEditor cellEditor) {
		TableColumn col = new TableColumn(colIndex, colSize, cellRenderer, cellEditor);
		col.setHeaderValue(colName);
		addColumn(col);
	}
}
