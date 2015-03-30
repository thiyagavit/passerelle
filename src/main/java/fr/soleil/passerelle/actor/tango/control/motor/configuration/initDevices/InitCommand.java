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
 * run the command "Init" and raise exception if device is in particularly state after the execution
 * of the command
 */
public class InitCommand extends Command {

    public static final String Init_ERROR_MSG = "error while after command init on control box, device is still in error";

    public InitCommand(Actor actor, String deviceName, TangoCommand stateCommand) throws DevFailed {
        super(actor, deviceName, stateCommand);
        command = new TangoCommand(deviceName, MotorManager.INIT_CMD);
    }

    @Override
    public void execute(DevState... states) throws DevFailed, ProcessingException {
        ExecutionTracerService.trace(actor, "Init command executed on " + deviceName);
        command.execute();

        try {
            Thread.sleep(200);
        }
        catch (InterruptedException e) {
            e.printStackTrace();// TODO log
        }

        DevState deviceState = stateCommand.execute(DevState.class);
        for (DevState state : states) {
            if (state == deviceState) {
                ExceptionUtil.throwProcessingExceptionWithLog(actor, Init_ERROR_MSG, this);
            }
        }
    }
}
