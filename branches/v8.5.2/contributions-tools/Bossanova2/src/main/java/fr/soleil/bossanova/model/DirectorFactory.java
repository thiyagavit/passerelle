/**
 * 
 */
package fr.soleil.bossanova.model;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.domain.cap.Director;
import fr.soleil.bossanova.configuration.Configuration;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.domain.RecordingDirector;

/**
 * @author viguier
 * 
 */
public class DirectorFactory {

	private DirectorFactory() {
		// DO NOT USE
	}
	public static Director createNewDirector(DirectorType dType, Workspace wspace) throws Exception {
		Director result = null;
		if (DirectorType.RECORDING.equals(dType)) {
			result = new RecordingDirector(wspace);
		} else if (DirectorType.SOLEILSTD.equals(dType)) {
			result = new BasicDirector(wspace);
		}
		// Added to avoid FindBugs bug JC Pret Jan 2011
		else
		{
			throw new IllegalArgumentException("Unknown director type");
		}
		// End JCP
		// Erwin DL : this is not right. Class name has a specific purpose.
		// result.setClassName("Bossanova Director");
		result.setName("Bossanova Director");
		// no longer done. System properties must be set elsewhere, e.g. in a hmi.ini file.
//		Parameter directorParam = (Parameter) result.getAttribute("Properties File", Parameter.class);
//		directorParam.setExpression(Configuration.getPasserelleConfDirectory() + "systemproperties.txt");
		return result;
	}

	public static Director createNewRecordingDirector(Workspace wspace) throws Exception {
		return createNewDirector(DirectorType.RECORDING,  wspace);
	}

	public static Director createNewSoleilDirector(Workspace wspace) throws Exception {
		return createNewDirector(DirectorType.SOLEILSTD,  wspace);
	}
}
