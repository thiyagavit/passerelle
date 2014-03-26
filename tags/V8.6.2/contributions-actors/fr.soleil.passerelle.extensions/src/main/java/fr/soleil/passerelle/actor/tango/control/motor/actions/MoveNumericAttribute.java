package fr.soleil.passerelle.actor.tango.control.motor.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.tango.clientapi.TangoAttribute;

public class MoveNumericAttribute implements IMoveAction {

    private final static Logger logger = LoggerFactory.getLogger(MoveNumericAttribute.class);
    public static String STATUS_PREFIX = "motor is at ";
    protected TangoAttribute attributeToMove;
    protected String deviceName;
    protected String position;
    protected String attributeToMoveName;
    private WaitStateTask waitTask;

    @Override
    public void move() throws DevFailed {
        logger.debug("moving " + deviceName + "/" + attributeToMoveName + " to " + position);
        attributeToMove.write(position);
    }

    @Override
    public void init() throws DevFailed {
        attributeToMove = new TangoAttribute(deviceName + "/" + attributeToMoveName);
    }

    @Override
    public String getStatus() throws DevFailed {
        return STATUS_PREFIX + attributeToMove.readAsString("", "");
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
        attributeToMoveName = actionName;
    }

    @Override
    public void waitEndMouvement() throws DevFailed {
        waitTask = new WaitStateTask(this.deviceName, DevState.MOVING, 100, false);
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
}
