/**
 *
 */
package fr.soleil.passerelle.control.tango;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.ext.ExecutionPrePostProcessor;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoDs.TangoConst;

/**
 * @author root
 *
 */
@SuppressWarnings("serial")
public class GeneralPrePostProcessor extends AbstractExecutionPrePostProcessor implements ExecutionPrePostProcessor {

	private final static Logger logger = LoggerFactory.getLogger(GeneralPrePostProcessor.class);

	private Director dir;
	private String modelName;
	public GeneralPrePostProcessor(Director container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		dir = container;
		//container.setExecutionPrePostProcessor(this);
		String directorName = container.getFullName();
		modelName = directorName.substring(1, container.getFullName().lastIndexOf("."));
		//System.out.println("ctr GeneralPrePostProcessor "+directorName);
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.ext.ExecutionPrePostProcessor#preProcess()
	 */
	public void doPreProcess() {
	  boolean isMockMode = DirectorUtils.getAdapter(dir, null).isMockMode();
		if(isMockMode){
			ExecutionTracerService.trace(dir,"MOCK- ###START SEQUENCE "+modelName +"###");
		}else{
			ExecutionTracerService.trace(dir,"###START SEQUENCE "+modelName +"###");
			//allow all access on tangodb
			Database db;
			try {
				db = ApiUtil.get_db_obj();
				db.setAccessControl(TangoConst.ACCESS_WRITE);
			} catch (DevFailed e) {
				logger.debug("Connection to TANGO failed", e);
				ExecutionTracerService.trace(dir,"Connection to TANGO db failed - could not set write access");
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.ext.ExecutionPrePostProcessor#postProcess()
	 */
	public void doPostProcess() {
    boolean isMockMode = DirectorUtils.getAdapter(dir, null).isMockMode();
    if(isMockMode){
			ExecutionTracerService.trace(dir,"MOCK- ###END SEQUENCE "+modelName +"###");
		}else{
			ExecutionTracerService.trace(dir,"###END SEQUENCE "+modelName +"###");
		}
	}

}
