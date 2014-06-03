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

/**
 * @author root
 *
 */
@SuppressWarnings("serial")
public class SamplePrePostProcessor extends AbstractExecutionPrePostProcessor implements ExecutionPrePostProcessor {

	private final static Logger logger = LoggerFactory.getLogger(SamplePrePostProcessor.class);

	public SamplePrePostProcessor(Director container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		DirectorUtils.getAdapter(container, null).setExecutionPrePostProcessor(this);
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.ext.ExecutionPrePostProcessor#postProcess()
	 */
	public void doPostProcess() {
		logger.debug("in postProcess");
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.ext.ExecutionPrePostProcessor#preProcess()
	 */
	public void doPreProcess() {
		logger.debug("in preProcess");
	}

}
