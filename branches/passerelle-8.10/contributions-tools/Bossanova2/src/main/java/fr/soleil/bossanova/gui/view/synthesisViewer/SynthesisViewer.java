package fr.soleil.bossanova.gui.view.synthesisViewer;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

public class SynthesisViewer extends JDialog {

	private JEditorPane pane;

	public SynthesisViewer() {
		init();
	}

	public void init() {

		pane = new JEditorPane();
		pane.setEditable(false); // Read-only
		getContentPane().add(new JScrollPane(pane), "Center");
	}

	public void setSynthesisText(String text) {

		try {
			pane.setText(text);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
