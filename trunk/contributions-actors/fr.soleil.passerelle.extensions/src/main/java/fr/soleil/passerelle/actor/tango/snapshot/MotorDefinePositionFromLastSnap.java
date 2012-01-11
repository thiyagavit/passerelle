package fr.soleil.passerelle.actor.tango.snapshot;

import java.util.List;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.control.motor.MotorConfiguration;
import fr.soleil.passerelle.actor.tango.control.motor.MotorConfiguration.EncoderType;
import fr.soleil.passerelle.util.DevFailedInitializationException;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class MotorDefinePositionFromLastSnap extends ASnapExtractor {

    public Parameter motorNameParam;
    private String motorName;

    public Parameter snapExtractionTypeParam;
    private String snapExtractionType;

    private boolean getReadPart;

    public Parameter contextIDParam;
    private String contextID;

    private TangoCommand definePosition;
    // private TangoCommand getSnapValue;
    private TangoCommand getSnapID;

    private MotorConfiguration conf;

    public MotorDefinePositionFromLastSnap(final CompositeEntity container,
	    final String name) throws NameDuplicationException,
	    IllegalActionException {
	super(container, name);

	attributeNameParam.setVisibility(Settable.EXPERT);
	motorNameParam = new StringParameter(this, "Motor Name");
	motorNameParam.setExpression("name");

	snapExtractionTypeParam = new StringParameter(this, "Extraction type");
	snapExtractionTypeParam.addChoice(ExtractionType.READ.toString());
	snapExtractionTypeParam.addChoice(ExtractionType.WRITE.toString());
	snapExtractionTypeParam.setExpression(ExtractionType.READ.toString());

	contextIDParam = new StringParameter(this, "Context ID");
	contextIDParam.setExpression("1");
    }

    @Override
    protected void doInitialize() throws InitializationException {
	super.doInitialize();
	if (!isMockMode()) {
	    try {
		definePosition = new TangoCommand(motorName, "DefinePosition");
		getSnapID = new TangoCommand(getSnapExtractorName(),
			"GetSnapID");
		conf = new MotorConfiguration(motorName);
		conf.retrieveConfig();
	    } catch (final DevFailed e) {
		throw new DevFailedInitializationException(e, this);
	    }
	}

    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
	if (isMockMode()) {
	    ExecutionTracerService.trace(this, "MOCK - " + motorName
		    + " define position done");
	    sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
	}
	try {
	    // Syntax for GetSnapID:
	    // ctx_id, "id_snap > | < | = | <= | >= nbr",
	    // "time < | > | >= | <=  yyyy-mm-dd hh:mm:ss | dd-mm-yyyy hh:mm:ss",
	    // "comment starts | ends | contains string",
	    // first | last
	    final String snapID = getSnapID.execute(String.class, contextID,
		    "last");
	    final String attributeName = motorName + "/position";
	    final List<String> snapValues = getSnapValue.executeExtractList(
		    String.class, snapID, attributeName);
	    String position;
	    if (getReadPart) {
		position = snapValues.get(0);
	    } else {
		position = snapValues.get(1);
	    }
	    try {
		Double.parseDouble(position);
	    } catch (final NumberFormatException nfe) {
		throw new ProcessingException(
			"the snapshot does not contains a value", position,
			null);
	    }
	    if (conf.getEncoder().equals(EncoderType.ABSOLUTE)) {
		ExecutionTracerService.trace(this, motorName
			+ " has an absolute encoder, no define position done ");
		sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
	    } else {
		definePosition.execute(position);
		ExecutionTracerService.trace(this, "define position on "
			+ motorName + " with " + position);
		sendOutputMsg(output, PasserelleUtil.createContentMessage(this,
			position));
	    }

	} catch (final DevFailed e) {
	    throw new DevFailedProcessingException(e, this);
	}
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute attribute)
	    throws IllegalActionException {
	if (attribute == contextIDParam) {
	    contextID = PasserelleUtil.getParameterValue(contextIDParam);
	} else if (attribute == motorNameParam) {
	    motorName = PasserelleUtil.getParameterValue(motorNameParam);
	} else if (attribute == snapExtractionTypeParam) {
	    snapExtractionType = PasserelleUtil
		    .getParameterValue(snapExtractionTypeParam);
	    if (snapExtractionType.compareTo(ExtractionType.READ.toString()) == 0) {
		getReadPart = true;
	    } else {
		getReadPart = false;
	    }
	} else {
	    super.attributeChanged(attribute);
	}
    }

    @Override
    protected String getExtendedInfo() {
	// TODO Auto-generated method stub
	return null;
    }

}
