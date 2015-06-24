package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.JTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;

public abstract class AbstractMultipleSelectionAction extends AbstractBossanovaAction {
  private static final long serialVersionUID = 1L;

  private final static Logger LOGGER = LoggerFactory.getLogger(AbstractMultipleSelectionAction.class);
  
  private JTable table;

  public AbstractMultipleSelectionAction(BossaNovaSequencerImpl sequencer, JTable table) {
    super(sequencer);
    this.table = table;
  }

  public void actionPerformed(ActionEvent event) {
    doSpecificPreTreatment();
    int[] rowsToTreat = table.getSelectedRows();
    for (int i = rowsToTreat.length - 1; i >= 0; i--) {
      int row = rowsToTreat[i];
      try {
        doSpecificUnitaryActionOnRow(row);
      } catch (ArrayIndexOutOfBoundsException e) {
        // Erwin : this happens when adding one or more steps and immediately cutting them again
        // for some reason that I was not able to discover, the rowsToTreat then contains one index too many...
        // Once the user has selected one particular step, or a number of steps using buttons or mouse,
        // the rowsToTreat is correctly returned...
        LOGGER.warn("Skipping ArrayIndexOutOfBoundsException for invalid selection "+row);
      }
    }
    doSpecificPostTreatment();
  }

  public void setTable(JTable table) {
    this.table = table;
  }

  public JTable getTable() {
    return this.table;
  }

  protected void doSpecificPreTreatment() {
  }

  protected abstract void doSpecificUnitaryActionOnRow(int row);

  protected void doSpecificPostTreatment() {
    BossaNovaData.getSingleton().getApplication().getMainScreen().changeStep();
  }
}
