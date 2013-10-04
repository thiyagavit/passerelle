package fr.soleil.bossanova.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;

import net.infonode.docking.RootWindow;
import fr.soleil.bossanova.controller.BatchManager;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.action.AddStepAction;
import fr.soleil.bossanova.gui.action.CopyStepsAction;
import fr.soleil.bossanova.gui.action.DeleteStepAction;
import fr.soleil.bossanova.gui.action.ImportBatchAfterAction;
import fr.soleil.bossanova.gui.action.InsertStepDownAction;
import fr.soleil.bossanova.gui.action.InsertStepUpAction;
import fr.soleil.bossanova.gui.action.LoadBatchAction;
import fr.soleil.bossanova.gui.action.MoveDownStepAction;
import fr.soleil.bossanova.gui.action.MoveUpStepAction;
import fr.soleil.bossanova.gui.action.NewBatchAction;
import fr.soleil.bossanova.gui.action.OpenActorListSelectorViewerAction;
import fr.soleil.bossanova.gui.action.PasteStepsAction;
import fr.soleil.bossanova.gui.action.RefreshSequenceRepositoryAction;
import fr.soleil.bossanova.gui.action.SaveBatchAction;
import fr.soleil.bossanova.gui.action.SaveBatchAsAction;
import fr.soleil.bossanova.gui.action.SelectDeselectAllStepAction;
//Bug 17627
import fr.soleil.bossanova.gui.action.CutStepsAction;
//Bug 17628
import fr.soleil.bossanova.gui.action.QuitAction;
//Bug 17570
import fr.soleil.bossanova.gui.action.CreateBlockAction;



public class ScreenManager {

	private static Action addStepAction = null;
	private static Action copyStepsAction = null;
	private static Action deleteStepAction = null;
	private static Action importBatchAfterAction = null;
	private static Action insertStepDownAction = null;
	private static Action insertStepUpAction = null;
	private static Action loadBatchAction = null;
	private static Action moveDownStepAction = null;
	private static Action moveUpStepAction = null;
	private static Action newBatchAction = null;
	private static Action openActorListSelectorViewerAction = null;
	private static Action pasteStepsAction = null;
	private static Action saveBatchAction = null;
	private static Action saveBatchAsAction = null;
	private static Action selectDeselectAllStepAction = null;
	private static Action refreshSequenceRepositoryAction = null;
    //Bug 17627
    private static Action cutStepsAction = null;
    //Bug 17628
    private static Action quitAction = null;
    //Bug 17570
	private static Action createBlockAction = null;

	public static Action getAddStepAction(BossaNovaSequencerImpl sequencer) {
		if (addStepAction == null) {
			addStepAction = new AddStepAction(sequencer);
		}
		return addStepAction;
	}

	public static Action getCopyStepsAction(BossaNovaSequencerImpl sequencer, JTable table) {
		if (copyStepsAction == null) {
			copyStepsAction = new CopyStepsAction(sequencer, table);
		}
		return copyStepsAction;
	}

	public static Action getDeleteStepAction(BossaNovaSequencerImpl sequencer, JTable table) {
		if (deleteStepAction == null) {
			deleteStepAction = new DeleteStepAction(sequencer, table);
		}
		return deleteStepAction;
	}

	public static Action getImportBatchAfterAction(BossaNovaSequencerImpl sequencer) {
		if (importBatchAfterAction == null) {
			importBatchAfterAction = new ImportBatchAfterAction(sequencer);
		}
		return importBatchAfterAction;
	}

	public static Action getInsertStepDownAction(BossaNovaSequencerImpl sequencer, JTable table) {
		if (insertStepDownAction == null) {
			insertStepDownAction = new InsertStepDownAction(sequencer, table);
		}
		return insertStepDownAction;
	}

	public static Action getInsertStepUpAction(BossaNovaSequencerImpl sequencer, JTable table) {
		if (insertStepUpAction == null) {
			insertStepUpAction = new InsertStepUpAction(sequencer, table);
		}
		return insertStepUpAction;
	}

	public static Action getLoadBatchAction() {
		if (loadBatchAction == null) {
			loadBatchAction = new LoadBatchAction();
		}
		return loadBatchAction;
	}

	public static Action getMoveDownStepAction(BossaNovaSequencerImpl sequencer, JTable table) {
		if (moveDownStepAction == null) {
			moveDownStepAction = new MoveDownStepAction(sequencer, table);
		}
		return moveDownStepAction;
	}

	public static Action getMoveUpStepAction(BossaNovaSequencerImpl sequencer, JTable table) {
		if (moveUpStepAction == null) {
			moveUpStepAction = new MoveUpStepAction(sequencer, table);
		}
		return moveUpStepAction;
	}

	public static Action getNewBatchAction() {
		if (newBatchAction == null) {
			newBatchAction = new NewBatchAction();
		}
		return newBatchAction;
	}

