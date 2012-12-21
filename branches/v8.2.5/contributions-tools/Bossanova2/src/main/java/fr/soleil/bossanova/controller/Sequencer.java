/**
 * 
 */
package fr.soleil.bossanova.controller;

import java.util.List;
import java.util.Map;

/**
 * @author viguier
 * 
 */
public interface Sequencer {

	public List<String> getPossibleStepsNames();

	public List<String> getPossibleSequenceStepsNames();

	public List<String> getPossibleActorStepsNames();

	public boolean addEmptyStep(String comment);

	public boolean addSequenceStep(String stepName);

	public boolean addSequenceStep(String stepName, String comment,
			Map<String, String> parameters);

	public boolean addActorStep(String stepName);

	public boolean addActorStep(String stepName, String comment,
			Map<String, String> parameters);

	public void cleanAll();
}
