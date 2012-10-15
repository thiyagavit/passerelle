package fr.soleil.passerelle.actor.tango.acquisition;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.Actor;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

public class ScientaUtil {

	private final static Logger logger = LoggerFactory.getLogger(ScientaUtil.class);
	protected String deviceName = "scienta";

	protected String regionName = "Region1";
	protected String[] regionNames = { "Region1" };

	protected String lensMode = "Transmission";
	protected String[] lensModes = { "Transmission" };

	protected double passEnergy = 2;
	protected double[] passEnergies = { 2 };

	protected double excitationEnergy = 0;
	protected double[] excitationEnergies = { 0 };

	protected String mode = "Fixed";
	protected String[] modes = { "Fixed" };

	protected String energyScale = "Kinetic";
	protected String[] energyScales = { "Kinetic" };

	protected double lowEnergy = 0;
	protected double[] lowEnergies = { 0 };

	protected double fixEnergy = 1;
	protected double[] fixEnergies = { 1 };

	protected double highEnergy = 1;
	protected double[] highEnergies = { 1 };

	protected double energyStep = 0.1;
	protected double[] energySteps = { 0.1 };

	protected double stepTime = 1;
	protected double[] stepTimes = { 1 };

	protected DeviceProxy dev;
	protected int sweepNumber = 1;
	protected int[] sweepNumbers = { 1 };
	private int currentSweep = 0;

	protected boolean isActive = true;
	protected boolean[] areActive = { true };

	Parameter deviceNameParam;
	Parameter regionNameParam;
	Parameter lensModeParam;
	Parameter passEnergyParam;
	Parameter modeParam;
	Parameter energyScaleParam;
	Parameter lowParam;
	Parameter fixParam;
	Parameter highParam;
	Parameter energyStepParam;
	Parameter stepTimeParam;
	Parameter sweepNumberParam;
	Parameter isActiveParam;

	private int currentConfigIdx = 0;
	private int nbConfig = 0;

	public ScientaUtil() {
	}

	public Vector<Parameter> createDefaultParameters(final Actor actor)
			throws IllegalActionException, NameDuplicationException {

		final Vector<Parameter> params = new Vector<Parameter>(13);
		deviceNameParam = new StringParameter(actor, "Device Name");
		deviceNameParam.setExpression(deviceName);
		params.add(deviceNameParam);

		regionNameParam = new StringParameter(actor, "Region Name");
		regionNameParam.setExpression(regionName);
		params.add(regionNameParam);

		isActiveParam = new StringParameter(actor, "Is Active");
		isActiveParam.setExpression(Boolean.toString(isActive));
		params.add(isActiveParam);

		lensModeParam = new StringParameter(actor, "Lens Mode");
		lensModeParam.setExpression(lensMode);
		modeParam.addChoice("Transmission");
		modeParam.addChoice("Angular");
		params.add(deviceNameParam);

		passEnergyParam = new StringParameter(actor, "Pass Energy");
		passEnergyParam.setExpression(Double.toString(passEnergy));
		params.add(passEnergyParam);

		energyScaleParam = new StringParameter(actor, "Energy Scale");
		energyScaleParam.setExpression(energyScale);
		energyScaleParam.addChoice("Kinetic");
		energyScaleParam.addChoice("Binding");
		params.add(energyScaleParam);

		lowParam = new StringParameter(actor, "Low energy");
		lowParam.setExpression(Double.toString(lowEnergy));
		params.add(lowParam);

		fixParam = new StringParameter(actor, "Fix energy");
		fixParam.setExpression(Double.toString(fixEnergy));
		params.add(fixParam);

		highParam = new StringParameter(actor, "High energy");
		highParam.setExpression(Double.toString(highEnergy));
		params.add(highParam);

		energyStepParam = new StringParameter(actor, "Energy step");
		energyStepParam.setExpression(Double.toString(energyStep));
		params.add(energyStepParam);

		stepTimeParam = new StringParameter(actor, "Step time");
		stepTimeParam.setExpression(Double.toString(stepTime));
		params.add(stepTimeParam);

		modeParam = new StringParameter(actor, "Mode");
		modeParam.setExpression(mode);
		modeParam.addChoice("Fixed");
		modeParam.addChoice("Swept");
		params.add(modeParam);

		sweepNumberParam = new StringParameter(actor, "Number of sweeps");
		sweepNumberParam.setExpression(Integer.toString(sweepNumber));
		params.add(sweepNumberParam);

		return params;

	}

