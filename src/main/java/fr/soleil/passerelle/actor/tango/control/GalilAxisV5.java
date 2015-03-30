package fr.soleil.passerelle.actor.tango.control;

import static fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.Command.executeCmdAccordingState;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.tango.control.motor.MotorMoverV5;
import fr.soleil.passerelle.actor.tango.control.motor.actions.IMoveAction;
import fr.soleil.passerelle.actor.tango.control.motor.actions.MoveNumericAttribute;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorManager;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.OffCommand;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.OnCommand;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

/**
 * Move a motor and output the reached position. This class check in ValidateInitialization if the
 * device is a Motor. To do it, the actor need to know the motor class name. To be available to test
 * this actor, we need to use a simulated Motor. So motor class name change according to the
 * environment (ie Production or Test). So to choose the motor class name, i add a boolean expert
 * parameter named "Simulated motor".
 * 
 */
@SuppressWarnings("serial")
public class GalilAxisV5 extends MotorMoverV5 implements IActorFinalizer {

    public static final String MOTOR_NAME_LABEL = "Motor name";
    public static final String DEFAULT_ACTORNAME = "MoveMotorV5.";
    protected static List<String> attributeList = new ArrayList<String>();

    @ParameterName(name = MotorManager.ON_IF_NEEDED)
    public Parameter turnOnParam;
    private boolean turnOn = false;

    public Port offsetPort;

    static {
        attributeList.add(MotorManager.POSITION);
    }

    public GalilAxisV5(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name, attributeList);

        final URL url = this.getClass().getResource("/image/MOT.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:cyan;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\"" + url + "\"/>\n" + "</svg>\n");

        offsetPort = PortFactory.getInstance().createInputPort(this, MotorManager.OFFSET, null);

        mouvementTypeParam.setVisibility(Settable.EXPERT);
        mouvementTypeParam.addChoice(MotorManager.POSITION);
        mouvementTypeParam.setExpression(MotorManager.POSITION);

        deviceNameParam.setDisplayName(MOTOR_NAME_LABEL);

        turnOnParam = new Parameter(this, MotorManager.ON_IF_NEEDED, new BooleanToken(turnOn));
        turnOnParam.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == turnOnParam) {
            turnOn = PasserelleUtil.getParameterBooleanValue(turnOnParam);
        }
        super.attributeChanged(attribute);
    }

    @Override
    protected void validateInitialization() throws ValidationException {
        super.validateInitialization();

        if (!MotorManager.isMotorClass(getDeviceName())) {
            throw new ValidationException(ErrorCode.FLOW_VALIDATION_ERROR, "The device: \"" + getDeviceName()
                    + "\" is not supported, expected " + MotorManager.getMotorClasses(), this, null);
        }
    }

    @Override
    protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        boolean switchToOffAfterInit = false;
        TangoCommand stateCmd = null;
        if (turnOn) {
            try {
                stateCmd = new TangoCommand(getDeviceName(), "State");
                // Turn On the motor if it is OFF before
                switchToOffAfterInit = executeCmdAccordingState(new OnCommand(this, getDeviceName(), stateCmd),
                        DevState.OFF);
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }

        // apply offset if

        String offset = "";

        ManagedMessage offsetManagedMsg = request.getMessage(offsetPort);

        // if port is Connected
        if (offsetManagedMsg != null) {
            offset = ((String) PasserelleUtil.getInputValue(offsetManagedMsg)).trim();
            // message is not empty
            if (!offset.isEmpty()) {

                // set the offset
                try {
                    getDeviceProxy().write_attribute(
                            new DeviceAttribute(MotorManager.OFFSET, Double.parseDouble(offset)));

                    ExecutionTracerService.trace(this, "apply offset " + offset);
                } catch (NumberFormatException e) {
                    ExceptionUtil.throwProcessingExceptionWithLog(this, ErrorCode.FATAL,
                            "Error: offset is not a number", ctxt);
                } catch (DevFailed e) {
                    ExceptionUtil.throwProcessingException(this, e);
                } catch (PasserelleException e) {
                    ExceptionUtil.throwProcessingExceptionWithLog(this, ErrorCode.FATAL, e.getMessage(), ctxt, e);
                }
            }
        }
        // call super class to execute movement
        super.process(ctxt, request, response);

        // if the motor was off before the init, we switch it to off again
        if (switchToOffAfterInit) {
            try {
                executeCmdAccordingState(new OffCommand(this, getDeviceName(), stateCmd), DevState.ON,
                        DevState.STANDBY, DevState.ALARM);
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }

    }

    @Override
    protected void doInitialize() throws InitializationException {
        final Director dir = getDirector();
        if (dir instanceof BasicDirector) {
            ((BasicDirector) dir).registerFinalizer(this);
        }
        super.doInitialize();
    }

    @Override
    public IMoveAction createMoveAction() {
        return new MoveNumericAttribute();
    }

    @Override
    public void doFinalAction() {
        if (!isMockMode()) {
            try {
                // bug 22954
                if (TangoAccess.executeCmdAccordingState(getDeviceName(), DevState.MOVING, "Stop")) {
                    ExecutionTracerService.trace(this, "motor has been stop");
                }
            } catch (final DevFailed e) {
                TangoToPasserelleUtil.getDevFailedString(e, this);
            } catch (final Exception e) {
                ExecutionTracerService.trace(this, e.getMessage());
            }
        }
    }
}
