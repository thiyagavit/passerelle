package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.state.StateMachine;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BatchManager;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.BossanovaUserPref;
import fr.soleil.bossanova.model.Batch;
import fr.soleil.bossanova.resources.Icons;

public class ImportBatchAfterAction extends AbstractBossanovaAction {

    /**
     *
     */
    public ImportBatchAfterAction(BossaNovaSequencerImpl sequencer) {
        super(sequencer);
        // This is an instance initializer; it is executed just after the
        // constructor of the superclass is invoked

            // The following values are completely optional
        	putValue(Action.NAME, "Insert Batch");
            // Set tool tip text
            putValue(Action.SHORT_DESCRIPTION, "Import a batch after the selected one");

            // This text is not directly used by any Swing component;
            // however, this text could be used in a help system
            putValue(Action.LONG_DESCRIPTION, "Import a batch after the selected one to the current batch");

            // Set an icon
            //Icon icon = new ImageIcon("icon.gif");
            putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.list.import"));

            // Set a mnemonic character. In most look and feels, this causes the
            // specified character to be underlined This indicates that if the component
            // using this action has the focus and In some look and feels, this causes
            // the specified character in the label to be underlined and
            putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_I);

            // Set an accelerator key; this value is used by menu items
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl shift I"));
    } 
	
    @Override
	public void actionPerformed(ActionEvent e) {
		try {
			
			JTable table = BossaNovaData.getSingleton().getApplication().getMainScreen().getBatchViewerPanel().getTable();			
			String defaultPath = BossanovaUserPref.getPref(BossanovaUserPref.BATCH_DIRECTORY, System.getProperty("user.home"));
			JFileChooser fileChooser = new JFileChooser(defaultPath);
			fileChooser.showOpenDialog(BossaNovaData.getSingleton().getApplication().getMainScreen());
			File selectedFile = fileChooser.getSelectedFile();
			Batch batch = null;
			if (selectedFile != null) {
				batch = BatchManager.readBatchFile(selectedFile);
				int index = table.getSelectedRow();
				
				// if no step were selected we import the batch in the end of the current batch
				// if the current batch is empty we import the batch content in the current batch in the begin
				if(index < 0 || index == table.getRowCount())
				{
					index = table.getRowCount() - 1;
				}
				
				if (batch.getSteps()!=null && batch.getSteps().size()>0) {
					sequencer.insertStepsAt(batch.getSteps(),index+1);
			    	// We select the new step.
					if(batch.getSteps().size() > 0)
						BossaNovaData.getSingleton().getApplication().setSelectedStep(0,batch.getStep(0));
			    	
			    	
			    	table.getSelectionModel().setSelectionInterval(index+1, index+1);
			    	
			    	StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);			    	
				}
			}
			
		} catch (Exception e1) {
			e1.printStackTrace();
			PopupUtil.showError(BossaNovaData.getSingleton().getApplication().getMainScreen(), HMIMessages.ERROR_GENERIC, "Didn't work out"
					+ e1.getMessage());
		}
	}
}
