/**
 *
 */
package fr.soleil.bossanova.gui.view.batchViewer;

import java.util.Observable;
import java.util.Observer;

import javax.swing.table.DefaultTableModel;

import fr.soleil.bossanova.configuration.RepositoryManager;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.model.Step;
import fr.soleil.bossanova.model.StepType;

/**
 * @author HARDION,VIGUIER
 * 
 */
public class BatchViewerTableModel extends DefaultTableModel implements Observer {

	final static String[] COLUMN_NAMES = new String[] { "Order", "Step", "Comment", "Nb", "Enable" };
	public final static int INDEX_COLUMN = 0;
	public final static int STEP_COLUMN = 1;
	public final static int COMMENT_COLUMN = 2;
	public final static int COUNT_COLUMN = 3;
	public final static int ENABLE_COLUMN = 4;
	private BossaNovaSequencerImpl sequencer;

	// -----------------------------------------------------------------------------------
	// CONSTRUCTOR
	// -----------------------------------------------------------------------------------
	public BatchViewerTableModel(BossaNovaSequencerImpl sequencer) {
		this(sequencer, 0);
	}

	public BatchViewerTableModel(BossaNovaSequencerImpl sequencer, int rowCount) {
		super(COLUMN_NAMES, rowCount);
		this.sequencer = sequencer;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		int currentRunningStepIndex = sequencer.getCurrentRunningStepIndex();
		if (currentRunningStepIndex > -1) { // a batch is running ...
			if (row <= currentRunningStepIndex)
				return false;
		}
		// Note that the data/cell address is constant,
		// no matter where the cell appears onscreen.
		if (col < STEP_COLUMN) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String getColumnName(int c) {
		return COLUMN_NAMES[c];
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (column != 0)
			return super.getValueAt(row, column);
		else
			return row + 1;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		if (value != null) {
			super.setValueAt(value, row, column);
			String newStepName = (String) getValueAt(row, STEP_COLUMN);
			StepType stepType = RepositoryManager.getStepTypeFor(newStepName);
			switch (column) {
			case INDEX_COLUMN:
				break;
			case STEP_COLUMN:
				sequencer.modifyTypeAndNameForStepAt(row, newStepName, stepType);
				break;
			case COMMENT_COLUMN:
				sequencer.modifyCommentForStepAt(row, (String) value);
				break;
			case COUNT_COLUMN:
				sequencer.modifyIterationCountForStepAt(row, (Integer) value);
				break;
			case ENABLE_COLUMN:
				sequencer.modifyEnableForStepAt(row, (Boolean) value);
				break;
			default:
			}
		}
		fireTableDataChanged();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> result = Object.class;
		Object object = getValueAt(0, columnIndex);
		if (object != null) {
			result = object.getClass();
		}
		return result;
	}

	public void clear() {
		this.setRowCount(0);
	}

	public void update(Observable o, Object arg) {
		clear();
		if (o instanceof BossaNovaSequencerImpl) {
			BossaNovaSequencerImpl sequencer = (BossaNovaSequencerImpl) o;
			for (Step step : sequencer.getBatch().getSteps()) {
				if (step != null) {
					addRow(new Object[] { Integer.toString(getRowCount()), step.getName(), step.getComment(),
							step.getIterationCount(), step.isEnable() });
				}
			}
		}
	}
}
