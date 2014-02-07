package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BatchManager;
import fr.soleil.bossanova.gui.BossanovaUserPref;
import fr.soleil.bossanova.gui.MainScreen;

@SuppressWarnings("serial")
public class SaveBatchAsAction extends AbstractAction {
  private final static Logger LOGGER = LoggerFactory.getLogger(SaveBatchAsAction.class);

  public SaveBatchAsAction() {
    putValue(Action.SHORT_DESCRIPTION, "Save the current batch");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    MainScreen mainScreen = BossaNovaData.getSingleton().getApplication().getMainScreen();
    try {
      String defaultPath = BossanovaUserPref.getPref(BossanovaUserPref.BATCH_DIRECTORY, System.getProperty("user.home"));
      JFileChooser fileChooser = new JFileChooser(defaultPath);
      fileChooser.showSaveDialog(mainScreen);
      File selectedFile = fileChooser.getSelectedFile();
      if (selectedFile != null) {
        if (selectedFile.exists()) {
          throw new IOException("File already exists " + selectedFile.getName());
        } else {
          BatchManager.saveBatchAs(fileChooser.getSelectedFile());
          BossanovaUserPref.putPref(BossanovaUserPref.BATCH_DIRECTORY, fileChooser.getSelectedFile().getParent());
          // Bug 18267 Displaying current batch name
          mainScreen.displayCurrentBatchName();
        }
      }
    } catch (Exception e1) {
      LOGGER.error("Error saving file", e1);
      PopupUtil.showError(mainScreen, HMIMessages.HMI_ERROR_FILE_SAVE, e1.getMessage());
    }
  }
}
