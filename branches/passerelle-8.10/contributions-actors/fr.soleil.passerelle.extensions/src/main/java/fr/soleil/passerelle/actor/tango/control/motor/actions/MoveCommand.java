package fr.soleil.passerelle.actor.tango.control.motor.actions;

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

    @Override
    public void init() throws DevFailed {
        cmd = new TangoCommand(deviceName, commandName);
    }

    @Override
    public void move() throws DevFailed {
        cmd.execute(position);
    }

    @Override
    public void waitEndMouvement() throws DevFailed {
        waitTask = new WaitStateTask(deviceName, DevState.MOVING, 500, false);
        waitTask.run();
        if (waitTask.hasFailed()) {
            throw waitTask.getDevFailed();
        }
    }

    @Override
    public void cancelWaitEnd() {
        if (waitTask != null) {
            waitTask.cancel();
        }
    }

    @Override
    public String getStatus() throws DevFailed {
        return cmd.getDeviceProxy().status();
    }

    @Override
    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;

    }

    @Override
    public void setDesiredPosition(final String position) {
        this.position = position;

    }

    @Override
    public void setActionName(final String actionName) {
        commandName = actionName;

    }

}
