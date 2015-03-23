package fr.soleil.passerelle.actor.tango.control;

import java.net.URL;

import ptolemy.actor.Director;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.EncoderType;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.InitType;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfiguration;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class MotorInitReferencePosition extends ATangoDeviceActor implements IActorFinalizer {

    private EncoderType encoder;
    private InitType initStrategy;
    private MotorConfiguration conf;

    public Port noInitDone;
    private WaitStateTask waitTask;

    public MotorInitReferencePosition(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setName("InitOK");
        noInitDone = PortFactory.getInstance().createOutputPort(this, "noInitDone");

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
    }

    @Override
    protected void doInitialize() throws InitializationException {
        super.doInitialize();
        if (!isMockMode()) {
            final Director dir = getDirector();
            if (dir instanceof BasicDirector) {
                ((BasicDirector) dir).registerFinalizer(this);
            }
            try {
                // test if commands exists, otherwise this device is not a motor
                final DeviceProxy dev = getDeviceProxy();
                dev.command_query("MotorON");
                dev.command_query("InitializeReferencePosition");
                conf = new MotorConfiguration(getDeviceName());
                conf.retrieveConfig();
                encoder = conf.getEncoder();
                initStrategy = conf.getInitStrategy();
                // System.out.println(encoder + " " + initStrategy);
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            } catch (final PasserelleException e) {
                ExceptionUtil.throwInitializationException(e.getMessage(), this, e);
            }
        }
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {
        final String deviceName = getDeviceName();
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - initializing reference position of " + deviceName);
        } else {
            DeviceProxy dev = null;
            try {
                dev = getDeviceProxy();
            } catch (final PasserelleException e1) {
                ExceptionUtil.throwProcessingException("Invalide DeviceProxy " + e1.getMessage(), this);
            }
            try {
                conf.initMotor(this);

                // Do an InitializeReferencePosition when possible
                final String AXIS_NOT_INIT = "axis not initialized [no initial ref. pos.]";
                if (!dev.status().contains(AXIS_NOT_INIT)) {
                    ExecutionTracerService.trace(this, deviceName + " is already initialized, nothing done");
                    // output data on output to mean that init is OK
                    // sendOutputMsg(output,
                    // PasserelleUtil.createTriggerMessage());
                    response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());

                } else if (encoder.equals(EncoderType.ABSOLUTE)) {
                    ExecutionTracerService.trace(this, deviceName + " has an absolute encoder, no need to intialize");
                    // sendOutputMsg(noInitDone, PasserelleUtil
                    // .createTriggerMessage());
                    response.addOutputMessage(1, noInitDone, PasserelleUtil.createTriggerMessage());

                } else if (initStrategy.equals(InitType.DP)) {
                    ExecutionTracerService.trace(this, deviceName
                            + " has no intialization strategy, must use DefinePosition");
                    // sendOutputMsg(noInitDone, PasserelleUtil
                    // .createTriggerMessage());
                    response.addOutputMessage(1, noInitDone, PasserelleUtil.createTriggerMessage());
                } else {
                    dev.command_inout("InitializeReferencePosition");
                    ExecutionTracerService.trace(this, "initializing reference position of " + deviceName);
                    // since I am not sure that the device motor switch
                    // immediatly to the moving state, do a little sleep
                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        // ignore
                    }
                    waitTask = new WaitStateTask(deviceName, DevState.MOVING, 1000, false);
                    waitTask.run();
                    if (waitTask.hasFailed()) {
                        throw waitTask.getDevFailed();
                    }

                    // Bug 22954
                    final DevState currentState = TangoAccess.getCurrentState(deviceName);
                    if (currentState.equals(DevState.FAULT) || currentState.equals(DevState.ALARM)
                            && dev.status().contains(AXIS_NOT_INIT)) {
                        final String status = dev.status();
                        ExecutionTracerService.trace(this, deviceName + " has not been correcty inialized: " + status);
                        ExceptionUtil.throwProcessingException(deviceName + " has not been correcty inialized: "
                                + status, deviceName);
                    } else {
                        ExecutionTracerService.trace(this, deviceName + " reference position initialized");
                        // sendOutputMsg(output, PasserelleUtil
                        // .createTriggerMessage());
                        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());

                    }
                }

            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
    }

    @Override
    protected void doStop() {
        if (waitTask != null) {
            waitTask.cancel();
        }
        super.doStop();
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
