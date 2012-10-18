package fr.soleil.passerelle.actor.tango.acquisition.scan;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.soleil.passerelle.actor.tango.acquisition.ScanOLD;
import fr.soleil.passerelle.util.PasserelleUtil;

// TODO move to package Scan
@SuppressWarnings("serial")
public class PreConfiguredScanOLD extends ScanOLD {

	public Port toPort;
	public Port nbStepsPort;
	public Port integrationTimePort;

	public Parameter xRelativeParam;
	boolean xRelative;

	public PreConfiguredScanOLD(final CompositeEntity container, final String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		xRelativeParam = new Parameter(this, "X Relative", new BooleanToken(
				false));
		xRelativeParam.setTypeEquals(BaseType.BOOLEAN);

		input.setName("from");
		input.setExpectedMessageContentType(Double.class);

		toPort = PortFactory.getInstance().createInputPort(this, "to",
				Double.class);

		nbStepsPort = PortFactory.getInstance().createInputPort(this,
				"NbSteps", Double.class);

		integrationTimePort = PortFactory.getInstance().createInputPort(this,
				"IntegrationTime", Double.class);

	}

	@Override
	protected void process(final ActorContext ctxt,
			final ProcessRequest request, final ProcessResponse response)
			throws ProcessingException {

		final ManagedMessage fromMessage = request.getMessage(input);

		final double from = (Double) PasserelleUtil.getInputValue(fromMessage);
		// System.out.println("from:" + from);

		final ManagedMessage toMessage = request.getMessage(toPort);
		final double to = (Double) PasserelleUtil.getInputValue(toMessage);
		// System.out.println("to:" + to);

		final ManagedMessage nbStepsmessage = request.getMessage(nbStepsPort);
		final int nbSteps = (int) ((Double) PasserelleUtil
				.getInputValue(nbStepsmessage)).doubleValue();
		// System.out.println("nbSteps:" + nbSteps);

		final ManagedMessage intTimemessage = request
				.getMessage(integrationTimePort);
		final double intTime = (Double) PasserelleUtil
				.getInputValue(intTimemessage);
		// System.out.println("intTime:" + intTime);

		if (!isMockMode()) {
			scanApi.change1DTrajectory(from, to, nbSteps, intTime, xRelative);
			String log;
			if (xRelative) {
				log = "Relative scan";
			} else {
				log = "Absolute scan";
			}
			log += " from " + from + " to " + to + " in " + nbSteps + " steps ";
			ExecutionTracerService.trace(this, log);
		} else {
			ExecutionTracerService.trace(this, "MOCK - Scan from " + from
					+ " to " + to + " in " + nbSteps + " steps");
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
