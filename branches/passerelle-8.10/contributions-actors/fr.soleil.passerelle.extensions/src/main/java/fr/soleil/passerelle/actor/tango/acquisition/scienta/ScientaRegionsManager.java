package fr.soleil.passerelle.actor.tango.acquisition.scienta;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.actor.Actor;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

public class ScientaRegionsManager {

	private final static Logger logger = LoggerFactory.getLogger(ScientaRegionsManager.class);
	protected DeviceProxy dev;
	protected String deviceName = "scienta";

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(final String deviceName) {
		this.deviceName = deviceName;
	}

	private int currentSweep = 0;
	protected double initialExcitationEnergy = 0;
	protected double excitationEnergy = 0;
	protected boolean isActive = true;

	private List<ScientaRegion> regions = new ArrayList<ScientaRegion>();;

	/*
	 * protected String regionName = "Region1"; protected String[] regionNames =
	 * {"Region1"};
	 *
	 * protected String lensMode = "Transmission"; protected String[] lensModes
	 * = {"Transmission"};
	 *
	 * protected double passEnergy = 2; protected double[] passEnergies = {2};
	 *
	 * protected double initialExcitationEnergy = 0; protected double
	 * excitationEnergy = 0; protected double[] excitationEnergies= {0};
	 *
	 * protected String mode = "Fixed"; protected String[] modes= {"Fixed"};
	 *
	 * protected String energyScale = "Kinetic"; protected String[]
	 * energyScales= {"Kinetic"};
	 *
	 * protected double lowEnergy = 0; protected double[] lowEnergies= {0};
	 *
	 * protected double fixEnergy = 1; protected double[] fixEnergies= {1};
	 *
	 * protected double highEnergy = 1; protected double[] highEnergies= {1};
	 *
	 * protected double energyStep = 0.1; protected double[] energySteps= {0.1};
	 *
	 * protected double stepTime = 1; protected double[] stepTimes= {1};
	 *
	 *
	 * protected int sweepNumber = 1; protected int[] sweepNumbers= {1}; private
	 * int currentSweep = 0;
	 *
	 * protected boolean isActive = true; protected boolean[]areActive = {true};
	 */

	// private ScientaRegionManager scientaManager = null;
	private int currentConfigIdx = 0;
	private int nbConfig = 0;

	private ScientaRegion currentScientaRegion;

	public ScientaRegionsManager() {
	}

	public void initialize() throws DevFailed {
		dev = ProxyFactory.getInstance().createDeviceProxy(deviceName);
		// see bug 22954 : The deviceProxy is still created here because the 
                // daughter classes need of it
		// dev.ping();
		new TangoCommand(deviceName, "State").execute();
	}

	public void intializeMulti() {
		currentConfigIdx = 0;
		currentSweep = 0;
		nbConfig = regions.size();
	}

