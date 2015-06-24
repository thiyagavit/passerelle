package fr.soleil.bossanova.model;

import java.util.List;
import java.util.Vector;

public class StepBlock{
	private int iterationCount = 1;
	private List<Step> steps;

	public StepBlock() {
		steps = new Vector<Step>(); 
	}
	
	public StepBlock( int count ) {
		iterationCount = count;
		steps = new Vector<Step>(); 
	}
	
	public boolean addStep( Step step )
	{
		return steps.add( step );
	}
	
	public int getIterationCount()
	{
		return iterationCount;
	}
	
	public List<Step> getSteps() {
		return steps;
	}

}
