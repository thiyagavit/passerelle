package fr.soleil.passerelle.actor.tango.control.motor.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tango.utils.DevFailedUtils;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.comete.tango.data.service.helper.TangoDeviceHelper;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;

public class MotorManager {

    public static final String AXIS_ENCODER_TYPE_PROPERTY = "AxisEncoderType";
    public static final String AXIS_INIT_TYPE_PROPERTY = "AxisInitType";
    public static final String AXIS_INIT_POSITION_PROPERTY = "AxisInitPosition";

    // Device status
    public static final String AXIS_NOT_INIT = "axis not initialized";

    // Device command
    public static final String MOTOR_ON = "MotorON";
    public static final String INIT_CMD = "Init";
    public static final String MOTOR_OFF = "MotorOFF";
    public static final String MOTOR_STOP = "Stop";
    public static final String INITIALIZE_REFERENCE_POSITION = "InitializeReferencePosition";
    public static final String DEFINE_POSITION = "DefinePosition";

    // Attribute
    public static final String POSITION = "position";
    public static final String OFFSET = "offset";

    // Parameter
    public static final String INIT_DEVICES = "Should init controlBox and galilAxis devices";
    public static final String ON_IF_NEEDED = "Turn on as needed";

    // Errors messages
    public static final String AXIS_ENCODER_TYPE_PROPERTY_IS_NOT_INT = AXIS_ENCODER_TYPE_PROPERTY
            + " does not exist or is not an integer";
    public static final String AXIS_INIT_POSITION_PROPERTY_IS_NAN = AXIS_INIT_POSITION_PROPERTY
            + " does not exist or is not a number";

    private static final Map<String, String> motorClassesMap = new HashMap<String, String>();
    private static final Map<String, String> motorControlBoxMap = new HashMap<String, String>();
    private static final Map<String, EncoderType> motorEncoderTypeMap = new HashMap<String, EncoderType>();
    private static final Map<String, InitType> motorInitTypeMap = new HashMap<String, InitType>();
    private static final List<String> controlBoxClasses = new ArrayList<String>();
    private static final List<String> motorClasses = new ArrayList<String>();

    static {
        // Simulated motor
        motorClassesMap.put("SimulatedControlBox", "SimulatedMotor");
        motorClassesMap.put("SimulatedMotor", "SimulatedControlBox");

        // GalilAxis
        motorClassesMap.put("ControlBox", "GalilAxis");
        motorClassesMap.put("GalilAxis", "ControlBox");

        // Control box classes
        controlBoxClasses.add("SimulatedControlBox");
        controlBoxClasses.add("ControlBox");

        // motor classes
        motorClasses.add("GalilAxis");
        motorClasses.add("SimulatedMotor");
    }

    public static String getAssociatedClassForMotorClass(String motorNameClass) {
        return motorClassesMap.get(motorNameClass);
    }

    public static String getAssociatedClassForMotor(String motorName) {
        String assiociatedClass = null;
        if (!isNullOrEmpty(motorName)) {
            Database database = TangoDeviceHelper.getDatabase();
            if (database != null) {
                try {
                    String motorClass = database.get_class_for_device(motorName);
                    assiociatedClass = getAssociatedClassForMotorClass(motorClass);
                } catch (DevFailed e) {
                    e.printStackTrace();
                }
            }
        }
        return assiociatedClass;
    }

    public static List<String> getControlBoxClasses() {
        return controlBoxClasses;
    }

    public static List<String> getMotorClasses() {
        return motorClasses;
    }

    public static String getControlBoxForMotor(String motorName) {
        String cb = motorControlBoxMap.get(motorName);
        if (cb == null) {
            String cbClass = getAssociatedClassForMotor(motorName);
            if (cbClass != null) {
                Database database = TangoDeviceHelper.getDatabase();
                if (database != null) {
                    try {
                        String serverInstanceName = database.get_device_info(motorName).server;
                        String[] cbList = database.get_device_name(serverInstanceName, cbClass);
                        if (cbList != null && cbList.length > 0) {
                            cb = cbList[0];
                        }
                    } catch (DevFailed e) {
                        System.err.println("No control box found=" + DevFailedUtils.toString(e));
                    }
                }
            }
            if (!isNullOrEmpty(cb)) {
                motorControlBoxMap.put(motorName, cb);
            }
        }

        return cb;
    }

