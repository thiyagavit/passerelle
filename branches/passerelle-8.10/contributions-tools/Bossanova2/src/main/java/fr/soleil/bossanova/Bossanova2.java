package fr.soleil.bossanova;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.gui.PasserelleEditorPaneFactory;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.action.ModelExecutor;
import com.isencia.passerelle.hmi.generic.GenericHMI;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.model.Flow;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BatchManager;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.MainScreen;
import fr.soleil.bossanova.gui.ScreenManager;
import fr.soleil.bossanova.model.Step;

/**
 * @author VIGUIER
 * 
 */
public class Bossanova2 extends GenericHMI implements ExecutionListener {

	// Bug 18567
	private final static Logger logger = LoggerFactory.getLogger(Bossanova2.class);
	private final BossaNovaSequencerImpl sequencer;
	private MainScreen mainScreen;
	private int selectedStepIndex;
	private Step selectedStep;

	protected class BossanovaModelExecutionListener extends ModelExecutionListener {

		@Override
		public void executionError(Manager manager, Throwable throwable) {
			super.executionError(manager, throwable);
			sequencer.stopWithError("Error executing step "+manager.getName());
		}

		@Override
		public void executionFinished(Manager manager) {
		}

		@Override
		public void managerStateChanged(Manager manager) {
		}
		
	}
	
	/**
	 * @throws IOException
	 */
	public Bossanova2() throws IOException {
		super(true,false);
		BossaNovaData.getSingleton().setApplication(this);
		sequencer = new BossaNovaSequencerImpl();
		BossaNovaData.getSingleton().setSequencer(sequencer);
		sequencer.addExecutionListener(this);
		BatchManager.setSequencer(sequencer);

		try {
			this.setEditorPaneFactory(new PasserelleEditorPaneFactory());
		} catch (IllegalActionException e) {
			e.printStackTrace();
		} catch (NameDuplicationException e) {
			e.printStackTrace();
		}
	}

	public void initUI(boolean standAlone) {
		init();

		ResourceBundle rb = ResourceBundle.getBundle("fr.soleil.bossanova.resources.application");
		String title = rb.getString("project.name") + " " + rb.getString("project.version") + " " + rb.getString("build.date");

		mainScreen = new MainScreen(title, standAlone);
		if (standAlone) {
			mainScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			BatchManager.createNewBatch();
		}
		// Bug 17412
		JFrame mainPasserelleFrame = (JFrame) this.getDialogHookComponent();
		mainPasserelleFrame.setTitle("BossaNova");
		// End Bug 17412
		
		BossaNovaSequencerImpl.setCurrentBatchSaved(true);
	}

	public MainScreen getMainScreen() {
		return this.mainScreen;
	}

	/**
	 * refreshes the views linked to the currently selected step
	 * (most importantly : the param cfg panels)
	 */
	public void refreshCurrentlySelectedStep() {
		setSelectedStep(selectedStepIndex, selectedStep);
	}
	
