package fr.soleil.passerelle.actor.tango.acquisition.ccd;

import java.util.LinkedHashMap;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class AcquisitionAviex extends CCDAcquisitionPerformer{

	static LinkedHashMap<String, Integer> acqModeMap = new LinkedHashMap<String, Integer>();
	static { 
		acqModeMap.put("OneShot", 0); /* takes one frame with given exposure time */
		acqModeMap.put("MultiFrame", 2);  /* takes a sequence of frames with given exposure time and timing between frames */
		acqModeMap.put("CircularMultiFrame", 3);/* takes a sequence of frames, loop back and overwrite old frames */
		acqModeMap.put("Geometrical", 4);
		acqModeMap.put("SubImage", 5);
		acqModeMap.put("StreakCamera", 6);
		acqModeMap.put("Strobe", 10);/* external trigger for each frame, using given exposure time */
		acqModeMap.put("Bulb", 11);/* exposure time given by the external pulse length */
		acqModeMap.put("MeasureDark", 20);/* special mode for measuring dark current frame */
		acqModeMap.put("MeasureFloodField", 21); /* special mode for measuring flood field frame */
		}
	                                                                      
	
	
	public AcquisitionAviex(CompositeEntity arg0, String arg1) throws NameDuplicationException, IllegalActionException {
		super(arg0, arg1,acqModeMap,true);
	
	}
	
}
