package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.state.StateMachine;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BatchManager;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.BossanovaUserPref;
import fr.soleil.bossanova.gui.MainScreen;
import fr.soleil.bossanova.resources.Icons;

@SuppressWarnings("serial")
public class LoadBatchAction extends AbstractAction {

	public LoadBatchAction() {
		super();
		putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.open"));
		putValue(Action.SHORT_DESCRIPTION, "Open batch");
	}

    @Override
	public void actionPerformed(ActionEvent e) {
		MainScreen mainScreen = BossaNovaData.getSingleton()
				.getApplication().getMainScreen();
		try {
		    
		    BatchManager.getSequencer();
            // Bug 17628
		    // Offer to save unsaved edits before loading batch
            if( BossaNovaSequencerImpl.getCurrentBatchSaved() ==  false )
            {
                int iSaveBatch =  JOptionPane.showConfirmDialog(mainScreen, "Voulez-vous sauvegarder le batch en cours?", "Modifications non sauvegardees", JOptionPane.YES_NO_OPTION);
                if( iSaveBatch == JOptionPane.YES_OPTION  )
                {
                    String defaultPath = BossanovaUserPref.getPref(BossanovaUserPref.BATCH_DIRECTORY, System.getProperty("user.home"));
                    JFileChooser fileChooser = new JFileChooser(defaultPath);
                    fileChooser.showSaveDialog(mainScreen);
                    File selectedFile = fileChooser.getSelectedFile();
                    if (selectedFile != null) {
                        BatchManager.saveBatchAs(fileChooser.getSelectedFile());
                        BossanovaUserPref.putPref(BossanovaUserPref.BATCH_DIRECTORY, fileChooser.getSelectedFile().getParent());
                    }
                }
            }
            // End Bug 17628

			String defaultPath = BossanovaUserPref.getPref(BossanovaUserPref.BATCH_DIRECTORY, System.getProperty("user.home"));
			JFileChooser fileChooser = new JFileChooser(defaultPath);
			fileChooser.showOpenDialog(mainScreen);
			File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile != null) {
				BatchManager.loadBatch(selectedFile);
			    BossanovaUserPref.putPref(BossanovaUserPref.BATCH_DIRECTORY, fileChooser.getSelectedFile().getParent());
                // Bug 18267 Displaying current batch name
			    mainScreen.displayCurrentBatchName();
			}
			StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
		} catch (Exception e1) {
			e1.printStackTrace();
			PopupUtil.showError(mainScreen, HMIMessages.ERROR_GENERIC,
					"Didn't work out" + e1.getMessage());
		}
	}
}