	/**
	 * Configure Tango Access
	 *
	 * @param scientaRegion
	 * @throws DevFailed
	 */
	public void configure(final ScientaRegion scientaRegion) throws DevFailed {

		if (scientaRegion == null) {
			return;
		}

		// ================write all attributes=======================
		DeviceAttribute da = new DeviceAttribute("mode");
		da.insert(scientaRegion.getAcqMode());
		dev.write_attribute(da);

		da = new DeviceAttribute("lensMode");
		da.insert(scientaRegion.getLensMode());
		dev.write_attribute(da);

		da = new DeviceAttribute("excitationEnergy");
		da.insert(excitationEnergy);
		dev.write_attribute(da);

		da = new DeviceAttribute("energyScale");
		da.insert(scientaRegion.getKinBin());
		dev.write_attribute(da);

		if (scientaRegion.getAcqMode().compareTo("Swept") == 0) {
			DeviceAttribute low = new DeviceAttribute("lowEnergy");
			low = dev.read_attribute("highEnergy");
			final double lowEn = low.extractDouble();
			if (lowEn > scientaRegion.getHigh()) {
				// write low before high
				da = new DeviceAttribute("lowEnergy");
				da.insert(scientaRegion.getLow());
				dev.write_attribute(da);

				da = new DeviceAttribute("highEnergy");
				da.insert(scientaRegion.getHigh());
				dev.write_attribute(da);
			} else {
				// write high energy before low
				da = new DeviceAttribute("highEnergy");
				da.insert(scientaRegion.getHigh());
				dev.write_attribute(da);

				da = new DeviceAttribute("lowEnergy");
				da.insert(scientaRegion.getLow());
				dev.write_attribute(da);
			}
		} else {
			da = new DeviceAttribute("fixEnergy");
			da.insert(scientaRegion.getFix());
			dev.write_attribute(da);
		}
		da = new DeviceAttribute("energyStep");
		da.insert(scientaRegion.getEnergyStep());
		dev.write_attribute(da);

		da = new DeviceAttribute("stepTime");
		da.insert(scientaRegion.getStepTime());
		dev.write_attribute(da);

		final double temp = Double.valueOf(scientaRegion.getPassEnergy());
		final short[] pE = { (short) temp };
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
			// we test if the ScientaRegion is active before launching the step
			// TODO V�rifier cette partie
			final ScientaRegion currentScienta = regions.get(currentConfigIdx);
			isActive = currentScienta.isActive();
			if (isActive) {
				if (currentSweep == currentScienta.getNumberSweeps() - 1) {
					currentConfigIdx++;
					currentSweep = 0;
				} else {
					currentSweep++;
				}

				logger.debug("acq config :" + currentConfigIdx
						+ " and sweep " + currentSweep);
				// the acquisition region is dependent of the first excitation
				// energy and the current excitation energy
				final double diffE = excitationEnergy - initialExcitationEnergy;
				logger.debug("excitationEnergy: " + excitationEnergy);
				logger.debug("initialExcitationEnergy: "
						+ initialExcitationEnergy);
				logger.debug("diffE: " + diffE);
				final ScientaRegion realRegion = (ScientaRegion) currentScienta
						.clone();
				if (realRegion.getAcqMode().compareToIgnoreCase("Swept") == 0) {
					final double currentLowE = realRegion.getLow();
					realRegion.setLow(currentLowE + diffE);
					final double currentHighE = realRegion.getHigh();
					realRegion.setHigh(currentHighE + diffE);
					logger.debug("currentLowE: " + currentLowE);
					logger.debug("currentHighE: " + currentHighE);
				} else {
					final double currentFixE = realRegion.getFix();
					realRegion.setFix(currentFixE + diffE);
				}
				currentScientaRegion = realRegion;
				if (!isMock) {
					this.configure(currentScientaRegion);
				} else {
					logger.debug("Mock mode.  Acq Config :"
							+ currentConfigIdx + " and sweep " + currentSweep);
					logger.debug(currentScienta.toString());
				}
			} else {
				logger.debug("NOT acq config :" + currentConfigIdx
						+ " and sweep " + currentSweep);
				currentConfigIdx++;
			}
		}
		return continu;
	}

	public boolean isActive() {
		return isActive;
	}

	public void doAcquisition(final Actor actor, final String symbolName)
			throws DevFailed, IllegalActionException {
		// DataRecorder.setNxEntryNameAndSaveContext(actor,
		// currentScientaRegion.getRegionName());
		DataRecorder.getInstance().setSymbol(actor, symbolName,
				currentScientaRegion.getRegionName());
		dev.command_inout("Start");
		final WaitStateTask waitTask = new WaitStateTask(deviceName, DevState.RUNNING,
				1000, false);
		waitTask.run();
		if (waitTask.hasFailed()) {
			throw waitTask.getDevFailed();
		}

		DataRecorder.getInstance().saveDevice(actor, deviceName);
		DataRecorder.getInstance().saveExperimentalData(actor, deviceName);
	}

	public double getExcitationEnergy() {
		return excitationEnergy;
	}

	public void setExcitationEnergy(final double excitationEnergy) {
		this.excitationEnergy = excitationEnergy;
	}

	public double getInitialExcitationEnergy() {
		return initialExcitationEnergy;
	}

	public void setInitialExcitationEnergy(final double initialExcitationEnergy) {
		this.initialExcitationEnergy = initialExcitationEnergy;
	}

	public ScientaRegion getCurrentScientaRegion() {
		return currentScientaRegion;
	}

	public List<ScientaRegion> getRegions() {
		return regions;
	}

	public void setRegions(final List<ScientaRegion> regions) {
		this.regions = regions;
	}

	public void addScientaRegion(final ScientaRegion scientaRegion) {
		if (regions != null) {
			regions.add(scientaRegion);
		}
	}

	public void removeStep(final ScientaRegion scientaRegion) {
		if (regions != null) {
			regions.remove(scientaRegion);
		}
	}

	public void removeStepAt(final int index) {
		if (regions != null) {
			if (index <= regions.size() - 1) {
				regions.remove(index);
			}
		}
	}

}
