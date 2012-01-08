package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.util.SwingUtils;
import com.isencia.passerelle.model.Flow;

public class DeleteFileAction extends AbstractAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 2603323877590335139L;
	private JFileChooser chooser = null;
	/**
	 * map of all open sequence
	 */
	private final Map<URL, Flow> map;

	public DeleteFileAction(final String desc, final JFileChooser aChooser, final Map<URL, Flow> aMap) {
		super(desc);
		chooser = aChooser;
		map = aMap;

	}

	public void actionPerformed(final ActionEvent e) {
		final File file = chooser.getSelectedFile();
		if (file != null) {
			final int res = JOptionPane.showConfirmDialog(chooser, HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_TITLE) + " " + chooser.getSelectedFile().getAbsolutePath() + "?", HMIMessages
					.getString(HMIMessages.FILECHOOSER_DELETE_MESSAGE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (res == JOptionPane.OK_OPTION) {
				try {
					if (map.containsKey(file.toURI().toURL())) {
						JOptionPane.showMessageDialog(chooser, HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_IMPOSSIBLE), HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_TITLE),
								JOptionPane.ERROR_MESSAGE);
					} else {
						file.delete();
						chooser.rescanCurrentDirectory(); // update list of
						// files.

						// unselect deleted file.
						chooser.setSelectedFile(null);

						// get all JTextField in chooser
						final List<JTextField> textFieldList = SwingUtils.getDescendantsOfType(JTextField.class, chooser, true);

						// get JTextField which contains the name of file
						// previously deleted and set text to "".
						boolean found = false;
						final Iterator<JTextField> itt = textFieldList.iterator();
						while (itt.hasNext() && !found) {
							final JTextField temp = itt.next();
							if (temp.getText().equals(file.getName())) {
								temp.setText("");
								found = true;
							}
						}
					}
				} catch (final MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}
	}
}
