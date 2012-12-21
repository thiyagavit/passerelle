/**
 *
 */
package fr.soleil.bossanova.gui.view.batchViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BatchManager;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.ScreenManager;

/**
 * @author VIGUIER
 *
 */
public class BatchViewer extends JPanel implements Observer, MouseListener {

	private BatchViewerTableModel model;
	private JTable table;
	private BatchViewerTableColumnModel colModel;
	private final BossaNovaSequencerImpl sequencer;
	private JPopupMenu popup;
	private JScrollPane tableScrollPane;

	// -----------------------------------------------------------------------------------
	// CONSTRUCTOR
	// -----------------------------------------------------------------------------------
	public BatchViewer(/* Bossanova2 application, */ BossaNovaSequencerImpl sequencer) {
		super(new BorderLayout());
		//  this.application = application;
		this.sequencer = sequencer;
		initComponents();
		addPopupMenu();
	}

	private void initComponents() {
		// setBorder(BorderFactory.createTitledBorder("Batch " +
		// BatchManager.getCurrentBatchFileName()));
		initTable();
		validate();
		setVisible(true);
	}

	private void addPopupMenu() {
		popup = new JPopupMenu();
		List<JMenuItem> menuItems = getMenuItems();
		for (Iterator<JMenuItem> iterator = menuItems.iterator(); iterator
				.hasNext();) {
			JMenuItem menuItem = iterator.next();
			popup.add(menuItem);
		}
		// Add listener to components that can bring up popup menus.
		table.addMouseListener(this);
	}

	private void initTable() {
		model = new BatchViewerTableModel(sequencer);
		colModel = new BatchViewerTableColumnModel(sequencer);
		// Create table
		table = new JTable(model, colModel);
		table.getTableHeader().setColumnModel(colModel);
		table.setRowHeight(20);
		table.setRowSelectionAllowed(true);
		table.setCellSelectionEnabled(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(true);
		table.getTableHeader().setPreferredSize(new Dimension(800, 20));
		tableScrollPane = new JScrollPane(table);
		add(tableScrollPane, BorderLayout.CENTER);
	}

	public List<JMenuItem> getMenuItems() {
		List<JMenuItem> result = new ArrayList<JMenuItem>();
		// ADD STEP
		JMenuItem menuItem = new JMenuItem(ScreenManager
				.getAddStepAction(sequencer));
		result.add(menuItem);
		// DELETE STEP
		menuItem = new JMenuItem(ScreenManager.getDeleteStepAction(sequencer,
				getTable()));
		result.add(menuItem);
		// MOVE UP
		menuItem = new JMenuItem(ScreenManager.getMoveUpStepAction(sequencer,
				getTable()));
		result.add(menuItem);
		// MOVE DOWN
		menuItem = new JMenuItem(ScreenManager.getMoveDownStepAction(sequencer,
				getTable()));
		result.add(menuItem);
		// INSERT BEFORE
		menuItem = new JMenuItem(ScreenManager.getInsertStepUpAction(sequencer,
				getTable()));
		result.add(menuItem);
		// COPY
		menuItem = new JMenuItem(ScreenManager.getCopyStepsAction(sequencer,
				getTable()));
		result.add(menuItem);
		// PASTE
		menuItem = new JMenuItem(ScreenManager.getPasteStepsAction(sequencer,
				getTable()));
		result.add(menuItem);
		// Bug 17627
		//CUT
	    menuItem = new JMenuItem(ScreenManager.getCutStepsAction(sequencer,
	                getTable()));
	    result.add(menuItem);
	    // Bug 17570
	    //CREATE BLOCK
	    menuItem = new JMenuItem(ScreenManager.getCreateBlockAction(sequencer,
                getTable()));
	    result.add(menuItem);

		return result;
	}

	public void addListeners() {
		table.addMouseListener(new RowMouseSelectionAdapter());
		table.addKeyListener(new RowKeySelectionAdapter());
		tableScrollPane.getViewport().addKeyListener(
				new RowKeySelectionAdapter());
		tableScrollPane.getViewport().addMouseListener(this);
		addMouseListener(new RowMouseSelectionAdapter());
		addKeyListener(new RowKeySelectionAdapter());
	}

	public BatchViewerTableModel getModel() {
		return model;
	}

	public BatchViewerTableColumnModel getColumnModel() {
		return colModel;
	}

	public JTable getTable() {
		return table;
	}

	public void setModel(BatchViewerTableModel model) {
		this.model = model;
	}

	public int getSelectedRowIndex() {
		return table.getSelectedRow();
	}

	public Object getCellValue(int rowIndex, int columnIndex) {
		return table.getModel().getValueAt(rowIndex, columnIndex);
	}

    // Bug 18267 Displaying current batch name
	public void displayCurrentBatchName()
	{
	    setBorder(BorderFactory.createTitledBorder( BatchManager.getCurrentBatchFileName()) );
	}

	// -----------------------------------------------------------------------------------
	// OBSERVER IMPLEMENTATION
	// -----------------------------------------------------------------------------------
	public void update(Observable o, Object arg) {
		setBorder(BorderFactory.createTitledBorder("Batch "
				+ BatchManager.getCurrentBatchFileName()));
		if (getTable().getRowCount() < 1) {
			BossaNovaData.getSingleton().getApplication()
					.getMainScreen().disableStepButton();
		}
	}

	// -----------------------------------------------------------------------------------
	// MOUSE LISTENER IMPLEMENTATION
	// -----------------------------------------------------------------------------------
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.getButton() == 3) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

}
