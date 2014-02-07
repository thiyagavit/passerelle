package fr.soleil.bossanova.gui.view.actorListSelectorViewer;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import fr.soleil.bossanova.configuration.ActorRepository;
import fr.soleil.bossanova.configuration.RepositoryManager;
import fr.soleil.bossanova.gui.view.BossaNovaViewFactory;;

@SuppressWarnings("serial")
public class ActorListSelectorTableModel extends DefaultTableModel {

	final static String[] COLUMN_NAMES = new String[] { "Name", "Enabled" };
	public final static int NAME_COLUMN = 0;
	public final static int ENABLED_COLUMN = 1;
	private ActorRepository actorRepo;
	// -----------------------------------------------------------------------------------
	// CONSTRUCTOR
	// -----------------------------------------------------------------------------------
	public ActorListSelectorTableModel() {
		super(COLUMN_NAMES, 0);
		refreshData();
	}
	
	// Bug 17625
	public void selectAllActors( boolean mode)
	{
	    int iRowInd;
	    int iRowNum = getRowCount();

	    for( iRowInd=0; iRowInd < iRowNum; iRowInd++)
	    {
	        setValueAt( mode, ENABLED_COLUMN, iRowInd) ;
	    }
	}
    // End Bug 17625
	
	private void refreshData() {
		actorRepo = RepositoryManager.getActorRepository();
		List<String> actorNames = actorRepo.getAllActorNames();
		for (String actorName : actorNames) {
			addRow(new Object[] { actorName, Boolean.valueOf((actorRepo.isElementEnabled(actorName))) });
		}
	}
	@Override
	public boolean isCellEditable(int row, int column) {
		return column > 0;
	}
	@Override
	public void setValueAt(Object value, int row, int column) {
		super.setValueAt(value, row, column);
		String elementName = (String) getValueAt(row, NAME_COLUMN);
		if (value instanceof Boolean) {
			Boolean bValue = (Boolean) value;
			if (bValue.booleanValue()) {
				actorRepo.addEnabledElement(elementName);
			} else {
				actorRepo.removeEnabledElement(elementName);
			}
		}
	}
}
