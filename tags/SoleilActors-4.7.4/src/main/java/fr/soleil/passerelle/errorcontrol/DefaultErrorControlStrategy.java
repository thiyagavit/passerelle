package fr.soleil.passerelle.errorcontrol;

import ptolemy.actor.Director;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.ext.ErrorControlStrategy;
import com.isencia.passerelle.ext.impl.DefaultActorErrorControlStrategy;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * A base implementation of ErrorControlStrategy for Soleil:
 * <ul>
 * <li>can be configured as an attribute of a Director
 * <li>implements the default behaviour, so a developer only needs to override selected methods (similar to the ....Adapter classes in Swing)
 * </ul>
 * @author erwin
 *
 */
@SuppressWarnings("serial")
public class DefaultErrorControlStrategy extends Attribute implements ErrorControlStrategy {

	private ErrorControlStrategy defaultStrategy = new DefaultActorErrorControlStrategy();

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public DefaultErrorControlStrategy(Director container, String name) throws IllegalActionException, NameDuplicationException {
		super(container,name);
		DirectorUtils.getAdapter(container, null).setErrorControlStrategy(this,false);
	}

	public void handleFireException(Actor a, ProcessingException e) throws IllegalActionException {
		ExecutionTracerService.trace(a, e.getMessage());
		defaultStrategy.handleFireException(a, e);
	}

	public void handleFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException {
		ExecutionTracerService.trace(a, e.getMessage());
		defaultStrategy.handleFireRuntimeException(a, e);
	}

	public void handleInitializationException(Actor a, InitializationException e) throws IllegalActionException {
		ExecutionTracerService.trace(a, e.getMessage());
		defaultStrategy.handleInitializationException(a, e);
	}

	public void handlePostFireException(Actor a, ProcessingException e) throws IllegalActionException {
		ExecutionTracerService.trace(a, e.getMessage());
		defaultStrategy.handlePostFireException(a, e);
	}

	public void handlePostFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException {
		ExecutionTracerService.trace(a, e.getMessage());
		defaultStrategy.handlePostFireRuntimeException(a, e);
	}

	public void handlePreFireException(Actor a, ProcessingException e) throws IllegalActionException {
		ExecutionTracerService.trace(a, e.getMessage());
		defaultStrategy.handlePreFireException(a, e);
	}

	public void handlePreFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException {
		ExecutionTracerService.trace(a, e.getMessage());
		defaultStrategy.handlePreFireRuntimeException(a, e);
	}

	public void handleTerminationException(Actor a, TerminationException e) throws IllegalActionException {
		ExecutionTracerService.trace(a, e.getMessage());
		defaultStrategy.handleTerminationException(a, e);
	}

	public void handleInitializationValidationException(Actor a, ValidationException e) throws IllegalActionException {
		ExecutionTracerService.trace(a, e.getMessage());
		defaultStrategy.handleInitializationValidationException(a, e);
	}

	public void handleIterationValidationException(Actor a, ValidationException e) throws IllegalActionException {
		ExecutionTracerService.trace(a, e.getMessage());
		defaultStrategy.handleIterationValidationException(a, e);
	}
}
