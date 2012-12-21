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
import fr.soleil.bossanova.resources.Icons;

public class SaveBatchAction extends AbstractAction {

    public SaveBatchAction() {
        super(HMIMessages.getString(HMIMessages.MENU_SAVE), Icons.getIcon("bossanova.save"));
        putValue(Action.SHORT_DESCRIPTION, "Save the current batch");
    }

    public void actionPerformed(ActionEvent e) {
        MainScreen mainScreen = BossaNovaData.getSingleton().getApplication().getMainScreen();
        try {
            if (BatchManager.getCurrentBatchFile() != null) {
                BatchManager.saveBatch();
            } else {
                String defaultPath = BossanovaUserPref.getPref(BossanovaUserPref.BATCH_DIRECTORY, System.getProperty("user.home"));
                JFileChooser fileChooser = new JFileChooser(defaultPath);
                fileChooser.showSaveDialog(mainScreen);
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    BatchManager.saveBatchAs(fileChooser.getSelectedFile());
                    BossanovaUserPref.putPref(BossanovaUserPref.BATCH_DIRECTORY, fileChooser.getSelectedFile().getParent());
                }
            }
            
        } catch (Exception e1) {
            e1.printStackTrace();
            PopupUtil.showError(mainScreen, HMIMessages.ERROR_GENERIC,
                    "Didn't work out" + e1.getMessage());
        }
    }
}
