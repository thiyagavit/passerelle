package fr.soleil.passerelle.actor.tango.control;

import static fr.soleil.passerelle.actor.tango.control.motor.configuration.EncoderType.ABSOLUTE;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.InitType.DP;

import java.net.URL;

import ptolemy.actor.Director;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.DevFailedValidationException;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;

public class MotorInitReferencePositionV2 extends ATangoDeviceActorV5 implements IActorFinalizer {

    public static final String NO_INIT_DONE_PORT_NAME = "noInitDone";
    public static String OUTPUT_PORT_NAME = "InitOK";
    public final Port noInitDone;
    private final String AXIS_NOT_INIT = "axis not initialized [no initial ref. pos.]";
    private MotorConfigurationV2 conf;
    private WaitStateTask waitTask;

    public MotorInitReferencePositionV2(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        final URL url = this.getClass().getResource(
                "/org/tango-project/tango-icon-theme/32x32/categories/applications-system.png");
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

        output.setName(OUTPUT_PORT_NAME);
        noInitDone = PortFactory.getInstance().createOutputPort(this, NO_INIT_DONE_PORT_NAME);
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
            dev.command_query("MotorON");
            dev.command_query("InitializeReferencePosition");

            conf = new MotorConfigurationV2(getDeviceName(), true);
            conf.retrieveFullConfig();

        }
        catch (DevFailed devFailed) {
            throw new DevFailedValidationException(devFailed, this);
        }
        catch (PasserelleException e) {
            throw new ValidationException(e.getErrorCode(), e.getMessage(), this, e);
        }
    }

    @Override
    protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {
        String deviceName = getDeviceName();
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - initializing reference position of "
                    + deviceName);
        } else {
            try {
                DeviceProxy dev = getDeviceProxy();

                conf.initDevice(this);

                if (conf.getEncoder() == ABSOLUTE) {
                    ExecutionTracerService.trace(this, deviceName
                            + " has an absolute encoder, no need to initialize");
                    response.addOutputMessage(noInitDone, createMessage());

                } else if (conf.getInitStrategy() == DP) {
                    ExecutionTracerService.trace(this, deviceName
                            + "  has no initialization strategy, must use DefinePosition");
                    response.addOutputMessage(noInitDone, createMessage());

                } else if (!dev.status().contains(AXIS_NOT_INIT)) {
                    ExecutionTracerService.trace(this, deviceName
                            + " is already initialized, nothing done");
                    response.addOutputMessage(output, createMessage());

                } else { // run InitReferencePosition
                    runInitRefAndWaitEndMovement(deviceName, dev);

                    // Bug 22954
                    final DevState currentState = TangoAccess.getCurrentState(deviceName);

                    // check init has been correctly executed
                    if (currentState.equals(DevState.FAULT) || currentState.equals(DevState.ALARM)
                            && dev.status().contains(AXIS_NOT_INIT)) {
                        final String status = dev.status();
                        ExecutionTracerService.trace(this, deviceName
                                + " has not been correcty inialized: " + status);
                        throw new ProcessingExceptionWithLog(this,
                                PasserelleException.Severity.FATAL, deviceName
                                        + " has not been correcty inialized: " + status, ctxt, null);
                    } else {
                        // if the motor was off before the init, we switch it to off again
                        if (conf.isSwitchToOffAfterInit()) {
                            dev.command_inout("MotorOff");
                        }
                        ExecutionTracerService.trace(this, deviceName
                                + " reference position initialized");
                        response.addOutputMessage(output, createMessage());
                    }
                }

            }
            catch (DevFailed e) {
                throw new DevFailedProcessingException(e, this);

            }
            catch (PasserelleException e) {
                throw new ProcessingExceptionWithLog(this, PasserelleException.Severity.FATAL,
                        e.getMessage(), ctxt, e);
            }

        }
    }

    private void runInitRefAndWaitEndMovement(String deviceName, DeviceProxy dev) throws DevFailed {
        dev.command_inout("InitializeReferencePosition");
        ExecutionTracerService.trace(this, "initializing reference position of " + deviceName);
        // since I am not sure that the device motor switch immediately to the moving state, do a
        // little sleep
        try {
            Thread.sleep(1000);
        }
        catch (final InterruptedException e) {
            // ignore
        }
        waitTask = new WaitStateTask(deviceName, DevState.MOVING, 1000, false);
        waitTask.run();
        if (waitTask.hasFailed()) {
            throw waitTask.getDevFailed();
        }
    }

    private void stopMotor() {
        if (!isMockMode()) {
            if (waitTask != null) {
                waitTask.cancel();
            }
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
                ExecutionTracerService.trace(this, e.getMessage());
            }
        }
    }

    @Override
    protected void doStop() { // TODO send STOP ??
        stopMotor();
        super.doStop();
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}