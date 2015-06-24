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
public class OnCommand extends Command {

    public static final String ON_ERROR_MSG = "error while after command On on control box, device is still in OFF";

    public OnCommand(Actor actor, String deviceName, TangoCommand stateCommand) throws DevFailed {
        super(actor, deviceName, stateCommand);
        command = new TangoCommand(deviceName, MotorManager.MOTOR_ON);
    }

    @Override
    public void execute(DevState... states) throws DevFailed, ProcessingException {
        ExecutionTracerService.trace(actor, "Motor is off, try to execute On command" + deviceName);
        command.execute();
        DevState deviceState = stateCommand.execute(DevState.class);
        for (DevState state : states) {
            if (state == deviceState) {
                ExceptionUtil.throwProcessingExceptionWithLog(actor, ON_ERROR_MSG, this);
            }
        }
    }
}
