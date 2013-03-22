package fr.soleil.passerelle.actor.tango.control.motor;

import java.util.ArrayList;
import java.util.List;

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
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.control.motor.dataProviders.AttributeDataProvider;
import fr.soleil.passerelle.util.DevFailedInitializationException;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.DevFailedValidationException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * A basic class for Actor which deal with motor device (ie GalilAxis). This class check in
 * ValidateInitialization if the device is a Motor. To do it, the actor need to know the motor class
 * name. To be available to test this actor, we need to use a simulated Motor. So motor class name
 * change according to the environment (ie Production or Test). So to choose the motor class name, i
 * add a boolean expert parameter named "Simulated motor".
 */
public abstract class AGalilMotorActor extends MotorMoverV5 {

    public static final String MOTOR_NAME_LABEL = "Motor name";
    public static final String SIMULATED_MOTOR_LABEL = "Simulated Motor";
    public static final String SIMULATED_MOTOR_CLASS = "fr.soleil.deviceservers.simulated.motor.SimulatedMotor";
    public static final String REAL_MOTOR_CLASS = "GalilAxis";
    protected static List<String> attributeList = new ArrayList<String>();

    static {
        attributeList.add("position");
    }

    @ParameterName(name = SIMULATED_MOTOR_LABEL)
    public Parameter simulatedMotorParam;
    private boolean simulatedMotor;
    private AttributeDataProvider offsetDataProvider;

    public AGalilMotorActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name, attributeList);
        mouvementTypeParam.setVisibility(Settable.EXPERT);
        mouvementTypeParam.addChoice("position");
        mouvementTypeParam.setExpression("position");

        simulatedMotorParam = new ExpertParameter(this, SIMULATED_MOTOR_LABEL);
        simulatedMotorParam.setToken(new BooleanToken(false));
        simulatedMotorParam.setTypeEquals(BaseType.BOOLEAN);

        deviceNameParam.setDisplayName(MOTOR_NAME_LABEL);
    }

    public abstract AttributeDataProvider createOffsetDataProvider();

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
        super.doInitialize();
        try {
            offsetDataProvider = createOffsetDataProvider();
            offsetDataProvider.setDeviceName(getDeviceName());
            offsetDataProvider.init(this);
        }
        catch (DevFailed devFailed) {
            throw new DevFailedInitializationException(devFailed, this);
        }
    }

    @Override
    protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {
        // TODO manage mock mode

        try {
            if (offsetDataProvider.attributeIsAvailable()) {

                String offset = offsetDataProvider.getData(this, request, input);
                if (offset != null) {
                    TangoAttribute offsetWriter = new TangoAttribute(getDeviceName() + "/offset");

                    offsetWriter.write(offset);
                    ExecutionTracerService.trace(this, "offset has been set to " + offset);
                }
            }

            super.process(ctxt, request, response);
        }
        catch (DevFailed devFailed) {
            throw new DevFailedProcessingException(devFailed, this);
        }
    }
}
