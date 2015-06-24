/*
 * Created on 10 juin 2005
 * with Eclipse
 */
package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.resources.Icons;




public class SelectDeselectAllStepAction extends AbstractBossanovaAction {

	private JTable table;
	private boolean select = false;
	
    /**
     *
     */
    public SelectDeselectAllStepAction(BossaNovaSequencerImpl sequencer, JTable table) {
        super(sequencer);
        this.table = table;


        // This is an instance initializer; it is executed just after the
        // constructor of the superclass is invoked

            // The following values are completely optional
        	putValue(Action.NAME, "Select/Deselect All Step");
            // Set tool tip text
            putValue(Action.SHORT_DESCRIPTION, "Select or Deselect all step to the current batch");

            // This text is not directly used by any Swing component;
            // however, this text could be used in a help system
            putValue(Action.LONG_DESCRIPTION, "Select or Deselect all step  to the current batch");

            // Set an icon
            //Icon icon = new ImageIcon("icon.gif");
            putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.list.selectall"));

            // Set a mnemonic character. In most look and feels, this causes the
            // specified character to be underlined This indicates that if the component
            // using this action has the focus and In some look and feels, this causes
            // the specified character in the label to be underlined and
            putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_L);

            // Set an accelerator key; this value is used by menu items
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl shift L"));
    }

    public void actionPerformed(ActionEvent e) {
    	int rowSelected = table.getSelectedRow(); 
    	sequencer.modifyEnableForAllStep(select);
    	select = !select;
    	if(rowSelected >= 0)
    		table.getSelectionModel().setSelectionInterval(rowSelected, rowSelected);
    }
}