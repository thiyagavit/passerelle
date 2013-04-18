package fr.soleil.passerelle.actor.tango.control;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.ExpertParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.tango.control.motor.MotorMoverV5;
import fr.soleil.passerelle.actor.tango.control.motor.actions.IMoveAction;
import fr.soleil.passerelle.actor.tango.control.motor.actions.MoveNumericAttribute;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.DevFailedValidationException;
import fr.soleil.passerelle.util.PasserelleUtil;

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
    public static final String SIMULATED_MOTOR_LABEL = "Simulated Motor";
    public static final String SIMULATED_MOTOR_CLASS = "fr.soleil.deviceservers.simulated.motor.SimulatedMotor";
    public static final String REAL_MOTOR_CLASS = "GalilAxis";
    protected static List<String> attributeList = new ArrayList<String>();

    @ParameterName(name = SIMULATED_MOTOR_LABEL)
    public Parameter simulatedMotorParam;
    private boolean simulatedMotor;

    static {
        attributeList.add("position");
    }

    public GalilAxisV5(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name, attributeList);

        final URL url = this.getClass().getResource("/image/MOT.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:cyan;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\"" + url
                + "\"/>\n" + "</svg>\n");

        mouvementTypeParam.setVisibility(Settable.EXPERT);
        mouvementTypeParam.addChoice("position");
        mouvementTypeParam.setExpression("position");

        simulatedMotorParam = new ExpertParameter(this, SIMULATED_MOTOR_LABEL);
        simulatedMotorParam.setToken(new BooleanToken(false));
        simulatedMotorParam.setTypeEquals(BaseType.BOOLEAN);

        deviceNameParam.setDisplayName(MOTOR_NAME_LABEL);
    }

    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == simulatedMotorParam) {
            simulatedMotor = PasserelleUtil.getParameterBooleanValue(simulatedMotorParam);
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected void validateInitialization() throws ValidationException {
        super.validateInitialization();

        try {
            String motorClass = simulatedMotor ? SIMULATED_MOTOR_CLASS : REAL_MOTOR_CLASS;
            if (!getDeviceProxy().get_class().equals(motorClass)) {
                throw new ValidationException(ErrorCode.FLOW_VALIDATION_ERROR, "The device: \""
                        + getDeviceName() + "\" is not a " + motorClass, this, null);
            }
        }
        catch (DevFailed devFailed) {
            throw new DevFailedValidationException(devFailed, this);
        }
        catch (PasserelleException e) {
            ExecutionTracerService.trace(this, e);
            throw new ValidationException(e.getErrorCode(), e.getMessage(), this, e);
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
            }
            catch (final DevFailed e) {
                TangoToPasserelleUtil.getDevFailedString(e, this);
            }
            catch (final Exception e) {
                // TODO change to log
                e.printStackTrace();
            }
        }
    }
}
