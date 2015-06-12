package fr.soleil.passerelle.actor.tango.control.motor.configuration;

import static fr.esrf.Tango.DevState.ALARM;
import static fr.esrf.Tango.DevState.DISABLE;
import static fr.esrf.Tango.DevState.FAULT;
import static fr.esrf.Tango.DevState.MOVING;
import static fr.esrf.Tango.DevState.OFF;
import static fr.esrf.Tango.DevState.UNKNOWN;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.EncoderType.ABSOLUTE;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.InitType.DP;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.InitType.OTHER;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.Command.executeCmdAccordingState;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Except;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.ErrorCommand;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.InitCommand;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.MicroCodeCommand;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.OnCommand;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.tango.clientapi.TangoCommand;

//TODO CHANGE TO THOWS MotorConfigurationException anywhere
public class MotorConfigurationV2 {

    // Errors messages
    public static final String NO_CONTROL_BOX_ATTACHED_TO = "No control box attached to ";
    public static final String DEFINE_POS_CANT_BE_APPLY_WITH_OTHER_STRATEGIE = "  has an initialization strategy, must use ReferenceInitPosition";
    public static final String INIT_REF_CANT_BE_APPLY_WITH_DP_STATEGIE = "  has no initialization strategy, must use DefinePosition";
    public static final String INIT_NOT_POSSIBLE_WITH_ABSOLUTE_ENCODER = " has an absolute encoder, no need to initialize";
    
  
    
    private final String deviceName;
    private EncoderType encoder;
    private InitType initStrategy;
    /**
     * indicate if we have to switch the motor in off state after executing InitReferencePosition or
     * DefinePosition
     */
    private boolean switchToOffAfterInit;
    private String controlBoxName;

    /**
     * Retrieve the controlBox (cb) of one device To test this class, we don't use real cb so the
     * class device is not same than real. So we add a boolean to specify which class name should be
     * be used to find the cb
     * 
     * @param proxy the proxy of the motor.
     * @param deviceName the motor name that we want to find the cb
     * @param useSimulatedMotor flag that indicate if we are in test or production environment
     * 
     * @throws fr.esrf.Tango.DevFailed if the deviceProxy to the motor can not be created of
     *             Devfailed is raised
     */
    public MotorConfigurationV2(final String deviceName) throws DevFailed {
        this.deviceName = deviceName;
        switchToOffAfterInit = false;

    }

    public boolean isSwitchToOffAfterInit() {
        return switchToOffAfterInit;
    }

    /**
     * retrieve the controlbox associated to the motor and it characteristics (encoder, init
     * strategy, initPosition)
     * 
     * @throws DevFailed
     */
    public void retrieveFullConfig() throws DevFailed {
        retrieveMyControlBox();
        retrieveProperties();
    }

    /**
     * retrieve the motor characteristics (encoder, init strategy, initPosition)
     * 
     * @throws DevFailed
     */
    public void retrieveProperties() throws DevFailed {

        encoder =  MotorManager.getEncoderType(deviceName) ;
     
        initStrategy = MotorManager.getInitType( deviceName);
      
    }
  

    /**
     * retrieve the controlBox associated to the device
     * 
     * @throws DevFailed
     */
    public void retrieveMyControlBox() throws DevFailed {
        String cbName = MotorManager.getControlBoxForMotor(deviceName);
        if (cbName == null) {
            Except.throw_exception("TANGO_ERROR", NO_CONTROL_BOX_ATTACHED_TO + deviceName,
                    "MotorConfiguration.retrieveMyControlBox");
        }
        controlBoxName = cbName;
    }

    public void initDevice(final Actor actor) throws ProcessingException {

        try {
            // 1 - Init the controlBox
            TangoCommand stateCmd = new TangoCommand(controlBoxName, "State");

            executeCmdAccordingState(new InitCommand(actor, controlBoxName, stateCmd), FAULT, UNKNOWN);
            executeCmdAccordingState(new MicroCodeCommand(actor, controlBoxName, stateCmd), ALARM);

            // 2- Init the galil
            stateCmd = new TangoCommand(deviceName, "State");

            executeCmdAccordingState(new InitCommand(actor, deviceName, stateCmd), FAULT, UNKNOWN);
            executeCmdAccordingState(new ErrorCommand(actor, deviceName, stateCmd), MOVING, DISABLE);
            switchToOffAfterInit = executeCmdAccordingState(new OnCommand(actor, deviceName, stateCmd), OFF);
            
            if(switchToOffAfterInit){
                ExecutionTracerService.trace(actor, "Call " +  deviceName + "/" + MotorManager.MOTOR_ON );
            }

            // checks galil is in expected state
            DevState galilState = stateCmd.execute(DevState.class);
            if (galilState == FAULT || galilState == UNKNOWN) {
                ExceptionUtil.throwProcessingExceptionWithLog(actor, deviceName + " is down", this);
            }

        } catch (DevFailed e) {
            ExceptionUtil.throwProcessingException(ErrorCode.FATAL,actor,e);
        }
    }

    public void assertDefinePositionCanBeApplyOnMotor() throws MotorConfigurationException {
        assertEncoderIsValidForInitalization();

        if (initStrategy != DP) {
            throw new MotorConfigurationException(deviceName + DEFINE_POS_CANT_BE_APPLY_WITH_OTHER_STRATEGIE);
        }
    }

    public void assertInitRefPosBeApplyOnMotor() throws MotorConfigurationException {
        assertEncoderIsValidForInitalization();
        if (initStrategy != OTHER) {
            throw new MotorConfigurationException(deviceName + INIT_REF_CANT_BE_APPLY_WITH_DP_STATEGIE);
        }
    }

    public void assertEncoderIsValidForInitalization() throws MotorConfigurationException {
        if (encoder == ABSOLUTE) {
            throw new MotorConfigurationException(deviceName + INIT_NOT_POSSIBLE_WITH_ABSOLUTE_ENCODER);
        }
    }

    public EncoderType getEncoder() {
        return encoder;
    }

    public InitType getInitStrategy() {
        return initStrategy;
    }

    public String getControlBoxName() {
        return controlBoxName;
    }
}
