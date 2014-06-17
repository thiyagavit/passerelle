package fr.soleil.passerelle.actor.tango.control;

import java.net.URL;

import ptolemy.actor.Director;
import ptolemy.data.BooleanToken;
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
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationException;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.DevFailedValidationException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;

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

  public static final String INITIALIZE_REFERENCE_POSITION = "InitializeReferencePosition";
  public static final String AXIS_NOT_INIT = "axis not initialized [no initial ref. pos.]";
  public static final String USE_SIMULATED_MOTOR = "Use simulated motor";
  public static final String INIT_DEVICES = "Should init controlBox and galilAxis devices";
  public static final String DEFAULT_ACTORNAME = "MotorInitReferencePosition.";

  private MotorConfigurationV2 conf;
  private WaitStateTask waitTask;

  /**
   * flag that indicate whether the actor must initialize the devices (Cb an Galil) prior to
   * execute InitializeReferencePosition
   */
  @ParameterName(name = INIT_DEVICES)
  public Parameter shouldInitDevicesParam;
  private boolean shouldInitDevice = false;

  /**
   * flag to indicate whether the actor must use the simulated devices
   */
  @ParameterName(name = USE_SIMULATED_MOTOR)
  public Parameter useSimulatedMotorParam;
  private boolean useSimulatedMotor = false;

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
      dev.command_query(INITIALIZE_REFERENCE_POSITION);

      conf = new MotorConfigurationV2(dev, getDeviceName(), useSimulatedMotor);
      conf.retrieveFullConfig();
      conf.assertInitRefPosBeApplyOnMotor();
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
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
      throws ProcessingException {
    String deviceName = getDeviceName();
    if (isMockMode()) {
      ExecutionTracerService.trace(this, "MOCK - initializing reference position of "
          + deviceName);
    } else {
      try {
        DeviceProxy dev = getDeviceProxy();

        if (!dev.status().contains(AXIS_NOT_INIT)) {
          ExecutionTracerService.trace(this, "Warning: " + deviceName
              + " is already initialized, nothing done");

        } else {
          if (shouldInitDevice) {
            conf.initDevice(this);
          }

          // run InitReferencePosition
          runInitRefAndWaitEndMovement(deviceName, dev);

          // Bug 22954
          final DevState currentState = TangoAccess.getCurrentState(deviceName);

          // check init has been correctly executed
          final String status = dev.status();
          if (currentState.equals(DevState.FAULT) || currentState.equals(DevState.ALARM)
              && status.contains(AXIS_NOT_INIT)) {

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
    dev.command_inout(INITIALIZE_REFERENCE_POSITION);
    ExecutionTracerService.trace(this, "initializing reference position of " + deviceName);
    // since I am not sure that the device motor switch immediately to the moving state, do a
    // little sleep
    try {
      Thread.sleep(1000);
    }
    catch (final InterruptedException e) {
      e.printStackTrace();
      // TODO
    }
    waitTask = new WaitStateTask(deviceName, DevState.MOVING, 1000, false);
    waitTask.run();
    if (waitTask.hasFailed())
      throw waitTask.getDevFailed();
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
  protected void doStop() {
    stopMotor();
    super.doStop();
  }

  @Override
  public void doFinalAction() {
    stopMotor();
  }
}