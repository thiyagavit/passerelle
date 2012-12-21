package fr.soleil.bossanova.gui.view.batchViewer;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXTable.BooleanEditor;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import fr.soleil.bossanova.configuration.ActorRepository;
import fr.soleil.bossanova.configuration.RepositoryManager;
import fr.soleil.bossanova.configuration.SequenceRepository;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.components.AbstractBossanovaColumnModel;

@SuppressWarnings("serial")
public class BatchViewerTableColumnModel extends AbstractBossanovaColumnModel
        implements Observer {

    private final BossaNovaSequencerImpl sequencer;
    private JComboBox selectionBox;
    private BatchViewerComboBoxCellEditor comboEditor;

    // -----------------------------------------------------------------------------------
    // CONSTRUCTOR
    // -----------------------------------------------------------------------------------
    public BatchViewerTableColumnModel(BossaNovaSequencerImpl sequencer) {
        super();
        RepositoryManager.getActorRepository().addObserver(this);
        RepositoryManager.getSequenceRepository().addObserver(this);
        this.sequencer = sequencer;
        init();

    }

    private void init() {
        // Create Combobox Editor
        Vector<String> model = new Vector<String>(RepositoryManager.getElementNames());
        selectionBox = new JComboBox(model);
        selectionBox.setRequestFocusEnabled(false);
        AutoCompleteDecorator.decorate(selectionBox);
        BatchViewerComboBoxCellRenderer comboRenderer = new BatchViewerComboBoxCellRenderer(
                sequencer);
        comboEditor = new BatchViewerComboBoxCellEditor(selectionBox);

        // Integer editor
        BatchViewerIntegerTableCellEditor integerEditor = new BatchViewerIntegerTableCellEditor(
                new JTextField());

        // Integer cell renderer
        BatchViewerIntegerTableCellRenderer integerCellRender = new BatchViewerIntegerTableCellRenderer(
                sequencer);
        // Default (String mostly) renderer
        BatchViewerTableCellRenderer defaultBatchCellRenderer = new BatchViewerTableCellRenderer(
                sequencer);
        // Then create all columns
        addColumn(
                BatchViewerTableModel.COLUMN_NAMES[BatchViewerTableModel.INDEX_COLUMN],
                BatchViewerTableModel.INDEX_COLUMN, 50, integerCellRender, null);
        addColumn(
                BatchViewerTableModel.COLUMN_NAMES[BatchViewerTableModel.STEP_COLUMN],
                BatchViewerTableModel.STEP_COLUMN, 300, comboRenderer,
                comboEditor);
        addColumn(
                BatchViewerTableModel.COLUMN_NAMES[BatchViewerTableModel.COMMENT_COLUMN],
                BatchViewerTableModel.COMMENT_COLUMN, 50,
                defaultBatchCellRenderer, new DefaultCellEditor(
                new JTextField()));
        addColumn(
                BatchViewerTableModel.COLUMN_NAMES[BatchViewerTableModel.COUNT_COLUMN],
                BatchViewerTableModel.COUNT_COLUMN, 50, integerCellRender,
                integerEditor);
        addColumn(
                BatchViewerTableModel.COLUMN_NAMES[BatchViewerTableModel.ENABLE_COLUMN],
                BatchViewerTableModel.ENABLE_COLUMN, 50,
                new BatchViewerBooleanTableCellRenderer(sequencer),
                new BooleanEditor());
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof ActorRepository || o instanceof SequenceRepository) {
            updateComboboxModel();
        }
    }

    public BatchViewerComboBoxCellEditor getComboEditor() {
        // Attempt to fix bug 17623
        // updateComboboxModel();
        comboEditor = new BatchViewerComboBoxCellEditor(selectionBox);
        return comboEditor;
    }

    private void updateComboboxModel() {        
        Vector<String> items = new Vector<String>(RepositoryManager.getElementNames());
        ComboBoxModel model = new DefaultComboBoxModel(items);
        selectionBox.setModel(model);
        
    }
}
