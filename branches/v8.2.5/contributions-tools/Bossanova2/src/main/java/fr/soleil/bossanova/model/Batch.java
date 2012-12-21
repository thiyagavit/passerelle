package fr.soleil.bossanova.model;

import java.util.List;
import java.util.Vector;

import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.configuration.RepositoryManager;
import fr.soleil.bossanova.gui.view.batchViewer.BatchLoadSaveMonitor;

/**
 * A <code>Batch</code> contains an ordered list of <code>Step</code>s.
 * 
 * @author VIGUIER
 * 
 */
public class Batch {

	// use Vector, because this implementation of java.util.List is synchronized
	private List<Step> steps;
//	private transient Director batchDirector = null;
	
	// public BatchLoadSaveMonitor BLSMonitor;

	public Batch() {
		steps = new Vector<Step>(); 
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// Add STEP
	// //////////////////////////////////////////////////////////////////////////////////////////
	public boolean addStep(Step step, boolean checkStep) {
		return addStep(steps.size(), step, checkStep);
	}

	public boolean addStep(int index, Step step, boolean checkStep) {
		boolean result = false;
		if (step != null) {
			List<String> allElements = RepositoryManager.getElementNames();
			if (allElements != null) {
				if (allElements.contains(step.getName()) || !checkStep) {
					result = true;
					steps.add(index, step);
				}
			}
		}
		return result;
	}

	/**
	 * Insert a step in the list
	 * 
	 * @param step
	 * @param index
	 */
	public boolean insertStepAt(Step step, int index) {
		boolean result = false;
		if (step != null && index >= 0) {
			result = addStep(index, step, false);
		}
		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// Remove STEP
	// //////////////////////////////////////////////////////////////////////////////////////////
	public Step removeStepAt(int index) {
		return steps.remove(index);
	}

	public boolean removeStep(Step step) {
		return steps.remove(step);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// Accessors
	// //////////////////////////////////////////////////////////////////////////////////////////
	public Step getStep(int index) {
		Step result = null;
		if (!steps.isEmpty()) {
			result = steps.get(index);
		}
		return result;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> stepsToSet) {
		this.steps = stepsToSet;
	}

	public int size() {
		return steps.size();
	}

	public void setAllConfiguredParameters() {
	    int stepNum = getSteps().size();
	    int currentStepInd = 0, percentCompleted = 0;
	    String tracePercent;
	    
	    BatchLoadSaveMonitor BLSMonitor = new BatchLoadSaveMonitor("Saving batch");
	    
	    BLSMonitor.setLocationRelativeTo(BossaNovaData.getSingleton().getApplication().getMainScreen());
	    BLSMonitor.setVisible(true);
	    	    
		for (Step step : getSteps()) {
			step.applyFlowParametersToStep();
			//Bug 17645
            //BossaNovaData.getSingleton().getApplication().setSelectedStep(step, true);
			try
			{
			    Thread.sleep(100);
			}
			catch (final InterruptedException e)
			{
			    e.printStackTrace();
			}
			currentStepInd++;
			if( (currentStepInd * 100) / stepNum > percentCompleted)
			{
			    percentCompleted = (currentStepInd * 100) / stepNum;
			    tracePercent = percentCompleted + "% Completed";
			    
			    BLSMonitor.updateBar(percentCompleted);
			    BLSMonitor.setString(tracePercent);
			    BLSMonitor.update(BLSMonitor.getGraphics());
	 
			}

		}
		BLSMonitor.setVisible(false);
	}

	public int getIndexForStep(Step step) {
		return steps.indexOf(step);
	}

//	public Director getBatchDirector() {
//		return batchDirector;
//	}
//
//	public void setBatchDirector(Director batchDirector) {
//		this.batchDirector = batchDirector;
//	}

}