	public Vector<Parameter> createMultiDefaultParameters(final Actor actor)
			throws IllegalActionException, NameDuplicationException {
		final Vector<Parameter> params = new Vector<Parameter>(13);
		deviceNameParam = new StringParameter(actor, "Device Name");
		deviceNameParam.setExpression(deviceName);
		params.add(deviceNameParam);

		regionNameParam = new StringParameter(actor, "Region Names");
		regionNameParam.setExpression(regionName);
		params.add(regionNameParam);

		isActiveParam = new StringParameter(actor, "Are Active");
		// isActiveParam.setExpression(Boolean.toString(isActive));
		isActiveParam.setExpression("true");
		logger.debug("isActive " + isActiveParam.getName());
		params.add(isActiveParam);

		lensModeParam = new StringParameter(actor, "Lens Modes");
		lensModeParam.setExpression(lensMode);
		params.add(modeParam);

		passEnergyParam = new StringParameter(actor, "Pass Energies");
		passEnergyParam.setExpression(Double.toString(passEnergy));
		params.add(passEnergyParam);

		energyScaleParam = new StringParameter(actor, "Energy Scales");
		energyScaleParam.setExpression(energyScale);
		params.add(energyScaleParam);

		lowParam = new StringParameter(actor, "Low energies");
		lowParam.setExpression(Double.toString(lowEnergy));
		params.add(lowParam);

		fixParam = new StringParameter(actor, "Fix energies");
		fixParam.setExpression(Double.toString(fixEnergy));
		params.add(fixParam);

		highParam = new StringParameter(actor, "High energies");
		highParam.setExpression(Double.toString(highEnergy));
		params.add(highParam);

		energyStepParam = new StringParameter(actor, "Energy steps");
		energyStepParam.setExpression(Double.toString(energyStep));
		params.add(energyStepParam);

		stepTimeParam = new StringParameter(actor, "Step times");
		stepTimeParam.setExpression(Double.toString(stepTime));
		params.add(stepTimeParam);

		modeParam = new StringParameter(actor, "Acq Modes");
		modeParam.setExpression(mode);
		params.add(modeParam);

		sweepNumberParam = new StringParameter(actor, "Numbers of sweeps");
		sweepNumberParam.setExpression(Integer.toString(sweepNumber));
		params.add(sweepNumberParam);

		return params;

	}

	/*
	 * public Vector<Parameter> createMultiConfigParameters(Actor actor) throws
	 * IllegalActionException, NameDuplicationException{ Vector<Parameter>
	 * params = new Vector<Parameter>(3); lowParam = new StringParameter(actor,
	 * "Low energies"); params.add(lowParam);
	 *
	 * fixParam = new StringParameter(actor, "Fix energies");
	 * params.add(fixParam);
	 *
	 * highParam = new StringParameter(actor, "High energies");
	 * params.add(highParam);
	 *
	 * energyStepParam = new StringParameter(actor, "Energy steps");
	 * params.add(energyStepParam);
	 *
	 * stepTimeParam = new StringParameter(actor, "Step times");
	 * params.add(stepTimeParam);
	 *
	 * return params; }
	 */

	/*
	 * public Vector<Parameter> createConfigParameters(Actor actor) throws
	 * IllegalActionException, NameDuplicationException{ Vector<Parameter>
	 * params = new Vector<Parameter>(3); lowParam = new StringParameter(actor,
	 * "Low energy"); params.add(lowParam);
	 *
	 * fixParam = new StringParameter(actor, "Fix energy");
	 * params.add(fixParam);
	 *
	 * highParam = new StringParameter(actor, "High energy");
	 * params.add(highParam);
	 *
	 * energyStepParam = new StringParameter(actor, "Energy step");
	 * params.add(energyStepParam);
	 *
	 * stepTimeParam = new StringParameter(actor, "Step time");
	 * params.add(stepTimeParam);
	 *
	 * return params; }
	 */

	/*
	 * public Vector<Port> createConfigPorts(Actor actor) throws
	 * IllegalActionException, NameDuplicationException{ Vector<Port> ports =
	 * new Vector<Port>(5); Port lowPort =
	 * PortFactory.getInstance().createInputPort(actor, "Low energy",
	 * Double.class); ports.add(lowPort);
	 *
	 * Port fixPort = PortFactory.getInstance().createInputPort(actor,
	 * "Fix energy", Double.class); ports.add(fixPort);
	 *
	 * Port highPort = PortFactory.getInstance().createInputPort(actor,
	 * "High energy", Double.class); ports.add(highPort);
	 *
	 * Port energyStepPort = PortFactory.getInstance().createInputPort(actor,
	 * "Energy step", Double.class); ports.add(energyStepPort);
	 *
	 * Port stepTimePort = PortFactory.getInstance().createInputPort(actor,
	 * "Step time", Double.class); ports.add(stepTimePort);
	 *
	 * return ports; }
	 */

