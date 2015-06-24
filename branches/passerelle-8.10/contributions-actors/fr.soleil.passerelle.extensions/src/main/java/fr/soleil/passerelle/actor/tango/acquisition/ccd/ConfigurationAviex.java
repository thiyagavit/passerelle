package fr.soleil.passerelle.actor.tango.acquisition.ccd;

import java.util.LinkedHashMap;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class ConfigurationAviex extends CCDConfigurator {
	
	static LinkedHashMap<String, Integer> trigModeMap = new LinkedHashMap<String, Integer>();
	static { 
		trigModeMap.put("Internal", 1);/* Frames are taken with the specified timing between exposures */
		trigModeMap.put("External", 2);/* Each exposure is started by an external signal */
		}
	
	public ConfigurationAviex(CompositeEntity arg0, String arg1) throws NameDuplicationException, IllegalActionException {
		super(arg0, arg1,trigModeMap);
	
	}
}
