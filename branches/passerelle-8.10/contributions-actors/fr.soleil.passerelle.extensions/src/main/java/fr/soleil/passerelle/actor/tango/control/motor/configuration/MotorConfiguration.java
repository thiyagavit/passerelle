package fr.soleil.passerelle.actor.tango.control.motor.configuration;

import static fr.esrf.Tango.DevState.ALARM;
import static fr.esrf.Tango.DevState.FAULT;
import static fr.esrf.Tango.DevState.UNKNOWN;

import org.tango.utils.DevFailedUtils;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

public class MotorConfiguration {

    public static final String NO_CONTROL_BOX_ATTACHED_TO = "No control box attached to ";
    public static final String AXIS_ENCODER_TYPE_PROPERTY = "AxisEncoderType";
    public static final String AXIS_ENCODER_TYPE_PROPERTY_AN_INT = AXIS_ENCODER_TYPE_PROPERTY
            + " does not exist or is not an integer";
    private final String deviceName;
    private final DeviceProxy axisProxy;
    private final String controlBox;
    private EncoderType encoder;
    private InitType initStrategy;
    /**
     * indate if we have to switch the motor in off state after executing InitreferencePosition or
     * DefinePosition
     */
    private boolean switchToOffAfterInit;

    /**
     * Retrieve the controlBox (cb) of one device To test this class, we don't use real cb so the
     * class device is not same than real. So we add a boolean to specify which class name should be
     * be used to find the cb
     * 
     * @param deviceName the motor that we want to find the cb
     * @param isTestEnv flag that indicate if we are un test or production environment
     * 
     * @throws DevFailed if the deviceProxy to the motor can not be created of Devfailed is raised
     */
    public MotorConfiguration(final String deviceName, boolean isTestEnv) throws DevFailed {
        this.deviceName = deviceName;
        axisProxy = ProxyFactory.getInstance().createDeviceProxy(deviceName);
        controlBox = isTestEnv ? "fr.soleil.deviceservers.simulated.SimulatedControlBox"
                : "ControlBox";
        switchToOffAfterInit = false;

    }

    /**
     * Retrieve the controlBox (cb) of one device in production environment. @see
     * MotorConfiguration(final String deviceName, boolean isTestEnv)
     */
    public MotorConfiguration(final String deviceName) throws DevFailed {
        this(deviceName, false);
    }

    public boolean isSwitchToOffAfterInit() {
        return switchToOffAfterInit;
    }

    /**
     * retrieve the controBbox associated to the motor and it characteristics (encoder, init
     * strategy, initPosition)
     * 
     * @throws DevFailed
     */
    public void retrieveFullConfig() throws DevFailed {
        retrieveMyControlBox();
        retrieveConfig();
    }

    public void retrieveConfig() throws DevFailed {
        // TODO add AxisInitPosition (test is Number) ?
        final String[] props = { AXIS_ENCODER_TYPE_PROPERTY, "AxisInitType" };
        final DbDatum[] datum = axisProxy.get_property(props);
        try {
            encoder = EncoderType.getValueFromOrdinal(datum[0].extractLong());
        }
        catch (NumberFormatException e) {
            DevFailedUtils.throwDevFailed(AXIS_ENCODER_TYPE_PROPERTY_AN_INT);
        }
        initStrategy = InitType.getValuefromString(datum[1].extractString());
    }

    public String retrieveMyControlBox() throws DevFailed {
        String controlBoxName = null;
        final DeviceData dd = axisProxy.get_adm_dev().command_inout("QueryDevice");
        final String[] devices = dd.extractStringArray();
        for (final String device : devices) {
            final String[] classAndDevice = device.split("::");
            if (classAndDevice[0].equals(controlBox)) {
                controlBoxName = classAndDevice[1];
                break;
            }
        }
        if (controlBoxName == null) { // TODO should getMessage() = NO_CONTROL_BOX_ATTACHED_TO ????
            Except.throw_exception("TANGO_ERROR", NO_CONTROL_BOX_ATTACHED_TO + deviceName,
                    "MotorConfiguration.retrieveMyControlBox");
        }
        return controlBoxName;
    }

    public void initMotor(final Actor actor) throws DevFailed, ProcessingException {

        // 1- chech if devices (Control and GalilAxis) need an Init
        // command

        // searching for the related Controlbox device
        final String cbName = this.retrieveMyControlBox();
        final DeviceProxy controlBox = ProxyFactory.getInstance().createDeviceProxy(cbName);

        // Bug 22954
        final TangoCommand cmdStateOnCb = new TangoCommand(cbName, "State");
        DevState currentStateOnCb = TangoAccess.getCurrentState(cmdStateOnCb);

        if (currentStateOnCb.equals(FAULT) || currentStateOnCb.equals(UNKNOWN)) {
            ExecutionTracerService.trace(actor, "Init command executed on " + cbName);
            controlBox.command_inout("Init");
            // after init, does not switch immediatly to
            // correct state so wait a little
            try {
                Thread.sleep(200);
            }
            catch (final InterruptedException e) {
                // ignore
            }

            currentStateOnCb = TangoAccess.getCurrentState(cmdStateOnCb);
            if (currentStateOnCb.equals(FAULT) || currentStateOnCb.equals(UNKNOWN)) {
                ExceptionUtil.throwProcessingException( "error while after command init on control box, device is still in error");
            }
        }

        // if control box in ALARM -> microcode has been stop
        currentStateOnCb = TangoAccess.getCurrentState(cmdStateOnCb);
        if (currentStateOnCb.equals(ALARM)) {
            ExecutionTracerService.trace(actor, "StartMicrocode command executed on " + cbName);
            controlBox.command_inout("StartMicrocode");
        }

        // now initialize the galil axis
        final TangoCommand cmdStateOnAxis = new TangoCommand(axisProxy.get_name(), "State");
        DevState currentStateOnAxis = TangoAccess.getCurrentState(cmdStateOnAxis);
        if (currentStateOnAxis.equals(FAULT) || currentStateOnAxis.equals(UNKNOWN)) {

            ExecutionTracerService.trace(actor, "Init command executed on " + deviceName);
            axisProxy.command_inout("Init");

            // after init, does not switch immediatly to correct state
            // so wait a little
            try {
                Thread.sleep(200);
            }
            catch (final InterruptedException e) {
                // ignore
            }
        }
        currentStateOnAxis = TangoAccess.getCurrentState(cmdStateOnAxis);
        if (currentStateOnAxis.equals(FAULT) || currentStateOnAxis.equals(UNKNOWN)) {
            ExceptionUtil.throwProcessingException(  "error while after command init on axis, device is still in error");
        }

        // 2- chech if axis needs an On command
        currentStateOnAxis = TangoAccess.getCurrentState(cmdStateOnAxis);
        if (currentStateOnAxis.equals(DevState.OFF)) {
            axisProxy.command_inout("MotorON");
            ExecutionTracerService.trace(actor, deviceName + " was Off, Switched On");
        }
    }

    public EncoderType getEncoder() {
        return encoder;
    }

    public InitType getInitStrategy() {
        return initStrategy;
    }
}
