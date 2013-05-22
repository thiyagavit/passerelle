package fr.soleil.passerelle.actor.tango.control;

import static fr.soleil.passerelle.actor.tango.control.motor.configuration.EncoderType.ABSOLUTE;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.InitType.DP;

import java.net.URL;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.DevFailedValidationException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;

public class DefinePosition extends ATangoDeviceActorV5 {

    public static final String DEFINE_POSITION_CMD_NAME = "DefinePosition";
    public static final String OFFSET_PORT_NAME = "offset";
    public static final String AXIS_NOT_INIT = "axis not initialized [no initial ref. pos.]";
    public static final String USE_SIMULATED_MOTOR = "Use simulated motor";
    public static final String OUTPUT_PORT_NAME = "InitOK";
    public final Port offsetPort;

    private MotorConfigurationV2 conf;

    public static final String INIT_DEVICES = "Should init controlBox and galilAxis devices";
    @ParameterName(name = INIT_DEVICES)
    public Parameter shouldInitDevicesParam;
    private boolean shouldInitDevice = false;

    @ParameterName(name = USE_SIMULATED_MOTOR)
    public Parameter useSimulatedMotorParam;
    private boolean useSimulatedMotor = false;

    public DefinePosition(CompositeEntity container, String name) throws IllegalActionException,
            NameDuplicationException {
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
        offsetPort = PortFactory.getInstance().createInputPort(this, OFFSET_PORT_NAME, null);

        input.setName("position");
        shouldInitDevicesParam = new Parameter(this, INIT_DEVICES, new BooleanToken(
                shouldInitDevice));
        shouldInitDevicesParam.setTypeEquals(BaseType.BOOLEAN);

        useSimulatedMotorParam = new Parameter(this, USE_SIMULATED_MOTOR, new BooleanToken(
                useSimulatedMotor));
        useSimulatedMotorParam.setTypeEquals(BaseType.BOOLEAN);
        useSimulatedMotorParam.setVisibility(Settable.EXPERT);
    }

    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == shouldInitDevicesParam) {
            shouldInitDevice = PasserelleUtil.getParameterBooleanValue(shouldInitDevicesParam);
        } else if (attribute == useSimulatedMotorParam) {
            useSimulatedMotor = PasserelleUtil.getParameterBooleanValue(useSimulatedMotorParam);
        }
        super.attributeChanged(attribute);
    }

    @Override
    protected void validateInitialization() throws ValidationException {
        super.validateInitialization();

        try {
            // test if commands exists, otherwise this device is not a motor
            final DeviceProxy dev = getDeviceProxy();
            dev.command_query(DEFINE_POSITION_CMD_NAME);

            conf = new MotorConfigurationV2(dev, getDeviceName(), true);
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
    protected void process(ActorContext context, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {
        String deviceName = getDeviceName();
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - initializing reference position of "
                    + deviceName);
        } else {
            try {
                DeviceProxy dev = getDeviceProxy();

                if (shouldInitDevice) {
                    conf.initDevice(this);
                }

                if (conf.getEncoder() == ABSOLUTE) {
                    throw new ProcessingExceptionWithLog(this, PasserelleException.Severity.FATAL,
                            deviceName + " has an absolute encoder, no need to initialize",
                            context, null);

                } else if (conf.getInitStrategy() != DP) {
                    throw new ProcessingExceptionWithLog(
                            this,
                            PasserelleException.Severity.FATAL,
                            deviceName
                                    + "  has an initialization strategy, must use ReferenceInitPosition",
                            context, null);

                } else if (!dev.status().contains(AXIS_NOT_INIT)) {
                    throw new ProcessingExceptionWithLog(this, PasserelleException.Severity.FATAL,
                            deviceName + " is already initialized, nothing done", context, null);

                } else { // run DefinePosition
                    try {
                        double position = 0;
                        double offset = 0;

                        position = Double.parseDouble((String) PasserelleUtil.getInputValue(request
                                .getMessage(input)));

                        String offsetAsString = ((String) PasserelleUtil.getInputValue(request
                                .getMessage(offsetPort))).trim();

                        if (!offsetAsString.isEmpty()) {
                            offset = Double.parseDouble(offsetAsString);

                            // set the offset
                            dev.write_attribute(new DeviceAttribute("offset", offset));
                        }

                        // set the position
                        DeviceData value = new DeviceData();
                        value.insert(position);
                        dev.command_inout(DEFINE_POSITION_CMD_NAME, value);

                        raiseExceptionIfDefinePositionFailed(dev, context);

                        // if the motor was off before the init, we switch it to off again
                        if (conf.isSwitchToOffAfterInit()) {
                            dev.command_inout("MotorOff");
                        }

                        StringBuilder msg = new StringBuilder(deviceName);
                        msg.append("define position applied with position: ");
                        msg.append(position);
                        if (!offsetAsString.isEmpty()) {
                            msg.append(" ; offset: ");
                            msg.append(offset);
                        }
                        ExecutionTracerService.trace(this, msg.toString());
                        response.addOutputMessage(output, createMessage());

                    }
                    catch (NumberFormatException e) {
                        throw new ProcessingExceptionWithLog(this,
                                PasserelleException.Severity.FATAL,
                                "Error: position or offset is not a number", context, null);
                    }
                }

            }
            catch (DevFailed e) {
                throw new DevFailedProcessingException(e, this);
            }
            catch (PasserelleException e) {
                throw new ProcessingExceptionWithLog(this, PasserelleException.Severity.FATAL,
                        e.getMessage(), context, e);
            }

        }
    }

    private void raiseExceptionIfDefinePositionFailed(DeviceProxy dev, ActorContext context)
            throws DevFailed, ProcessingExceptionWithLog {
        String deviceName = getDeviceName();

        // Bug 22954
        final DevState currentState = TangoAccess.getCurrentState(deviceName);

        // if the motor is at the end of the rail (on the stop), the state is Alarm but it's ok.
        // So to be sure the definePosition command was successful we must check the status
        if (currentState.equals(DevState.FAULT)
                || (currentState.equals(DevState.ALARM) && dev.status().contains(AXIS_NOT_INIT))) {

            throw new ProcessingExceptionWithLog(this, PasserelleException.Severity.FATAL,
                    deviceName + " has not been correcty inialized: " + dev.status(), context, null);
        }
    }
}
