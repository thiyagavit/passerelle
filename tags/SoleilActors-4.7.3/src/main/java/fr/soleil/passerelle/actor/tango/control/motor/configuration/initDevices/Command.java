package fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices;

import com.isencia.passerelle.actor.Actor;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;
import fr.soleil.tango.clientapi.TangoCommand;

/**
 * run a command on device and raise an exception if device is in particularly state after the
 * execution of the command
 */
public abstract class Command {
    protected final Actor actor;
    protected final TangoCommand stateCommand;
    protected final String deviceName;
    protected TangoCommand command;

    /**
     * Build a command
     * 
     * @param actor the actor which "run " the command. It's use to trace message in
     *            ExecutorTracerService
     * @param deviceName the name of device on which the command will be executed
     * @param stateCommand the command use to know the state device
     * 
     * @throws DevFailed if an tango error occurred (bad device name...)
     */
    public Command(Actor actor, String deviceName, TangoCommand stateCommand) throws DevFailed {
        this.deviceName = deviceName;
        this.actor = actor;
        this.stateCommand = stateCommand;
    }

    /**
     * @return the command use to know the state device
     */
    public TangoCommand getStateCommand() {
        return stateCommand;
    }

    /**
     * execute the command and throw an ProcessingExceptionWithLog if device is in state defined by
     * parameter states
     * 
     * @param states state array in which the device should not be after the execution of the
     *            command
     * 
     * @return true if a specific action must be done after execution the command. It only usefull
     *         for ON //TODO change this
     * 
     * @throws DevFailed if an tango error occurred ( timeout ...)
     * @throws ProcessingExceptionWithLog (if the device in if device is in state defined by
     *             parameter states after the execution of the command)
     */
    public abstract boolean execute(DevState... states) throws DevFailed,
            ProcessingExceptionWithLog;
}