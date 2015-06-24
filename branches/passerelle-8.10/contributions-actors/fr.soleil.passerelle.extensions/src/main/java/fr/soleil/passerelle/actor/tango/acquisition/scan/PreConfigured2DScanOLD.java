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
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.soleil.passerelle.actor.tango.acquisition.ScanOLD;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class PreConfigured2DScanOLD extends ScanOLD {

    private final static Logger logger = LoggerFactory.getLogger(PreConfigured2DScanOLD.class);
    public Port xToPort;
    public Port xNbStepsPort;
    public Port yFromPort;
    public Port yToPort;
    public Port yNbStepsPort;
    public Port integrationTimePort;

    public Parameter xRelativeParam;
    boolean xRelative;

    public Parameter yRelativeParam;
    boolean yRelative;

    public PreConfigured2DScanOLD(final CompositeEntity container, final String name)
	    throws IllegalActionException, NameDuplicationException {
	super(container, name);

	xRelativeParam = new Parameter(this, "X Relative", new BooleanToken(false));
	xRelativeParam.setTypeEquals(BaseType.BOOLEAN);

	yRelativeParam = new Parameter(this, "Y Relative", new BooleanToken(false));
	yRelativeParam.setTypeEquals(BaseType.BOOLEAN);

	input.setName("XFrom");
	input.setExpectedMessageContentType(Double.class);

	xToPort = PortFactory.getInstance().createInputPort(this, "XTo", Double.class);
	xNbStepsPort = PortFactory.getInstance().createInputPort(this, "XNbSteps", Double.class);

	yFromPort = PortFactory.getInstance().createInputPort(this, "YFrom", Double.class);

	yToPort = PortFactory.getInstance().createInputPort(this, "YTo", Double.class);

	yNbStepsPort = PortFactory.getInstance().createInputPort(this, "YNbSteps", Double.class);

	integrationTimePort = PortFactory.getInstance().createInputPort(this, "IntegrationTime",
		Double.class);
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request,
	    final ProcessResponse response) throws ProcessingException {

	// X
	final ManagedMessage fromXMessage = request.getMessage(input);
	final double fromX = (Double) PasserelleUtil.getInputValue(fromXMessage);
	logger.debug("fromX:" + fromX);

	final ManagedMessage toXMessage = request.getMessage(xToPort);
	final double toX = (Double) PasserelleUtil.getInputValue(toXMessage);
	logger.debug("toX:" + toX);

	final ManagedMessage nbStepsmessageX = request.getMessage(xNbStepsPort);
	final int nbStepsX = (int) ((Double) PasserelleUtil.getInputValue(nbStepsmessageX))
		.doubleValue();
	logger.debug("nbStepsX:" + nbStepsX);

	final ManagedMessage fromYMessage = request.getMessage(yFromPort);
	final double fromY = (Double) PasserelleUtil.getInputValue(fromYMessage);
	logger.debug("fromY:" + fromY);

	// Y
	final ManagedMessage toYMessage = request.getMessage(yToPort);
	final double toY = (Double) PasserelleUtil.getInputValue(toYMessage);
	logger.debug("toY:" + toY);

	final ManagedMessage nbStepsmessageY = request.getMessage(yNbStepsPort);
	final int nbStepsY = (int) ((Double) PasserelleUtil.getInputValue(nbStepsmessageY))
		.doubleValue();
	logger.debug("nbStepsY:" + nbStepsY);

	final ManagedMessage intTimemessage = request.getMessage(integrationTimePort);
	final double intTime = (Double) PasserelleUtil.getInputValue(intTimemessage);
	logger.debug("intTime:" + intTime);

	if (!isMockMode()) {
	    scanApi.change2DTrajectory(fromX, toX, nbStepsX, xRelative, fromY, toY, nbStepsY,
		    yRelative, intTime);

	    String log;
	    log = "Scan [dim X ";
	    if (xRelative) {
		log += "relative: ";
	    } else {
		log += "absolute: ";
	    }
	    log += "from " + fromX + " to " + toX + " in " + nbStepsX + " steps] [dim Y ";
	    if (yRelative) {
		log += "relative: ";
	    } else {
		log += "absolute: ";
	    }
	    log += "from " + fromY + " to " + toY + " in " + nbStepsY + " steps] + integration: "
		    + intTime;
	    ExecutionTracerService.trace(this, log);

	} else {
	    ExecutionTracerService.trace(this, "MOCK - Scan dim final X from " + fromX + " to "
		    + toX + " in " + nbStepsX + " steps and dim Y from " + fromY + " to " + toY
		    + " in " + nbStepsY + " steps");
	}
	super.process(ctxt, request, response);
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
	if (arg0 == xRelativeParam) {
	    xRelative = PasserelleUtil.getParameterBooleanValue(xRelativeParam);
	} else if (arg0 == yRelativeParam) {
	    yRelative = PasserelleUtil.getParameterBooleanValue(yRelativeParam);
	} else {
	    super.attributeChanged(arg0);
	}
    }

}
