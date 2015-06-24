package fr.soleil.bossanova.gui.view.actorListSelectorViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ToolTipManager;

import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.resources.Icons;

@SuppressWarnings("serial")
public class ActorListSelectorViewer extends JDialog {

	JButton closeButton;
	JCheckBox selectButton;
	JTable table;
	// -----------------------------------------------------------------------------------
	// CONSTRUCTOR
	// -----------------------------------------------------------------------------------
	public ActorListSelectorViewer() {
		super(BossaNovaData.getSingleton().getApplication().getMainScreen(), "Actors Selector");
		initUI();
		setSize(new Dimension(250,500));
		setDialogLocation();
	}
    private void setDialogLocation() {
        Rectangle r = BossaNovaData.getSingleton().getApplication().getMainScreen().getBounds();
        int x = r.x + (r.width - getSize().width)/2;
        int y = r.y + (r.height - getSize().height)/2;
        setLocation(x, y);
    }
	private void initUI() {
		// TABLE
		ToolTipManager.sharedInstance().setInitialDelay(0);
		ActorListSelectorTableModel tableModel = new ActorListSelectorTableModel();
		ActorListSelectorViewerColumnModel colModel = new ActorListSelectorViewerColumnModel();
        //JTable table = new JTable(tableModel,colModel);
        table = new JTable(tableModel,colModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		// Bug 17625
		table.setAutoCreateRowSorter(true);

		JScrollPane tableScrollPane = new JScrollPane(table);
		add(tableScrollPane, BorderLayout.NORTH);
		// BUTTON
		selectButton = new JCheckBox( selectAllAction());
		selectButton.setText("Select/Deselect All");
        add(selectButton, BorderLayout.CENTER);
		
		closeButton = new JButton(createCloseAction());
		closeButton.setIcon(Icons.getIcon("bossanova.closeActorListSelector"));
		add(closeButton, BorderLayout.SOUTH);
	}
    private Action createCloseAction(){
        Action result = new AbstractAction("Close"){
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        return result;
    }
    private Action selectAllAction(){
        Action result = new AbstractAction("Select/Deselect All"){
            public void actionPerformed(ActionEvent e) {
                Boolean selectAll = selectButton.isSelected();
                int iTableRowInd, iTableRowNum;
                iTableRowNum=table.getRowCount();
                
                for( iTableRowInd=0; iTableRowInd<iTableRowNum; iTableRowInd++ )
                {
                    table.setValueAt(selectAll, iTableRowInd, 1);
                }
            }
        };
        return result;
    }
}
