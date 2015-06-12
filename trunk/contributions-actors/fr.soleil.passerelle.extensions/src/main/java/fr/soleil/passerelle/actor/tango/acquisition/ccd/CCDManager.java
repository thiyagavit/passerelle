package fr.soleil.passerelle.actor.tango.acquisition.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.actor.Actor;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.CancellableTangoTask;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;

public class CCDManager {
    protected String deviceName = "CX/EX/CCD.1";
    private static final Logger logger = LoggerFactory.getLogger(CCDManager.class);
    /*
     * protected String acqMode = "OneShot"; protected double exposureTime = 1;
     * protected boolean useRoi = true; protected int roiXmin =0; protected int
     * roiXmax = 1300; protected int roiYmin = 0; protected int roiYmax = 399;
     * protected int numFrames = 100; protected String triggerMode = "None";
     * protected double pausingTime = 0.1; protected int xBinning = 1; protected
     * int yBinning = 1; protected boolean recordAllSequence;
     */

    protected Actor actor;
    private final CCDConfiguration config = new CCDConfiguration();
    private WaitStateTask waitTask;
    private WaitStorage waitStore;

    public CCDManager(final Actor actor, final String deviceName) {
	this.actor = actor;
	this.deviceName = deviceName;
    }

    public void configure() throws DevFailed {

	TangoAttribute attr;
	// ROI
	if (config.isUseRoi()) {
	    attr = new TangoAttribute(deviceName + "/useROI");
	    attr.write(1);
	    attr = new TangoAttribute(deviceName + "/roi1xmin");
	    attr.write(config.getRoiXmin());
	    attr = new TangoAttribute(deviceName + "/roi1xmax");
	    attr.write(config.getRoiXmax());
	    attr = new TangoAttribute(deviceName + "/roi1ymin");
	    attr.write(config.getRoiYmin());
	    attr = new TangoAttribute(deviceName + "/roi1ymax");
	    attr.write(config.getRoiYmax());
	} else {
	    attr = new TangoAttribute(deviceName + "/useROI");
	    attr.write(0);
	}

	// Binning
	attr = new TangoAttribute(deviceName + "/xbin");
	attr.write(config.getXBinning());
	attr = new TangoAttribute(deviceName + "/ybin");
	attr.write(config.getYBinning());

	attr = new TangoAttribute(deviceName + "/frames");
	attr.write(config.getNumFrames());

	attr = new TangoAttribute(deviceName + "/trigger");
	attr.write(config.getTriggerModeValue());

	// set exposure time
	/*
	 * TODO if(triggerMode.equalsIgnoreCase("ExternalPositiveEdge") ||
	 * triggerMode.equalsIgnoreCase("ExternalNegativeEdge")){ // write on
	 * device that generate hw signals attr = new
	 * AttributeCompleteHelper(attributeExposureTime);
	 * attr.write(exposureTime); }
	 */

	attr = new TangoAttribute(deviceName + "/exposure");
	attr.write(config.getExposureTime());

	// set pausing time
	/*
	 * TODO if(triggerMode.equalsIgnoreCase("ExternalPositiveEdge") ||
	 * triggerMode.equalsIgnoreCase("ExternalNegativeEdge")){ // write on
	 * device that generate hw signals attr = new
	 * AttributeCompleteHelper(attributePausingTime);
	 * attr.write(pausingTime); }
	 */

	// set time between frames for software trigger
	attr = new TangoAttribute(deviceName + "/timing");
	attr.write(config.getPausingTime());
    }

    public void updateConfigFromDevice() throws DevFailed {

	TangoAttribute attr;
	// ROI
	attr = new TangoAttribute(deviceName + "/useROI");
	if (attr.readWritten(Boolean.class)) {
	    config.setUseRoi(true);
	} else {
	    config.setUseRoi(false);
	}
	attr = new TangoAttribute(deviceName + "/roi1xmin");
	config.setRoiXmin(attr.readWritten(Integer.class));

	attr = new TangoAttribute(deviceName + "/roi1xmax");
	config.setRoiXmax(attr.readWritten(Integer.class));

	attr = new TangoAttribute(deviceName + "/roi1ymin");
	config.setRoiYmin(attr.readWritten(Integer.class));

	attr = new TangoAttribute(deviceName + "/roi1ymax");
	config.setRoiYmax(attr.readWritten(Integer.class));

	// Binning
	attr = new TangoAttribute(deviceName + "/xbin");
	config.setXBinning(attr.readWritten(Integer.class));

	attr = new TangoAttribute(deviceName + "/ybin");
	config.setYBinning(attr.readWritten(Integer.class));

	attr = new TangoAttribute(deviceName + "/frames");
	final int frames = attr.readWritten(Integer.class);
	config.setNumFrames(frames);

	attr = new TangoAttribute(deviceName + "/trigger");
	final String trigMode = config.getTriggerModeForValue(attr
		.readWritten(Integer.class));
	if (trigMode == null) {
	    config.setTriggerMode(attr.readWritten(String.class));
	} else {
	    config.setTriggerMode(trigMode);
	}

	attr = new TangoAttribute(deviceName + "/exposure");
	config.setExposureTime(attr.readWritten(Double.class));

	// set time between frames for software trigger
	attr = new TangoAttribute(deviceName + "/timing");
	config.setPausingTime(attr.readWritten(Double.class));

	attr = new TangoAttribute(deviceName + "/acqMode");
	final String mode = config.getAcqModeForValue(attr
		.readWritten(Integer.class));
	if (mode == null) {
	    config.setAcqMode(attr.readAsString("", ""));
	} else {
	    config.setAcqMode(mode);
	}
    }

