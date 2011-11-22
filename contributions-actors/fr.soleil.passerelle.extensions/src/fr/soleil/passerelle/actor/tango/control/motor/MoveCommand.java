package fr.soleil.passerelle.actor.tango.control.motor;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.tango.clientapi.TangoCommand;

public class MoveCommand implements IMoveAction {

    protected TangoCommand cmd;
    protected String deviceName;
    protected String position;
    protected String commandName;
    private WaitStateTask waitTask;

    public void init() throws DevFailed {
	cmd = new TangoCommand(deviceName, commandName);
    }

    public void move() throws DevFailed {
	cmd.execute(position);
    }

    public void waitEndMouvement() throws DevFailed {
	waitTask = new WaitStateTask(cmd.getDeviceProxy(), DevState.MOVING,
		500, false);
	waitTask.run();
	if (waitTask.hasFailed()) {
	    throw waitTask.getDevFailed();
	}
    }

    public void cancelWaitEnd() {
	if (waitTask != null) {
	    waitTask.cancel();
	}
    }

    public String getStatus() throws DevFailed {
	return cmd.getDeviceProxy().status();
    }

    public void setDeviceName(final String deviceName) {
	this.deviceName = deviceName;

    }

    public void setDesiredPosition(final String position) {
	this.position = position;

    }

    public void setActionName(final String actionName) {
	commandName = actionName;

    }

}
