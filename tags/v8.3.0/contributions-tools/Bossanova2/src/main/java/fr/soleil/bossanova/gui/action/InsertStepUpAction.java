/*
 * Created on 10 juin 2005
 * with Eclipse
 */
package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JTable;

import com.isencia.passerelle.hmi.state.StateMachine;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.model.Step;
import fr.soleil.bossanova.resources.Icons;




public class InsertStepUpAction extends AbstractBossanovaAction {

	private JTable table;

    /**
     *
     */
    public InsertStepUpAction(BossaNovaSequencerImpl sequencer, JTable table) {
        super(sequencer);
		this.table = table;
        // This is an instance initializer; it is executed just after the
        // constructor of the superclass is invoked

            // The following values are completely optional
        	putValue(Action.NAME, "Insert Step before");
            // Set tool tip text
            putValue(Action.SHORT_DESCRIPTION, "Insert a step before the selected one");

            // This text is not directly used by any Swing component;
            // however, this text could be used in a help system
            putValue(Action.LONG_DESCRIPTION, "Insert a step before the selected one to the current batch");

            // Set an icon
            //Icon icon = new ImageIcon("icon.gif");
            putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.list.insertup"));

            // Set a mnemonic character. In most look and feels, this causes the
            // specified character to be underlined This indicates that if the component
            // using this action has the focus and In some look and feels, this causes
            // the specified character in the label to be underlined and
            //putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));

            // Set an accelerator key; this value is used by menu items
            //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl shift A"));

    }

    public void actionPerformed(ActionEvent e) {
		int index = table.getSelectedRow();
		if (index >= 0) {
	    	Step newStep = new Step(null, "", 1);
	    	sequencer.insertStepAt(newStep,index);
	    	// We select the new step.
	    	BossaNovaData.getSingleton().getApplication().setSelectedStep(index, newStep);
			StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);

    		table.getSelectionModel().setSelectionInterval(index, index);
		}    	
    	/*
		int index = table.getSelectedRow();
		if (index >= 0) {
			sequencer.moveStepUp(index);
			table.getSelectionModel().setSelectionInterval(index - 1 , index - 1);
		}    	
*/
    }

}