	public void setSelectedStep(int index, Step step) {
		selectedStepIndex = index;
		selectedStep = step;
		if (step != null && !step.getName().isEmpty()) {
			// as we want to show the param cfg panels for the selected step,
			// we need to make sure the flow is constructed here.
			if(step.getFlow()==null) {
				try {
					sequencer.createFlow(step, false);
				} catch (BossanovaException e) {
					// Erwin DL : we will never get in here!?
					// the createFLow() eats its exceptions and shows an own popup..
					logger.error("Error showing config panel for step " + step.getName(), e);
					PopupUtil.showError(mainScreen, "Error building form for " + step.getName(), e.getMessage());
				} catch (PasserelleException pe) {
					pe.printStackTrace();
					// String message = "WARNING: Sequence " + step.getName()
					// + "  has been updated in IDE.\n";
					// message +=
					// "BossaNova cannot apply the batch parameters for this sequence.";
					// PopupUtil.showError(mainScreen, message);
					step.resetParameters();
					setSelectedStep(index, step);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (step.getFlow() != null) {
				setCurrentModel(step.getFlow(), null, null, false);
				applyFieldValuesToParameters();
				// we change the title of the parameter's panel
				changeParametersPanelTitle(step, index + 1);
			}
		} else {
			clearModelForms(null);
			setCurrentModel(null, null, null,false);
			// we change the title of the parameter's panel
			changeParametersPanelTitle(step, index + 1);
		}
	}

	public boolean renderDirectorCfgPanel(Director d, JPanel parentPanel) {
		return super.renderModelComponent(false, d, parentPanel);
	}

	@Override
	public void setCurrentModel(Flow model, URL modelURL, String modelKey, boolean loadGraphPanel) {
		super.setCurrentModel(model, modelURL, modelKey, loadGraphPanel);
	}

	public void changeParametersPanelTitle(Step step, final int stepNumber) {
		getParameterScrollPane().setBorder(BorderFactory.createTitledBorder("Parameters for step " + stepNumber + " : " + (step!=null?step.getName():"new")));
	}

	// -----------------------------------------------------------------------------------
	// HMI Overrides
	// -----------------------------------------------------------------------------------
	@Override
	public void doExitApplication() {
		super.doExitApplication();
		ScreenManager.saveWindowLayoutPreferences(mainScreen.getRootWindow());
	}

	public void launchModel() {
		launchModel(new ModelExecutor(this));
	}

	@Override
	public void launchModel(final ModelExecutor executor) {
		try {
			logger.info("Start Batch");

			// we need to stop the current editing before launching batch
			mainScreen.stopCellEditing();

			// applyFieldValuesToParameters();
			if (getTraceComponent() != null) {
				getTraceComponent().trace((Director) null, "Start Batch");
			}
			sequencer.executeBatch(executor);
		}
		// Fixed Sonar critical alert JC Pret Jan 2011
		// Replaced catch( Throwable ) with catch ( Exception ) and catch (
		// Error )
		catch (Exception e) {
			e.printStackTrace();
			PopupUtil.showError(mainScreen, "exception occurred", e.getMessage());
			StateMachine.getInstance().transitionTo(executor.getErrorState());
		} catch (Error t) {
			t.printStackTrace();
			PopupUtil.showError(mainScreen, "error.execution.error", t.getMessage());
			StateMachine.getInstance().transitionTo(executor.getErrorState());
		}
		// End Fixed Sonar critical alert

	}

	/**
	 * Method stopModel was overidded because Bossanove need to stop the
	 * sequencer thread.
	 * 
	 * @author DBA
	 */
	@Override
	public void stopModel() {
		sequencer.stop();
		super.stopModel();
	}

	// -----------------------------------------------------------------------------------
	// Execution Listener implementation
	// -----------------------------------------------------------------------------------
	@Override
	protected ModelExecutionListener createExecutionListener() {
		return new BossanovaModelExecutionListener();
	}

	public void executionError(Manager arg0, Throwable arg1) {
		logger.info("Batch interrupted with error");
		if (getTraceComponent() != null) {
			getTraceComponent().trace((Director) null, "Batch interrupted with error");
		}
		StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
	}

	@Override
	public void executionFinished(final Manager manager) {
		logger.info("Batch executed");
		if (getTraceComponent() != null) {
			getTraceComponent().trace((Director) null, "Batch executed");
		}
		StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				PopupUtil.showInfo(getDialogHookComponent(), "info.execution.finished", null);
//			}
//		});
	}

	public void managerStateChanged(Manager arg0) {
		// NOTHING TO DO
	}

	// -----------------------------------------------------------------------------------
	// End of Execution Listener implementation
	// -----------------------------------------------------------------------------------

	public static void main(String[] args) throws IOException {
		Bossanova2 bn2 = new Bossanova2();
		bn2.initUI(true);
	}

}
