package fr.soleil.passerelle.actor.tango.control.motor.actions;

import fr.esrf.Tango.DevFailed;

public interface IMoveAction {

	public void init() throws DevFailed;

	public void move() throws DevFailed;

	public void waitEndMouvement() throws DevFailed;

	public void cancelWaitEnd();

	public String getStatus() throws DevFailed;

	public void setDeviceName(String deviceName);

	public void setActionName(String actionName);

	public void setDesiredPosition(String position);
}
