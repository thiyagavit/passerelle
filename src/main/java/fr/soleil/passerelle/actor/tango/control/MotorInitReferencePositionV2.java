package fr.soleil.passerelle.actor.tango.control;

import static fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.Command.executeCmdAccordingState;

import java.net.URL;

import ptolemy.actor.Director;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

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
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationException;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorManager;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.OffCommand;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

/**
 * this actor initialize the devices (cb and Galil) according to shouldInitDevice parameter and run
 * an InitializeReferencePosition on the motor specified by DeviceName parameter. To be able to
 * initialize a motor, it must be in On or StandBy state. So if the motor is OFF before the
 * beginning of the initialization we switch it to On then we initialize it and to finish we switch
 * it to Off again.
 * 
 * If the deviceName is Empty then an IllegalActionException or a ValidateException is thrown.
 * 
 * if the device has not the following commands: InitializeReferencePosition and MotorOn then an
 * IllegalActionException or a ValidateException is thrown
 * 
 * If an error occurred during the initialization of the motor then an ProcessingException is thrown
 * 
 */
public class MotorInitReferencePositionV2 extends ATangoDeviceActorV5 implements IActorFinalizer {

    private static final long serialVersionUID = 6385413377074403283L;

    public static final String DEFAULT_ACTORNAME = "MotorInitReferencePositionV2.";

    private MotorConfigurationV2 conf;
    private WaitStateTask waitTask;

    /**
     * flag that indicate whether the actor must initialize the devices (Cb an Galil) prior to
     * execute InitializeReferencePosition
     */
    @ParameterName(name = MotorManager.INIT_DEVICES)
    public Parameter shouldInitDevicesParam;
    private boolean shouldInitDevice = false;

    public MotorInitReferencePositionV2(CompositeEntity container, String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        final URL url = this.getClass().getResource(
                "/org/tango-project/tango-icon-theme/32x32/categories/applications-system.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:cyan;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\"" + url + "\"/>\n" + "</svg>\n");

        shouldInitDevicesParam = new Parameter(this, MotorManager.INIT_DEVICES, new BooleanToken(shouldInitDevice));
        shouldInitDevicesParam.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == shouldInitDevicesParam) {
            shouldInitDevice = PasserelleUtil.getParameterBooleanValue(shouldInitDevicesParam);
        }
        super.attributeChanged(attribute);
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
    protected void validateInitialization() throws ValidationException {
        super.validateInitialization();

        try {
            // test if commands exists, otherwise this device is not a motor
            final DeviceProxy dev = getDeviceProxy();
            dev.command_query(MotorManager.MOTOR_ON);
            dev.command_query(MotorManager.INITIALIZE_REFERENCE_POSITION);

            conf = new MotorConfigurationV2(getDeviceName());
            conf.retrieveFullConfig();
            conf.assertInitRefPosBeApplyOnMotor();
        } catch (DevFailed devFailed) {
            ExceptionUtil.throwValidationException(this, devFailed);
        } catch (PasserelleException e) {
            throw new ValidationException(e.getErrorCode(), e.getMessage(), this, e);
        } catch (MotorConfigurationException e) {
            throw new ValidationException(ErrorCode.FLOW_CONFIGURATION_ERROR, e.getMessage(), this, e);
        }
    }

    @Override
    protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {
        String deviceName = getDeviceName();
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - initializing reference position of " + deviceName);
        } else {
            try {
                // run DefinePosition
                DeviceProxy dev = getDeviceProxy();
                // Call Init if necessary before initialized process
                if (MotorManager.isMotorIsInit(conf, this, shouldInitDevice, dev)) {
                    ExecutionTracerService.trace(this, "Warning: " + deviceName
                            + " is already initialized, nothing done");
                } else if (TangoAccess.getCurrentState(deviceName) != DevState.OFF) {
                    // run InitReferencePosition
                    runInitRefAndWaitEndMovement(deviceName, dev);
                    // Bug 22954
                    TangoAccess.getCurrentState(deviceName);
                    MotorManager.raiseExceptionIfInitFailed(dev, ctxt, this);
                    ExecutionTracerService.trace(this, deviceName + " reference position succeed");

                    // if the motor was off before the init, we switch it to off again
                    if (conf.isSwitchToOffAfterInit()) {
                        TangoCommand stateCmd = new TangoCommand(getDeviceName(), "State");
                        executeCmdAccordingState(new OffCommand(this, getDeviceName(), stateCmd), DevState.ON,
                                DevState.STANDBY, DevState.ALARM);
                    }
                } else {
                    ExecutionTracerService.trace(this, "Warning: " + deviceName + " is OFF, nothing done");
                }

            } catch (DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            } catch (PasserelleException e) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, ErrorCode.FATAL, e.getMessage(), ctxt, e);
            }

            response.addOutputMessage(output, createMessage());

        }
    }

    private void runInitRefAndWaitEndMovement(String deviceName, DeviceProxy dev) throws DevFailed {
        dev.command_inout(MotorManager.INITIALIZE_REFERENCE_POSITION);
        ExecutionTracerService.trace(this, "Call " + deviceName + "/" + MotorManager.INITIALIZE_REFERENCE_POSITION);
        waitTask = new WaitStateTask(deviceName, DevState.MOVING, 1000, false);
        waitTask.run();
        if (waitTask.hasFailed())
            throw waitTask.getDevFailed();
    }

    private void stopMotor() {
        if (!isMockMode()) {
            MotorManager.stopMotor(getDeviceName(), this, waitTask);
        }
    }

    @Override
    protected void doStop() {
        stopMotor();
        super.doStop();
    }

    @Override
    public void doFinalAction() {
        stopMotor();
    }
}
