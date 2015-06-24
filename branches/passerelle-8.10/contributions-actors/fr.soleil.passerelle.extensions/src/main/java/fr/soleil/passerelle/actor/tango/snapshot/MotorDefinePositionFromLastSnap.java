package fr.soleil.passerelle.actor.tango.snapshot;

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
import fr.soleil.passerelle.actor.tango.control.motor.configuration.EncoderType;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfiguration;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class MotorDefinePositionFromLastSnap extends ASnapExtractor {

    public Parameter motorNameParam;
    private String motorName;

    public Parameter contextIDParam;
    private String contextID;

    private TangoCommand definePosition;

    private MotorConfiguration conf;

    public MotorDefinePositionFromLastSnap(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        attributeNameParam.setVisibility(Settable.EXPERT);
        motorNameParam = new StringParameter(this, "Motor Name");
        motorNameParam.setExpression("name");

        contextIDParam = new StringParameter(this, "Context ID");
        contextIDParam.setExpression("1");
    }

    @Override
    protected void doInitialize() throws InitializationException {
        super.doInitialize();
        if (!isMockMode()) {
            try {
                definePosition = new TangoCommand(motorName, "DefinePosition");
                conf = new MotorConfiguration(motorName);
                conf.retrieveConfig();
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }

    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - " + motorName + " define position done");
            sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
        } else {
            try {
                final String snapID = getGetSnapExtractor().getLastSnapID(contextID);
                final String attributeName = motorName + "/position";
                String position;
                if (getExtractionType().equals(ExtractionType.READ)) {
                    final String[] snapValues = getGetSnapExtractor().getReadValues(snapID, attributeName);
                    position = snapValues[0];
                } else {
                    final String[] snapValues = getGetSnapExtractor().getWriteValues(snapID, attributeName);
                    position = snapValues[1];
                }
                try {
                    Double.parseDouble(position);
                } catch (final NumberFormatException nfe) {
                    ExceptionUtil.throwProcessingException("the snapshot does not contains a number", position);
                }
                if (conf.getEncoder().equals(EncoderType.ABSOLUTE)) {
                    ExecutionTracerService
                            .trace(this, motorName + " has an absolute encoder, no define position done ");
                    sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
                } else {
                    definePosition.execute(position);
                    ExecutionTracerService.trace(this, "define position on " + motorName + " with " + position);
                    sendOutputMsg(output, PasserelleUtil.createContentMessage(this, position));
                }

            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == contextIDParam) {
            contextID = PasserelleUtil.getParameterValue(contextIDParam);
        } else if (attribute == motorNameParam) {
            motorName = PasserelleUtil.getParameterValue(motorNameParam);
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected String getExtendedInfo() {
        return null;
    }

}
