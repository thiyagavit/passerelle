
package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.resources.Icons;



/**
 * Best linked to a button in the DIrector panel.
 * When clicked, ensures that the current Director cfg is applied to all
 * steps that use a Batch director clone.
 *
 * REMARK : implemented by just putting new clones in them!
 *
 * @author delerw
 *
 */
public class ApplyDirectorChangeToAllStepsAction extends AbstractBossanovaAction {

    public ApplyDirectorChangeToAllStepsAction(BossaNovaSequencerImpl sequencer) {
        super(sequencer);


        // This is an instance initializer; it is executed just after the
        // constructor of the superclass is invoked

            // The following values are completely optional
        	putValue(Action.NAME, "Apply changes to all actors steps");
            // Set tool tip text
            putValue(Action.SHORT_DESCRIPTION, "Applies director config to all actors steps.");

            // This text is not directly used by any Swing component;
            // however, this text could be used in a help system
            putValue(Action.LONG_DESCRIPTION, "Applies the director configuration to all steps using a batch director");
    }

    public void actionPerformed(ActionEvent e) {
    	sequencer.applyDirectorConfigChangeToAllSteps();
    }
}