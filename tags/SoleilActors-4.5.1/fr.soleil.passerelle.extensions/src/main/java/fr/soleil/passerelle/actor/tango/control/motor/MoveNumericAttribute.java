package fr.soleil.passerelle.actor.tango.control.motor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.tango.clientapi.TangoAttribute;

public class MoveNumericAttribute implements IMoveAction {

    private final static Logger logger = LoggerFactory.getLogger(MoveNumericAttribute.class);

    protected TangoAttribute attributeToMove;
    protected String deviceName;
    protected String position;
    protected String attributeToMoveName;
    private WaitStateTask waitTask;

    public void move() throws DevFailed {
	logger.debug("moving " + deviceName + "/" + attributeToMoveName + " to " + position);
	attributeToMove.write(position);
    }

    public void init() throws DevFailed {
	attributeToMove = new TangoAttribute(deviceName + "/" + attributeToMoveName);
    }

    public String getStatus() throws DevFailed {
	String status = "motor is at ";
	status += attributeToMove.readAsString("", "");
	return status;
    }

    public void setDeviceName(final String deviceName) {
	this.deviceName = deviceName;
    }

    public void setDesiredPosition(final String position) {
	this.position = position;

    }

    public void setActionName(final String actionName) {
	attributeToMoveName = actionName;

    }

    public void waitEndMouvement() throws DevFailed {
	waitTask = new WaitStateTask(attributeToMove.getAttributeProxy().getDeviceProxy(),
		DevState.MOVING, 100, false);
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
}