    public static boolean isMotorClass(String motorName) {
        boolean isMotorClass = false;
        if (!isNullOrEmpty(motorName)) {
            Database database = TangoDeviceHelper.getDatabase();
            if (database != null) {
                try {
                    String motorClass = database.get_class_for_device(motorName);
                    isMotorClass = motorClasses.contains(motorClass);
                } catch (DevFailed e) {
                    e.printStackTrace();
                }
            }
        }
        return isMotorClass;
    }

    public static boolean isNullOrEmpty(String stringValue) {
        return (stringValue == null || stringValue.isEmpty());
    }

    public static EncoderType getEncoderType(String motorName) throws DevFailed {
        EncoderType type = motorEncoderTypeMap.get(motorName);
        if (type == null) {
            type = EncoderType.NONE;
            String encoderType = TangoAccess.getDeviceProperty(motorName, AXIS_ENCODER_TYPE_PROPERTY);
            if (!isNullOrEmpty(encoderType)) {
                int encoderInt = EncoderType.NONE.ordinal();

                try {
                    encoderInt = Integer.parseInt(encoderType);
                } catch (NumberFormatException e) {
                    // encoderInt = EncoderType.NONE.ordinal();
                    DevFailedUtils.throwDevFailed(AXIS_ENCODER_TYPE_PROPERTY_IS_NOT_INT + " for device " + motorName);
                }
                type = EncoderType.getValueFromOrdinal(encoderInt);
            }
            motorEncoderTypeMap.put(motorName, type);
        }
        return type;
    }

    public static InitType getInitType(String motorName) throws DevFailed {
        InitType type = motorInitTypeMap.get(motorName);
        if (type == null) {
            String initStrategyString = TangoAccess.getDeviceProperty(motorName, AXIS_INIT_TYPE_PROPERTY);
            if (!isNullOrEmpty(initStrategyString)) {
                type = InitType.getValuefromString(initStrategyString);
            }
            motorInitTypeMap.put(motorName, type);
        }
        return type;
    }

    public static Double getAxisInitPosition(String motorName) throws DevFailed {
        Double axisInitPosition = null;
        String initPosition = TangoAccess.getDeviceProperty(motorName, AXIS_INIT_TYPE_PROPERTY);
        if (!isNullOrEmpty(initPosition)) {
            try {
                axisInitPosition = Double.parseDouble(initPosition);
            } catch (NumberFormatException e) {
                DevFailedUtils.throwDevFailed(AXIS_INIT_POSITION_PROPERTY_IS_NAN);
            }
        }
        return axisInitPosition;
    }

    public static boolean isMotorIsInit(MotorConfigurationV2 config, Actor actor, boolean shouldInit, DeviceProxy dev)
            throws ProcessingException, DevFailed {
        boolean init = false;
        // Call Init if necessary before initialized process
        if (shouldInit) {
            config.initDevice(actor);
        }

        if (dev != null) {
            DevState state = dev.state();
            if (state != DevState.FAULT && state != DevState.UNKNOWN) {
                init = !dev.status().contains(MotorManager.AXIS_NOT_INIT);
            }
        }
        return init;
    }
    
    public static void stopMotor(String deviceName,Actor actor,WaitStateTask waitTask ){
        if (waitTask != null) {
            waitTask.cancel();
        }
        try {
            // bug 22954
            if (TangoAccess.executeCmdAccordingState(deviceName, DevState.MOVING, MOTOR_STOP)) {
                ExecutionTracerService.trace(actor, "Call " + deviceName + "/" + MOTOR_STOP);
            }
        } catch (final DevFailed e) {
            TangoToPasserelleUtil.getDevFailedString(e, actor);
        } catch (final Exception e) {
            ExecutionTracerService.trace(actor, e.getMessage());
        }
    }
    

    public static void raiseExceptionIfInitFailed(DeviceProxy dev, ActorContext context, Actor actor) throws DevFailed,
            ProcessingException {
        String deviceName = dev.name();
        // Bug 22954
        DevState currentState = TangoAccess.getCurrentState(deviceName);
        // if the motor is at the end of the rail (on the stop), the state is Alarm but it's ok.
        // So to be sure the definePosition command was successful we must check the status
        if (dev.status().contains(MotorManager.AXIS_NOT_INIT) || currentState == DevState.FAULT || currentState == DevState.UNKNOWN){
            ExceptionUtil.throwProcessingExceptionWithLog(actor, ErrorCode.FATAL,   deviceName
                    + " has not been correctly inialized: " + dev.status(),context);
        }
         
    }

}
