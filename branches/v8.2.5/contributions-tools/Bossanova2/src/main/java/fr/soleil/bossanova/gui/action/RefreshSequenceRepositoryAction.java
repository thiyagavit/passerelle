/**
 * 
 */
package fr.soleil.bossanova.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import fr.soleil.bossanova.configuration.RepositoryManager;

/**
 * @author viguier
 *
 */
public class RefreshSequenceRepositoryAction extends AbstractAction {

	public RefreshSequenceRepositoryAction(){
		super("Reload");
	}
	public void actionPerformed(ActionEvent e) {
		RepositoryManager.getSequenceRepository().loadRepository();
	}

}