    public void startStandardAcquisition() throws DevFailed,
	    IllegalActionException {
	// ExecutionTracerService.trace(actor, "starting CCD acquisition");

	// set acqMode to sequence mode
	TangoAttribute attr;
	attr = new TangoAttribute(deviceName + "/acqMode");
	attr.write(config.getAcqModeValue());
	TangoCommand cmd;

	// TODO: start trig generation for hardware trigger
	// execute sequence
	cmd = new TangoCommand(deviceName, "Start");
	cmd.execute();
    }

    public void stopAcquisition() throws DevFailed {
	final TangoCommand cmd = new TangoCommand(deviceName, "Stop");
	cmd.execute();
    }

    public void waitEndAcquisition() throws DevFailed {
//	final DeviceProxy dev = ProxyFactory.getInstance().createDeviceProxy(
//		deviceName);
	// int polling =
	// (int)Math.round(config.getExposureTime()+config.getPausingTime())/10;

	waitTask = new WaitStateTask(deviceName, DevState.RUNNING, 500, false);
	waitTask.run();
	if (waitTask.hasFailed()) {
	    throw waitTask.getDevFailed();
	}
    }

    public void cancelWaitEndAcquisition() {
	if (waitTask != null) {
	    logger.debug("cancel waitTask");
	    waitTask.cancel();
	}
	if (waitStore != null) {
	    logger.debug("cancel waitStore");
	    waitStore.cancel();
	}
	// DataRecorder.getInstance().cancel();
    }

    public void waitEndAcquisitionAndStore() throws DevFailed,
	    IllegalActionException {

	waitStore = new WaitStorage(deviceName);
	waitStore.run();
	if (waitStore.hasFailed()) {
	    throw waitStore.getDevFailed();
	}
	// if (dev.state() == DevState.FAULT) {
	// ExecutionTracerService.trace(actor,
	// "CCD acquisition finished with ERROR - device is in FAULT");
	// }

    }

    public void doStandardAcquisitionAndStore() throws DevFailed,
	    IllegalActionException {
	startStandardAcquisition();
	waitEndAcquisitionAndStore();
    }

    public void doStandardAcquisitionWithoutStore() throws DevFailed,
	    IllegalActionException {
	startStandardAcquisition();
	waitEndAcquisition();
    }

    public class WaitStorage extends CancellableTangoTask {

	private final String deviceName;

	public WaitStorage(final String deviceName) {
	    this.deviceName = deviceName;
	}

	@Override
	public void run() {

	    logger.debug("WaitStorage in");
	    hasFailed = false;
	    devFailed = null;
	    isRunning = true;
	    try {
		long currentFrameIdx = 0;
		long savedFrameIdx = 0;
		final TangoAttribute framesAttr = new TangoAttribute(deviceName
			+ "/frames");
		final TangoAttribute selectFrameAttr = new TangoAttribute(
			deviceName + "/selectFrame");

		// save contextual data
		DataRecorder.getInstance().saveDevice(actor, deviceName);

		// record all images of the sequence
		if (config.isRecordAllSequence()
			&& DataRecorder.getInstance().isSaveActive(actor)) {
		    do {
			// record all images of the sequence
			// done in parallel of the acquisition
			logger.debug("looping " + (savedFrameIdx + 1) + " "
				+ config.getNumFrames());

			logger.debug("WaitStorage cancelled is " + cancelled);
			// get progression of acquisition
			int retry = 0;
			while (retry < 4 && !cancelled) {
			    try {
				logger.debug("read frames");
				retry++;
				currentFrameIdx = framesAttr.read(Long.class);
				break;
			    } catch (final DevFailed e) {
				try {
				    Thread.sleep(3000);
				} catch (final InterruptedException e1) {
				}
				if (retry == 3) {
				    throw e;
				}
			    }
			}
			logger.debug("WaitStorage cancelled is " + cancelled);
			logger.debug("current acquired image : "
				+ currentFrameIdx);
			// save only if image has not already been saved
			if (currentFrameIdx > 0
				&& currentFrameIdx > savedFrameIdx) {
			    // select next image on device (that will be visible
			    // in
			    // attribute selected image)
			    selectFrameAttr.write(savedFrameIdx);
			    logger.debug("saving image idx: " + savedFrameIdx);
			    // save
			    DataRecorder.getInstance().saveExperimentalData(
				    actor, deviceName);
			    savedFrameIdx++;
			} else if (savedFrameIdx < config.getNumFrames()) {
			    // wait only if all acquired data is saved
			    try {
				final long delay = Math.round(config
					.getExposureTime()
					+ config.getPausingTime()) / 2;
				logger.debug("sleeping for " + delay);
				logger.debug("WaitStorage cancelled is "
					+ cancelled);
				Thread.sleep(delay);
			    } catch (final InterruptedException e1) {
			    }
			}

			// TODO: que ce passe t il si l'acq est stoppee par
			// l'utisateur?
			logger.debug("WaitStorage cancelled is " + cancelled);
		    } while (savedFrameIdx < config.getNumFrames()
			    && !cancelled);
		}

		else { // just record data once
		    waitEndAcquisition();
		    DataRecorder.getInstance().saveExperimentalData(actor,
			    deviceName);
		}
		logger.debug("WaitStorage finished");
	    } catch (final DevFailed e) {
		hasFailed = true;
		devFailed = e;
	    }
	    isRunning = false;

	}

    }

    public String getDeviceName() {
	return deviceName;
    }

    public CCDConfiguration getConfig() {
	return config;
    }

    public Actor getActor() {
	return actor;
    }

    public void setActor(final Actor actor) {
	this.actor = actor;
    }
}
