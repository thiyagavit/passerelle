package fr.soleil.passerelle.actor.tango.control.motor;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

public class MotorConfiguration {
	public enum EncoderType {
		NONE, INCREMENTAL, ABSOLUTE;
		public static EncoderType getValueFromOrdinal(final int ordinal) {
			EncoderType type;
			switch (ordinal) {
			case 1:
				type = INCREMENTAL;
				break;
			case 2:
				type = ABSOLUTE;
				break;
			case 0:
			default:
				type = NONE;
				break;
			}
			return type;
		}

	};

	public enum InitType {
		// LSBWD, LSFWD, FH, FI, DP;
		DP, OTHER;
		public static InitType getValueIfContains(final String compare) {
			InitType initStrategy;
			if (compare.startsWith(DP.toString())) {
				initStrategy = DP;
			} else {
				initStrategy = OTHER;
			}
			return initStrategy;
		}
	}

	private EncoderType encoder;
	private InitType initStrategy;
	private final String deviceName;
	private final DeviceProxy axisProxy;

	public MotorConfiguration(final String deviceName) throws DevFailed {
		this.deviceName = deviceName;
		axisProxy = ProxyFactory.getInstance().createDeviceProxy(deviceName);
	}

	public void retrieveConfig() throws DevFailed {

		final String[] props = { "AxisEncoderType", "AxisInitType" };
		final DbDatum[] datum = axisProxy.get_property(props);
		encoder = EncoderType.getValueFromOrdinal(datum[0].extractLong());
		initStrategy = InitType.getValueIfContains(datum[1].extractString());
	}

	public String retrieveMyControlBox() throws DevFailed {
		String controlBoxName = null;
		final DeviceData dd = axisProxy.get_adm_dev().command_inout(
				"QueryDevice");
		final String[] devices = dd.extractStringArray();
		for (final String device : devices) {
			final String[] classAndDevice = device.split("::");
			if (classAndDevice[0].equals("ControlBox")) {
				controlBoxName = classAndDevice[1];
				break;
			}
		}
		if (controlBoxName == null) {
			Except.throw_exception("TANGO_ERROR", "No control box attached to "
					+ deviceName, "MotorConfiguration.retrieveMyControlBox");
		}
		return controlBoxName;
	}

	public void initMotor(final Actor actor) throws DevFailed,
			ProcessingException {

		// 1- chech if devices (Control and GalilAxis) need an Init
		// command

		// searching for the related Controlbox device
		final String cbName = this.retrieveMyControlBox();
		final DeviceProxy controlBox = ProxyFactory.getInstance()
				.createDeviceProxy(cbName);
		if (controlBox.state().equals(DevState.FAULT)
				|| controlBox.state().equals(DevState.UNKNOWN)) {
			ExecutionTracerService.trace(actor, "Init command executed on "
					+ cbName);
			controlBox.command_inout("Init");
			// after init, does not switch immediatly to
			// correct state so wait a little
			try {
				Thread.sleep(200);
			} catch (final InterruptedException e) {
				// ignore
			}
			if (controlBox.state().equals(DevState.FAULT)
					|| controlBox.state().equals(DevState.UNKNOWN)) {
				throw new ProcessingException(
						"error while after command init on control box, device is still in error",
						null, null);
			}
		}

		// if control box in ALARM -> microcode has been stop
		if (controlBox.state().equals(DevState.ALARM)) {
			ExecutionTracerService.trace(actor,
					"StartMicrocode command executed on " + cbName);
			controlBox.command_inout("StartMicrocode");
		}

		// now initialize the galil axis
		if (axisProxy.state().equals(DevState.FAULT)
				|| axisProxy.state().equals(DevState.UNKNOWN)) {

			ExecutionTracerService.trace(actor, "Init command executed on "
					+ deviceName);
			axisProxy.command_inout("Init");

			// after init, does not switch immediatly to correct state
			// so wait a little
			try {
				Thread.sleep(200);
			} catch (final InterruptedException e) {
				// ignore
			}
		}
		if (axisProxy.state().equals(DevState.FAULT)
				|| axisProxy.state().equals(DevState.UNKNOWN)) {
			throw new ProcessingException(
					"error while after command init on axis, device is still in error",
					null, null);
		}

		// 2- chech if axis needs an On command
		if (axisProxy.state().equals(DevState.OFF)) {
			axisProxy.command_inout("MotorON");
			ExecutionTracerService.trace(actor, deviceName
					+ " was Off, Switched On");
		}
	}

	public EncoderType getEncoder() {
		return encoder;
	}

	public InitType getInitStrategy() {
		return initStrategy;
	}
}
