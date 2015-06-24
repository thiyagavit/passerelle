package fr.soleil.bossanova.gui.action;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.view.batchViewer.BatchViewerControler;
import fr.soleil.bossanova.model.Step;
import fr.soleil.bossanova.resources.Icons;

public class CreateBlockAction extends AbstractMultipleSelectionAction {
	
	private int iterationCount;

	public CreateBlockAction(BossaNovaSequencerImpl sequencer, JTable table) {
		super(sequencer, table);
		// This is an instance initializer; it is executed just after the
		// constructor of the superclass is invoked

		// The following values are completely optional
		putValue(Action.NAME, "Create Block");
		// Set tool tip text
		putValue(Action.SHORT_DESCRIPTION, "Create a block from selected steps");

		// This text is not directly used by any Swing component;
		// however, this text could be used in a help system
		putValue(Action.LONG_DESCRIPTION, "CCreate a block from selected steps");

		// Set an icon
		// Icon icon = new ImageIcon("icon.gif");
		//putValue(Action.SMALL_ICON, Icons.getIcon("bossanova.list.copy"));

		// Set a mnemonic character. In most look and feels, this causes the
		// specified character to be underlined This indicates that if the
		// component
		// using this action has the focus and In some look and feels, this
		// causes
		// the specified character in the label to be underlined and
		//putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_C);

		// Set an accelerator key; this value is used by menu items
		//putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));

	}
	@Override
	protected void doSpecificPreTreatment()
	{
		if( getTable().getSelectedRowCount() == 0 )
		{
            JOptionPane.showMessageDialog(null, "Cannot create block\nNo row selected", "Warning", JOptionPane.WARNING_MESSAGE );
		}
		else
		{
			int[] selection = getTable().getSelectedRows();
			boolean emptyStepFound = false;
			
			for( int i_row = 0; ( i_row < selection.length ) && ( emptyStepFound == false ); i_row++ )
			{
				int iselected_row = selection[i_row];
				String stepstr = sequencer.getBatch().getStep(iselected_row).getName();
				emptyStepFound = stepstr.equals("");
			}
			
			if( emptyStepFound == true )
			{
	            JOptionPane.showMessageDialog(null, "Cannot create block\nAt least one of the selected steps is empty", "Warning", JOptionPane.WARNING_MESSAGE );
	            getTable().clearSelection();
			}
			else
			{

				Object[] possibleValues = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
				Object selectedValue = JOptionPane.showInputDialog(null,
			            "Please select iteration count", "Creating block",
			            JOptionPane.INFORMATION_MESSAGE, null,
			            possibleValues, possibleValues[0]);
				String result = (String)selectedValue;
				iterationCount = Integer.parseInt( result );
				
				Step voidStep = new Step();
				voidStep.setName("Block");
				voidStep.setIterationCount(iterationCount);
				
				int positionToInsert = getTable().getSelectedRow();			
				sequencer.insertStepAt(voidStep, positionToInsert); 
			
				if( selection.length == 0)
				{
					System.out.println( "No rows selected" );
				}
				else
				{	
					int firstSelectedRow = selection[0] + 1;
					getTable().getSelectionModel().setSelectionInterval( firstSelectedRow, firstSelectedRow  + selection.length - 1 );
				}
			}
		}
	}
	
	public void doSpecificUnitaryActionOnRow(int row) {
		Step step = sequencer.getBatch().getStep(row);
		if (step != null) {
			step.setBlockId(BossaNovaSequencerImpl.getCurrentBlockInd());
			step.setBlockIterationCount(iterationCount);
			BossaNovaSequencerImpl.incCurrentBlockInd();
			
			Object stepObj = getTable().getValueAt(row, 1 );
			String stepName = "    " + (String)stepObj;
			getTable().setValueAt(stepName, row, 1 );
		}
	}
}
