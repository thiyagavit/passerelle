//Implemented as part of bug 17628 fix
// JC Pret Jan 2011

package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BatchManager;
import fr.soleil.bossanova.gui.BossanovaUserPref;
import fr.soleil.bossanova.gui.MainScreen;
import fr.soleil.bossanova.resources.Icons;

public class QuitAction extends AbstractAction{
    
    public BossaNovaSequencerImpl sequencer;

    public QuitAction() {
        super(HMIMessages.getString(HMIMessages.MENU_SAVE), Icons.getIcon("bossanova.quit"));
        putValue(Action.SHORT_DESCRIPTION, "Quit BossaNova application");
    }

    public void actionPerformed(ActionEvent e) {
        MainScreen mainScreen = BossaNovaData.getSingleton().getApplication().getMainScreen();
        try
        {
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
        /***
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
         ***/
       } catch (Exception e1) {
            e1.printStackTrace();
            PopupUtil.showError(mainScreen, HMIMessages.ERROR_GENERIC,
                    "Didn't work out" + e1.getMessage());
        }
        // End JCP
    }
    

}