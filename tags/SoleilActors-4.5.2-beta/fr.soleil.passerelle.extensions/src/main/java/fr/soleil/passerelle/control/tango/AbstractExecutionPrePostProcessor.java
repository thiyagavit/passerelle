package fr.soleil.passerelle.control.tango;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.ext.ExecutionPrePostProcessor;

@SuppressWarnings("serial")
public abstract class AbstractExecutionPrePostProcessor extends Attribute  implements ExecutionPrePostProcessor {

	public AbstractExecutionPrePostProcessor(Director container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		container.setExecutionPrePostProcessor(this);
	}

	public final void preProcess() {			
		try {
			doPreProcess();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	protected abstract void doPreProcess();
	
	public final void postProcess() {
		try {
			doPostProcess();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected abstract void doPostProcess();



}
