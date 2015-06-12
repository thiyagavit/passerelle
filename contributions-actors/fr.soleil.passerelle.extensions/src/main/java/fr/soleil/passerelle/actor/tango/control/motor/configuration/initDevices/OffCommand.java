package fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorManager;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.tango.clientapi.TangoCommand;

/**
 * run the command "MotorON" and raise exception if device is in particularly state after the
 * execution of the command note: MotorON is a command of Galil Device
 */
public class OffCommand extends Command {

    public static final String OFF_ERROR_MSG = "error while after command OFF on motor, device is still in ON";

    public OffCommand(Actor actor, String deviceName, TangoCommand stateCommand) throws DevFailed {
        super(actor, deviceName, stateCommand);
        command = new TangoCommand(deviceName, MotorManager.MOTOR_OFF);
    }

    @Override
    public void execute(DevState... states) throws DevFailed, ProcessingException {
        ExecutionTracerService.trace(actor, "Motor is On, try to execute Off command" + deviceName);
        command.execute();
        DevState deviceState = stateCommand.execute(DevState.class);
        for (DevState state : states) {
            if (state == deviceState) {
                ExceptionUtil.throwProcessingExceptionWithLog(actor, OFF_ERROR_MSG, this);
            }
        }
    }
}
