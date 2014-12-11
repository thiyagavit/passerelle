package fr.soleil.passerelle.actor.tango.control.motor.configuration;

import static fr.esrf.Tango.DevState.ALARM;
import static fr.esrf.Tango.DevState.DISABLE;
import static fr.esrf.Tango.DevState.FAULT;
import static fr.esrf.Tango.DevState.MOVING;
import static fr.esrf.Tango.DevState.OFF;
import static fr.esrf.Tango.DevState.ON;
import static fr.esrf.Tango.DevState.STANDBY;
import static fr.esrf.Tango.DevState.UNKNOWN;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.EncoderType.ABSOLUTE;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.InitType.DP;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.InitType.OTHER;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.Command.executeCmdAccordingState;

import org.tango.utils.DevFailedUtils;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;


import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.soleil.comete.tango.data.service.helper.TangoDeviceHelper;

import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.ErrorCommand;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.InitCommand;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.MicroCodeCommand;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.OnCommand;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

//TODO CHANGE TO THOWS MotorConfigurationException anywhere
public class MotorConfigurationV2 {

    public static final String AXIS_ENCODER_TYPE_PROPERTY = "AxisEncoderType";
    public static final String AXIS_INIT_TYPE_PROPERTY = "AxisInitType";
    public static final String AXIS_INIT_POSITION_PROPERTY = "AxisInitPosition";

    // Errors messages
    public static final String DEFINE_POS_CANT_BE_APPLY_WITH_OTHER_STRATEGIE = "  has an initialization strategy, must use ReferenceInitPosition";
    public static final String INIT_REF_CANT_BE_APPLY_WITH_DP_STATEGIE = "  has no initialization strategy, must use DefinePosition";
    public static final String INIT_NOT_POSSIBLE_WITH_ABSOLUTE_ENCODER = " has an absolute encoder, no need to initialize";
    public static final String NO_CONTROL_BOX_ATTACHED_TO = "No control box attached to ";
    public static final String AXIS_ENCODER_TYPE_PROPERTY_IS_NOT_INT = AXIS_ENCODER_TYPE_PROPERTY
            + " does not exist or is not an integer";
    public static final String AXIS_INIT_POSITION_PROPERTY_IS_NAN = AXIS_INIT_POSITION_PROPERTY
            + " does not exist or is not a number";

    private final String deviceName;
    private final DeviceProxy axisProxy;
    private final String controlBoxDeviceClass;
    private EncoderType encoder;
    private double axisInitPosition = Double.NaN;
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
    public MotorConfigurationV2(DeviceProxy proxy, final String deviceName, boolean useSimulatedMotor) throws DevFailed {
        this.deviceName = deviceName;

        if (proxy == null) {
            proxy = ProxyFactory.getInstance().createDeviceProxy(deviceName);
        }
        axisProxy = proxy;
        controlBoxDeviceClass = useSimulatedMotor ? "SimulatedControlBox" : "ControlBox";
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

    private String getDeviceProperty(String deviceName, String propertyName) {
        String value = null;
        Database database = TangoDeviceHelper.getDatabase();
        if (database != null && !isNullOrEmpty(deviceName) && !isNullOrEmpty(propertyName)) {
            try {
                DbDatum dbDatum = database.get_device_property(deviceName, propertyName);
                if (dbDatum != null && !dbDatum.is_empty()) {
                    value = dbDatum.extractString();
                }
            } catch (DevFailed e) {
                DevFailedUtils.printDevFailed(e);
            }
        }
        return value;
    }

    private boolean isNullOrEmpty(String stringValue) {
        return (stringValue == null || stringValue.isEmpty());
    }

    /**
     * retrieve the motor characteristics (encoder, init strategy, initPosition)
     * 
     * @throws DevFailed
     */
    public void retrieveProperties() throws DevFailed {

        String encoderType = getDeviceProperty(deviceName, AXIS_ENCODER_TYPE_PROPERTY);
        if (!isNullOrEmpty(encoderType)) {
            int encoderInt = 0;

            try {
                encoderInt = Integer.parseInt(encoderType);
            } catch (NumberFormatException e) {
                DevFailedUtils.throwDevFailed(AXIS_ENCODER_TYPE_PROPERTY_IS_NOT_INT + " for device " + deviceName);
            }

            encoder = EncoderType.getValueFromOrdinal(encoderInt);
        }

        String initStrategyString = getDeviceProperty(deviceName, AXIS_INIT_TYPE_PROPERTY);
        if (!isNullOrEmpty(initStrategyString)) {
            initStrategy = InitType.getValuefromString(initStrategyString);
        }

        if (initStrategy == InitType.OTHER) {
            // initializeReference command is available. to the command works the
            // AxisInitPosition property must be a number.
            String initPosition = getDeviceProperty(deviceName, getDeviceProperty(deviceName, AXIS_INIT_TYPE_PROPERTY));
            if (!isNullOrEmpty(initPosition)) {
                try {
                    axisInitPosition = Double.parseDouble(initPosition);
                } catch (NumberFormatException e) {
                    DevFailedUtils.throwDevFailed(AXIS_INIT_POSITION_PROPERTY_IS_NAN);
                }
            }
        }
    }

    /**
     * retrieve the controlBox associated to the device
     * 
     * @throws DevFailed
     */
    public void retrieveMyControlBox() throws DevFailed {
        String cbName = null;
        final DeviceData dd = axisProxy.get_adm_dev().command_inout("QueryDevice");
        final String[] devices = dd.extractStringArray();
        for (final String device : devices) {
            final String[] classAndDevice = device.split("::");
            if (classAndDevice[0].equals(controlBoxDeviceClass)) {
                cbName = classAndDevice[1];
                break;
            }
        }
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

            // checks galil is in expected state
            DevState galilState = stateCmd.execute(DevState.class);
            if (galilState != STANDBY && galilState != ON) {
                throw new ProcessingExceptionWithLog(actor, "Motor is " + galilState.toString()
                        + " insteadof  StandBy or On", this, null);
            }

        } catch (DevFailed e) {
            throw new DevFailedProcessingException(e, PasserelleException.Severity.FATAL, actor);
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
