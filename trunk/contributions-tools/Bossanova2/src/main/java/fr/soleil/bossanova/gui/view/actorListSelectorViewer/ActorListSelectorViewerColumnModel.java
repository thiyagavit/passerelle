package fr.soleil.bossanova.gui.view.actorListSelectorViewer;

import org.jdesktop.swingx.JXTable.BooleanEditor;

import fr.soleil.bossanova.gui.components.AbstractBossanovaColumnModel;
import fr.soleil.bossanova.gui.components.BossanovaBooleanTableCellRenderer;
import fr.soleil.bossanova.gui.components.BossanovaTableCellRenderer;

@SuppressWarnings("serial")
public class ActorListSelectorViewerColumnModel extends AbstractBossanovaColumnModel {

	public ActorListSelectorViewerColumnModel() {
		super();
		init();
	}
	private void init() {
		addColumn(ActorListSelectorTableModel.COLUMN_NAMES[ActorListSelectorTableModel.NAME_COLUMN],
				ActorListSelectorTableModel.NAME_COLUMN, 70, new BossanovaTableCellRenderer(), null);
		addColumn(ActorListSelectorTableModel.COLUMN_NAMES[ActorListSelectorTableModel.ENABLED_COLUMN],
				ActorListSelectorTableModel.ENABLED_COLUMN, 20, new BossanovaBooleanTableCellRenderer(),
				new BooleanEditor());
	}
}
