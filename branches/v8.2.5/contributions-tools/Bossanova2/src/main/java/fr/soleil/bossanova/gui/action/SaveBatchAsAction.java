package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;

import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BatchManager;
import fr.soleil.bossanova.gui.BossanovaUserPref;
import fr.soleil.bossanova.gui.MainScreen;

@SuppressWarnings("serial")
public class SaveBatchAsAction extends AbstractAction {

	public SaveBatchAsAction() {
		putValue(Action.SHORT_DESCRIPTION, "Save the current batch");
	}

    @Override
	public void actionPerformed(ActionEvent e) {
		MainScreen mainScreen = BossaNovaData.getSingleton()
				.getApplication().getMainScreen();
		try {
                        String defaultPath = BossanovaUserPref.getPref(BossanovaUserPref.BATCH_DIRECTORY, System.getProperty("user.home"));
			JFileChooser fileChooser = new JFileChooser(defaultPath);
			fileChooser.showSaveDialog(mainScreen);
			File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile != null) {
				BatchManager.saveBatchAs(fileChooser.getSelectedFile());
			    BossanovaUserPref.putPref(BossanovaUserPref.BATCH_DIRECTORY, fileChooser.getSelectedFile().getParent());
			    // Bug 18267 Displaying current batch name
			    mainScreen.displayCurrentBatchName();
			}

		} catch (Exception e1) {
			e1.printStackTrace();
			PopupUtil.showError(mainScreen, HMIMessages.ERROR_GENERIC,
					"Didn't work out" + e1.getMessage());
		}
	}
}
