package fr.soleil.bossanova.gui.view.batchViewer;

import javax.swing.JComboBox;

import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;

public class BatchViewerComboBoxCellEditor extends ComboBoxCellEditor {

	/**
	 * Constructor
	 *
	 * @param directoryPath
	 *            the path of the current directory
	 * @throws BossanovaException
	 */
	public BatchViewerComboBoxCellEditor(final JComboBox comboBox) {
			super(comboBox);
	}
}