	public static Action getOpenActorListSelectorViewerAction() {
		if (openActorListSelectorViewerAction == null) {
			openActorListSelectorViewerAction = new OpenActorListSelectorViewerAction();
		}
		return openActorListSelectorViewerAction;
	}

	public static Action getPasteStepsAction(BossaNovaSequencerImpl sequencer, JTable table) {
		if (pasteStepsAction == null) {
			pasteStepsAction = new PasteStepsAction(sequencer, table);
		}
		return pasteStepsAction;
	}
	public static Action getSaveBatchAction() {
		if (saveBatchAction == null) {
			saveBatchAction = new SaveBatchAction();
		}
		return saveBatchAction;
	}

	public static Action getSaveBatchAsAction() {
		if (saveBatchAsAction == null) {
			saveBatchAsAction = new SaveBatchAsAction();
		}
		return saveBatchAsAction;
	}

	public static Action getSelectDeselectAllStepAction(BossaNovaSequencerImpl sequencer, JTable table) {
		if (selectDeselectAllStepAction == null) {
			selectDeselectAllStepAction = new SelectDeselectAllStepAction(sequencer, table);
		}
		return selectDeselectAllStepAction;
	}

	// Bug 17627
   public static Action getCutStepsAction(BossaNovaSequencerImpl sequencer, JTable table) {
        if (cutStepsAction == null) {
            cutStepsAction = new CutStepsAction(sequencer, table);
        }
        return cutStepsAction;
    }

   // Bug 17628
   public static Action getQuitAction() {
       if (quitAction == null) {
           quitAction = new QuitAction();
       }
       return quitAction;
   }
   
	public static Action getCopyStepsAction() {
		return copyStepsAction;
	}

	public static Action getPasteStepsAction() {
		return pasteStepsAction;
	}
	
	//Bug 17627
	
	   public static Action getCutStepsAction() {
	        return cutStepsAction;
	    }

	public static Action getRefreshSequenceRepositoryAction(){
		if (refreshSequenceRepositoryAction == null){
			refreshSequenceRepositoryAction = new RefreshSequenceRepositoryAction();
		}
		return refreshSequenceRepositoryAction;
	}
	
	//Bug 17570
	public static Action getCreateBlockAction(BossaNovaSequencerImpl sequencer, JTable table) {
		if (createBlockAction == null) {
			createBlockAction = new CreateBlockAction(sequencer, table);
		}
		return createBlockAction;
	}
	
	public static void loadDefaultWindowLayoutPreferences(RootWindow rootWindow, byte[] defaultLayout) {
		
		try {
			if (defaultLayout != null) {
				rootWindow.read(new ObjectInputStream(new ByteArrayInputStream(defaultLayout)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadWindowLayoutPreferences(RootWindow rootWindow, byte[] defaultLayout) {
		byte[] prefs = BossanovaUserPref.getByteArrayPref(BossanovaUserPref.LAYOUT, defaultLayout);
		try {
			if (prefs != null) {
				rootWindow.read(new ObjectInputStream(new ByteArrayInputStream(prefs)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveWindowLayoutPreferences(RootWindow rootWindow) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			rootWindow.write(out, false);
			out.close();
			byte[] layout = bos.toByteArray();
			BossanovaUserPref.putByteArrayPref(BossanovaUserPref.LAYOUT, layout);
			bos.close();
			// Removing null assignment to avoid Sonar Critical violation JC Pret Jan 2011
			// bos = null;
			// End Removing null assignment
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(rootWindow, "ScreenManager.saveWindowLayoutPreferences() :"
					+ " Unexpected Error (see traces)", "BossaNova - Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//Bug 17628
    public static void saveUnsavedBatch(RootWindow rootWindow)
    {
        try
        {
            if( BossaNovaSequencerImpl.getCurrentBatchSaved() ==  false )
            {
                int iSaveBatch =  JOptionPane.showConfirmDialog(rootWindow, "Voulez-vous sauvegarder le batch en cours?", "Modifications non sauvegardees", JOptionPane.YES_NO_OPTION);
                if( iSaveBatch == JOptionPane.YES_OPTION  )
                {
                    String defaultPath = BossanovaUserPref.getPref(BossanovaUserPref.BATCH_DIRECTORY, System.getProperty("user.home"));
                    JFileChooser fileChooser = new JFileChooser(defaultPath);
                    fileChooser.showSaveDialog(rootWindow);
                    File selectedFile = fileChooser.getSelectedFile();
                    if (selectedFile != null)
                    {
                        BatchManager.saveBatchAs(fileChooser.getSelectedFile());
                        BossanovaUserPref.putPref(BossanovaUserPref.BATCH_DIRECTORY, fileChooser.getSelectedFile().getParent());
                    }
                }
            }
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
            PopupUtil.showError(rootWindow, HMIMessages.ERROR_GENERIC,
                    "Didn't work out" + e1.getMessage());
        }
    }
    
}