	public void initialize() throws DevFailed {
		dev = ProxyFactory.getInstance().createDeviceProxy(deviceName);
		// see bug 22954 
		new TangoCommand(deviceName, "State").execute();

	}

	public void intializeMulti() {
		currentConfigIdx = 0;
		currentSweep = 0;
		nbConfig = modes.length;
	}

	public void configure() throws DevFailed {

		// ================write all attributes=======================
		DeviceAttribute da = new DeviceAttribute("mode");
		da.insert(mode);
		dev.write_attribute(da);

		da = new DeviceAttribute("excitationEnergy");
		da.insert(excitationEnergy);
		dev.write_attribute(da);

		da = new DeviceAttribute("energyScale");
		da.insert(energyScale);
		dev.write_attribute(da);

		if (mode.compareTo("Swept") == 0) {
			DeviceAttribute low = new DeviceAttribute("lowEnergy");
			low = dev.read_attribute("highEnergy");
			final double lowEn = low.extractDouble();
			if (lowEn > highEnergy) {
				// write low before high
				da = new DeviceAttribute("lowEnergy");
				da.insert(lowEnergy);
				dev.write_attribute(da);

				da = new DeviceAttribute("highEnergy");
				da.insert(highEnergy);
				dev.write_attribute(da);
			} else {
				// write high energy before low
				da = new DeviceAttribute("highEnergy");
				da.insert(highEnergy);
				dev.write_attribute(da);

				da = new DeviceAttribute("lowEnergy");
				da.insert(lowEnergy);
				dev.write_attribute(da);
			}
		} else {
			da = new DeviceAttribute("fixEnergy");
			da.insert(fixEnergy);
			dev.write_attribute(da);
		}

		da = new DeviceAttribute("energyStep");
		da.insert(energyStep);
		dev.write_attribute(da);

		da = new DeviceAttribute("stepTime");
		da.insert(stepTime);
		dev.write_attribute(da);

		final short[] pE = { (short) passEnergy };
		da = new DeviceAttribute("passEnergy");
		da.insert(pE);
		dev.write_attribute(da);
	}

	public boolean configureMulti(final boolean isMock) throws DevFailed {
		boolean continu = true;
		if (currentConfigIdx == nbConfig) {
			continu = false;
			currentConfigIdx = 0;
			currentSweep = 0;
		} else {
			isActive = areActive[currentConfigIdx];
			logger.debug("isActive " + isActive);
			if (isActive) {
				logger.debug("acq config :" + currentConfigIdx
						+ " and sweep " + currentSweep);
				regionName = regionNames[currentConfigIdx];
				mode = modes[currentConfigIdx];
				lensMode = lensModes[currentConfigIdx];
				energyScale = energyScales[currentConfigIdx];
				passEnergy = passEnergies[currentConfigIdx];
				lowEnergy = lowEnergies[currentConfigIdx];
				fixEnergy = fixEnergies[currentConfigIdx];
				highEnergy = highEnergies[currentConfigIdx];
				energyStep = energySteps[currentConfigIdx];
				stepTime = stepTimes[currentConfigIdx];
				sweepNumber = sweepNumbers[currentConfigIdx];
				if (currentSweep == sweepNumber - 1) {
					currentConfigIdx++;
					currentSweep = 0;
				} else {
					currentSweep++;
				}
				if (!isMock) {
					this.configure();
				}
			} else {
				System.out.println("NOT acq config :" + currentConfigIdx
						+ " and sweep " + currentSweep);
				currentConfigIdx++;
			}
		}
		return continu;
	}

	public void doAcquisition(final Actor actor) throws DevFailed,
			IllegalActionException {
		dev.command_inout("Start");
		final WaitStateTask waitTask = new WaitStateTask(deviceName, DevState.RUNNING,
				1000, true);
		waitTask.run();
		if (waitTask.hasFailed()) {
			throw waitTask.getDevFailed();
		}
		DataRecorder.getInstance().saveDevice(actor, deviceName);
		DataRecorder.getInstance().saveExperimentalData(actor, deviceName);
	}

