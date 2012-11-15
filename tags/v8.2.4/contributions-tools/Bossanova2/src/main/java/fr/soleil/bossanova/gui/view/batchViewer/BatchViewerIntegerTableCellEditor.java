package fr.soleil.bossanova.gui.view.batchViewer;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class BatchViewerIntegerTableCellEditor extends DefaultCellEditor implements FocusListener{

	public BatchViewerIntegerTableCellEditor(JTextField textField){
		super(textField);
		textField.setBorder(null);
		textField.setHorizontalAlignment(JLabel.RIGHT);
		textField.addFocusListener(this);
	}
	public Object getCellEditorValue() {
		Integer result = null;

		String resultString = (String)super.getCellEditorValue();
		try{
			result = Integer.parseInt(resultString);
		} catch (NumberFormatException nfe){

		}
		return result;
	}
	public void focusGained(FocusEvent e) {
		JTextField textField = (JTextField) this.editorComponent;
		textField.selectAll();
	}
	public void focusLost(FocusEvent e) {
	}
}
