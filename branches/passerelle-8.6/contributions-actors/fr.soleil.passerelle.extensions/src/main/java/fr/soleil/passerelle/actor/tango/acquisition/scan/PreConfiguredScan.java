package fr.soleil.passerelle.actor.tango.acquisition.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.actor.tango.acquisition.Scan;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;
import fr.soleil.salsa.entity.impl.scan1d.Config1DImpl;
import fr.soleil.salsa.entity.scan1d.IConfig1D;

/**
 * Do scans using Salsa config, but the From/To NbSteps and InteegrationTime can
 * be configured
 *
 * @author GRAMER
 */
@SuppressWarnings("serial")
public class PreConfiguredScan extends Scan {

	public Port xToPort;
	public Port xNbStepsPort;
	public Port xIntegrationTimePort;

	private final static Logger logger = LoggerFactory
			.getLogger(PreConfiguredScan.class);

	public Parameter xRelativeParam;
	protected boolean xRelative;

	public PreConfiguredScan(final CompositeEntity container,
			final String name) throws IllegalActionException,
			NameDuplicationException {
		super(container, name);

		xRelativeParam = new Parameter(this, "X Relative", new BooleanToken(
				false));
		xRelativeParam.setTypeEquals(BaseType.BOOLEAN);

		input.setName("from");
		input.setExpectedMessageContentType(Double.class);

		xToPort = PortFactory.getInstance().createInputPort(this, "to",
				Double.class);

		xNbStepsPort = PortFactory.getInstance().createInputPort(this,
				"NbSteps", Double.class);

		xIntegrationTimePort = PortFactory.getInstance().createInputPort(this,
				"IntegrationTime", Double.class);

	}

	/**
	 * Initialize actor
	 */
	@Override
	public void validateInitialization() throws ValidationException {
		super.validateInitialization();
		// if (!(conf instanceof Config1DImpl)) {
		if (!(conf instanceof IConfig1D)) {
			String errorMessage = "Error: " + conf.getFullPath()
					+ " is not 1D configuration.";
			ExecutionTracerService.trace(this, errorMessage);
			throw new ValidationException(errorMessage, this, null);
		}
	}

	@Override
	protected void process(final ActorContext ctxt,
			final ProcessRequest request, final ProcessResponse response)
			throws ProcessingException {

		final ManagedMessage fromMessage = request.getMessage(input);
		final double fromX = (Double) PasserelleUtil.getInputValue(fromMessage);
		logger.debug("fromX:" + fromX);

		final ManagedMessage toMessage = request.getMessage(xToPort);
		final double toX = (Double) PasserelleUtil.getInputValue(toMessage);
		logger.debug("toX:" + toX);

		final ManagedMessage nbStepsmessage = request.getMessage(xNbStepsPort);
		final int nbStepsX = (int) ((Double) PasserelleUtil
				.getInputValue(nbStepsmessage)).doubleValue();
		logger.debug("nbStepsX:" + nbStepsX);

		final ManagedMessage intTimemessage = request
				.getMessage(xIntegrationTimePort);
		final double intTime = (Double) PasserelleUtil
				.getInputValue(intTimemessage);
		logger.debug("intTime:" + intTime);

		String logMessage = xRelative ? "Relative scan" : "Absolute scan";
		logMessage += " from " + fromX + " to " + toX + " in " + nbStepsX
		+ " steps with an integration time equals to " + intTime
		+ " s";
		if (!isMockMode()) {

			final Config1DImpl confTemp = (Config1DImpl) conf;
			final ScanRangeX scanRangeX = new ScanRangeX(fromX, toX, nbStepsX,
					intTime, xRelative);

			try {
				ScanUtil.setTrajectory1D(confTemp, scanRangeX);
			} catch (PasserelleException e) {
				throw new ProcessingExceptionWithLog(this,Severity.FATAL, e.getMessage(), this, null);
			}

			conf = confTemp;
			String log = xRelative ? "Relative scan" : "Absolute scan";
			log += " from " + fromX + " to " + toX + " in " + nbStepsX
					+ " steps with an integration time equals to " + intTime
					+ " s";

			ExecutionTracerService.trace(this, logMessage);
		} else {
			ExecutionTracerService.trace(this, "MOCK - " + logMessage);
		}
		super.process(ctxt, request, response);
	}

	@Override
	public void attributeChanged(final Attribute arg0)
			throws IllegalActionException {
		if (arg0 == xRelativeParam) {
			xRelative = PasserelleUtil.getParameterBooleanValue(xRelativeParam);
		} else {
			super.attributeChanged(arg0);
		}
	}
}
