package fr.soleil.bossanova.gui.view.batchViewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Bug 18567
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.soleil.bossanova.model.Step;

public class BatchViewerControler {

	private static List<Step> stepsToCopy = new ArrayList<Step>();

	public static void addStepToCopy(Step stepToCopy) {
		try {
			BatchViewerControler.stepsToCopy.add((Step) stepToCopy.clone());
		} catch (Exception e) {
            // Bug 18567
			LoggerFactory.getLogger("BatchViewerControler").error("Cannot Copy selected Step");
		}
	}

	public static List<Step> getStepsToCopy() {
		List<Step> result = new ArrayList<Step>();
		for (Iterator<Step> iterator = stepsToCopy.iterator(); iterator.hasNext();) {
			Step step = (Step) iterator.next();
			try {
				result.add((Step) step.clone());
			} catch (Exception e) {
                // Bug 18567
				LoggerFactory.getLogger("BatchViewerControler").error("Cannot Copy selected Step");
			}
		}
		return result;
	}

	public static void cleanStepsToCopy() {
		stepsToCopy.clear();
	}
}