	public void setParameter(final Attribute param)
			throws IllegalActionException {
		if (param == deviceNameParam) {
			deviceName = ((StringToken) deviceNameParam.getToken())
					.stringValue();
		}
		if (param == regionNameParam) {
			regionName = ((StringToken) regionNameParam.getToken())
					.stringValue();
		} else if (param == isActiveParam) {
			isActive = Boolean.valueOf(((StringToken) isActiveParam.getToken())
					.stringValue());
		} else if (param == modeParam) {
			mode = ((StringToken) modeParam.getToken()).stringValue();
		} else if (param == lensModeParam) {
			lensMode = ((StringToken) lensModeParam.getToken()).stringValue();
		} else if (param == energyScaleParam) {
			energyScale = ((StringToken) energyScaleParam.getToken())
					.stringValue();
		} else if (param == passEnergyParam) {
			passEnergy = Double.valueOf(((StringToken) passEnergyParam
					.getToken()).stringValue());
		} else if (param == lowParam) {
			lowEnergy = Double.valueOf(((StringToken) lowParam.getToken())
					.stringValue());
		} else if (param == fixParam) {
			fixEnergy = Double.valueOf(((StringToken) fixParam.getToken())
					.stringValue());
		} else if (param == highParam) {
			highEnergy = Double.valueOf(((StringToken) highParam.getToken())
					.stringValue());
		} else if (param == energyStepParam) {
			energyStep = Double.valueOf(((StringToken) energyStepParam
					.getToken()).stringValue());
		} else if (param == stepTimeParam) {
			stepTime = Double.valueOf(((StringToken) stepTimeParam.getToken())
					.stringValue());
		} else if (param == sweepNumberParam) {
			sweepNumber = Integer.valueOf(((StringToken) sweepNumberParam
					.getToken()).stringValue());
		}
	}

	public void setMultiParameter(final Attribute param)
			throws IllegalActionException {
		// System.out.println("**************setMultiParameter "+param.getName());
		if (param == deviceNameParam) {
			deviceName = ((StringToken) deviceNameParam.getToken())
					.stringValue();
		}
		if (param == regionNameParam) {
			regionNames = ((StringToken) regionNameParam.getToken())
					.stringValue().split(",");
		} else if (param == isActiveParam) {
			areActive = convertStringsToiBooleans(((StringToken) isActiveParam
					.getToken()).stringValue().split(","));
		} else if (param == modeParam) {
			modes = ((StringToken) modeParam.getToken()).stringValue().split(
					",");
			logger.debug("setMultiParameter -length mode " + modes.length);
			logger.debug("setMultiParameter -value "
					+ ((StringToken) modeParam.getToken()).stringValue());
		} else if (param == lensModeParam) {
			lensModes = ((StringToken) lensModeParam.getToken()).stringValue()
					.split(",");
		} else if (param == energyScaleParam) {
			energyScales = ((StringToken) energyScaleParam.getToken())
					.stringValue().split(",");
		} else if (param == passEnergyParam) {
			passEnergies = convertStringsTodoubles(((StringToken) passEnergyParam
					.getToken()).stringValue().split(","));
		} else if (param == lowParam) {
			lowEnergies = convertStringsTodoubles(((StringToken) lowParam
					.getToken()).stringValue().split(","));
		} else if (param == fixParam) {
			fixEnergies = convertStringsTodoubles(((StringToken) fixParam
					.getToken()).stringValue().split(","));
		} else if (param == highParam) {
			highEnergies = convertStringsTodoubles(((StringToken) highParam
					.getToken()).stringValue().split(","));
		} else if (param == energyStepParam) {
			energySteps = convertStringsTodoubles(((StringToken) energyStepParam
					.getToken()).stringValue().split(","));
		} else if (param == stepTimeParam) {
			stepTimes = convertStringsTodoubles(((StringToken) stepTimeParam
					.getToken()).stringValue().split(","));
		} else if (param == sweepNumberParam) {
			sweepNumbers = convertStringsToiInts(((StringToken) sweepNumberParam
					.getToken()).stringValue().split(","));
		}
	}

	private double[] convertStringsTodoubles(final String[] strings) {
		double[] result;
		result = new double[strings.length];
		for (int i = 0; i < strings.length; i++) {
			result[i] = Double.valueOf(strings[i]);
		}
		return result;
	}

	private int[] convertStringsToiInts(final String[] strings) {
		int[] result;
		result = new int[strings.length];
		for (int i = 0; i < strings.length; i++) {
			result[i] = Integer.valueOf(strings[i]);
		}
		return result;
	}

	private boolean[] convertStringsToiBooleans(final String[] strings) {
		boolean[] result;
		result = new boolean[strings.length];
		for (int i = 0; i < strings.length; i++) {
			result[i] = Boolean.valueOf(strings[i]);
		}
		return result;
	}

	public boolean isActive() {
		return isActive;
	}

	public String getRegionName() {
		return regionName;
	}

	public double getExcitationEnergy() {
		return excitationEnergy;
	}

	public void setExcitationEnergy(final double excitationEnergy) {
		this.excitationEnergy = excitationEnergy;
	}

}
