package fr.soleil.passerelle.actor.tango.control;

import java.net.URL;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

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
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationException;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorManager;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.DevFailedValidationException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;

/**
 * this actor initialize the devices (cb and Galil) according to shouldInitDevice parameter and run
 * an DefinePosition on the motor specified by DeviceName parameter. To be able to initialize a
 * motor, it must be in On or StandBy state. So if the motor is OFF before the beginning of the
 * initialization we switch it to On then we initialize it and to finish we switch it to Off again.
 * 
 * If the deviceName is Empty then an IllegalActionException or a ValidateException is thrown.
 * 
 * if the device has not the following commands: InitializeReferencePosition and MotorOn then an
 * IllegalActionException or a ValidateException is thrown
 * 
 * If an error occurred during the initialization of the motor then an ProcessingException is thrown
 * 
 */
public class DefinePosition extends ATangoDeviceActorV5 {

  private static final long serialVersionUID = 8283763328387658706L;

  public static final String DEFAULT_ACTORNAME = "DefinePosition.";
  public static final String OUTPUT_PORT_NAME = "InitOK";
  
  public final Port offsetPort;

  private MotorConfigurationV2 conf;

  /**
   * flag that indicate whether the actor must initialize the devices (Cb an Galil) prior to
   * execute InitializeReferencePosition
   */
  @ParameterName(name = MotorManager.INIT_DEVICES)
  public Parameter shouldInitDevicesParam;
  private boolean shouldInitDevice = false;

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
    offsetPort = PortFactory.getInstance().createInputPort(this, MotorManager.OFFSET, null);

    input.setName("position");
    shouldInitDevicesParam = new Parameter(this, MotorManager.INIT_DEVICES, new BooleanToken(
        shouldInitDevice));
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
  protected void validateInitialization() throws ValidationException {
    super.validateInitialization();

    try {
      // test if commands exists, otherwise this device is not a motor
      final DeviceProxy dev = getDeviceProxy();
      dev.command_query(MotorManager.MOTOR_OFF);
      dev.command_query(MotorManager.DEFINE_POSITION);
      conf = new MotorConfigurationV2(getDeviceName());
      conf.retrieveFullConfig();
      conf.assertDefinePositionCanBeApplyOnMotor();
    }
    catch (DevFailed devFailed) {
      throw new DevFailedValidationException(devFailed, this);
    }
    catch (PasserelleException e) {
      throw new ValidationException(e.getErrorCode(), e.getMessage(), this, e);
    }
    catch (MotorConfigurationException e) {
      throw new ValidationException(ErrorCode.FLOW_CONFIGURATION_ERROR, e.getMessage(), this,
          e);
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

        // run DefinePosition

        double position = 0;
        String offset = "";

        position = Double.parseDouble((String) PasserelleUtil.getInputValue(request
            .getMessage(input)));

        // if port is Connected
        ManagedMessage offsetManagedMsg = request.getMessage(offsetPort);
        if (offsetManagedMsg != null) {
          offset = ((String) PasserelleUtil.getInputValue(offsetManagedMsg)).trim();
          // message is not empty
          if (!offset.isEmpty()) {

            // set the offset
            dev.write_attribute(new DeviceAttribute(MotorManager.OFFSET, Double
                .parseDouble(offset)));
          }
        }

        // set the position
        DeviceData value = new DeviceData();
        value.insert(position);
        dev.command_inout(MotorManager.DEFINE_POSITION, value);

        raiseExceptionIfDefinePositionFailed(dev, context);

        // if the motor was off before the init, we switch it to off again
        if (conf.isSwitchToOffAfterInit()) {
          dev.command_inout(MotorManager.MOTOR_OFF);
        }

        StringBuilder msg = new StringBuilder(deviceName);
        msg.append("define position applied with position: ");
        msg.append(position);
        if (!offset.isEmpty()) {
          msg.append(" ; offset: ");
          msg.append(offset);
        }
        ExecutionTracerService.trace(this, msg.toString());
        response.addOutputMessage(output, createMessage());

      }
      catch (NumberFormatException e) {
        throw new ProcessingExceptionWithLog(this, PasserelleException.Severity.FATAL,
            "Error: position or offset is not a number", context, null);
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
    TangoAccess.getCurrentState(deviceName);

    // if the motor is at the end of the rail (on the stop), the state is Alarm but it's ok.
    // So to be sure the definePosition command was successful we must check the status
    if (dev.status().contains(MotorManager.AXIS_NOT_INIT))
      throw new ProcessingExceptionWithLog(this, PasserelleException.Severity.FATAL,
          deviceName + " has not been correcty inialized: " + dev.status(), context, null);
  }
}
