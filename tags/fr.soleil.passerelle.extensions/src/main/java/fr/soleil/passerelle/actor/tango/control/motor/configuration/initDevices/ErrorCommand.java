package fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices;

import com.isencia.passerelle.actor.Actor;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;
import fr.soleil.tango.clientapi.TangoCommand;

/**
 * raise an exception if the device is in particularly state eg: In the case of motor
 * initialisation, an error must be raise if the motor is disable or moving
 */
public class ErrorCommand extends Command {

    public ErrorCommand(Actor actor, String deviceName, TangoCommand stateCommand) throws DevFailed {
        super(actor, deviceName, stateCommand);
    }

    @Override
    public void execute(DevState... states) throws DevFailed, ProcessingExceptionWithLog {
        DevState deviceState = stateCommand.execute(DevState.class);
        for (DevState state : states) {
            if (state == deviceState) {
                throw new ProcessingExceptionWithLog(actor, "Error device " + deviceName
                        + " is in  " + state + " state", null, null);
            }
        }
    }
}
