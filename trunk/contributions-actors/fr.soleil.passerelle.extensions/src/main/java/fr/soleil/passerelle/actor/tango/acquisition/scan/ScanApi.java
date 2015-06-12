package fr.soleil.passerelle.actor.tango.acquisition.scan;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.prefs.Preferences;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.esrf.tangoatk.core.Device;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.salsa.SalsaFactory;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.salsa.model.SalsaModel;
import fr.soleil.salsa.model.ScanServer;
import fr.soleil.salsa.model.scanconfig.Dimension;
import fr.soleil.salsa.model.scanconfig.LinearRange;
import fr.soleil.salsa.model.scanconfig.LinearTrajectory;
import fr.soleil.salsa.model.scanconfig.Range;
import fr.soleil.salsa.model.scanconfig.ScanConfiguration;
import fr.soleil.salsa.model.scanmanagement.ScanManager;
import fr.soleil.tango.clientapi.TangoCommand;

public class ScanApi {

    protected ScanServer scanServer;
    protected SalsaModel model;
    protected ScanConfiguration config = null;
    protected Device dev;
    private final Actor actor;
    private WaitStateTask waitTask;
    private String scanRepository;
    public static final String PARAM_SCANMANAGER_REPOSITORY = "salsa.scanmanager.repository";

    public ScanApi(final Actor actor) {
        this.actor = actor;
    }

    public ScanConfiguration getConfig() {
        return config;
    }

    public void setConfig(final ScanConfiguration config) throws IllegalArgumentException {
        this.config = config;
        try {
            model = SalsaFactory.getInstance().createSalsaModel();
        } catch (final ConnectionException e) {
            throw new IllegalArgumentException(e);
        } catch (final DevFailed e) {
            throw new IllegalArgumentException(e);
        }
        config.setScanNumber(1);
        scanServer = model.getScanServer();
        // TODO : gestion timeout deviceproxy avec Proxyfactory
        dev = scanServer.getDevice();
        // TODO: en attente getDevice retourne une exception!
        if (dev == null) {
            throw new IllegalArgumentException("Cannot connect to scan server");
        }
        this.configureDR();
    }

    public String getScanRepository() throws InitializationException {
        // try {
        // model = SalsaFactory.getInstance().createSalsaModel();
        // } catch (final ConnectionException e) {
        // throw new InitializationException("Cannot create salsa model",
        // null, e);
        // } catch (final DevFailed e) {
        // throw new DevFailedInitializationException(e, actor);
        // }
        // scanRepository = model.getScanManager().getScanRepository();
        // Load preferences
        // FIXME: hach that should be removed as soon as it is in salsa
        final Preferences prefs = Preferences.userNodeForPackage(SalsaModel.class);
        scanRepository = prefs.get(PARAM_SCANMANAGER_REPOSITORY, "not initialized");

        return scanRepository;
    }

    public void loadScanFile(final File file) throws InitializationException {
        try {
            model = SalsaFactory.getInstance().createSalsaModel();
        } catch (final ConnectionException e) {
            ExceptionUtil.throwInitializationException("Cannot create salsa model", file, e);
        } catch (final DevFailed e) {
            ExceptionUtil.throwInitializationException(actor, e);
        }
        try {
            // load the config from the salsa file
            config = SalsaFactory.getInstance().loadConfig(file);
        } catch (final IOException e1) {
            ExceptionUtil.throwInitializationException("Cannot load salsa config", model, e1);
        }
        config.setScanNumber(1);
        scanServer = model.getScanServer();
        // TODO : gestion timeout deviceproxy avec Proxyfactory
        dev = scanServer.getDevice();
        // TODO: en attente getDevice retourne une exception!
        if (dev == null) {
            ExceptionUtil.throwInitializationException("Cannot connect to scan server", file);
        }
        this.configureDR();
    }

    /**
     * Set ranges for both dim X and dim Y (only one actuator per dimension)
     * 
     * @param rangesXList
     * @param rangesYList
     */
    public void changeTrajectory(final Map<Integer, ScanRangeX> rangesXList, final Map<Integer, ScanRangeY> rangesYList) {

        if (config.getDimensions().size() >= 1) {
            // first dimension X
            final Dimension dim = config.getDimensions().getDimension(0);
            for (final Map.Entry<Integer, ScanRangeX> config : rangesXList.entrySet()) {
                Range salsaRange;
                ScanRangeX configRange;
                if (dim.getRanges().size() < config.getKey()) {
                    salsaRange = new LinearRange();
                    dim.getRanges().addRange(salsaRange);
                } else {
                    salsaRange = dim.getRanges().getRange(config.getKey() - 1);
                }
                configRange = config.getValue();
                if (configRange.isStepNumerInit()) {
                    salsaRange.setStepNumber(configRange.getNumberOfSteps());
                }
                if (configRange.isIntegrationTimeInit()) {
                    ((LinearRange) salsaRange).setIntegrationTime(configRange.getIntegrationTime());
                }
                // we suppose that there is only one actuator
                final LinearTrajectory traj = (LinearTrajectory) dim.getTrajectory(dim.getActuators().getActuator(0),
                        salsaRange);
                if (configRange.isRelative()) {
                    traj.setRelative(configRange.isRelative());
                }
                if (configRange.isFromInit()) {
                    traj.setOriginPosition(configRange.getFrom());
                }
                if (configRange.isToInit()) {
                    traj.setEndPosition(configRange.getTo());
                }
            }
        }

        if (config.getDimensions().size() >= 2) {
            // second dimension Y
            final Dimension dimY = config.getDimensions().getDimension(1);
            for (final Map.Entry<Integer, ScanRangeY> config : rangesYList.entrySet()) {
                Range salsaRange;
                ScanRangeY configRange;
                if (dimY.getRanges().size() < config.getKey()) {
                    salsaRange = new LinearRange();
                    dimY.getRanges().addRange(salsaRange);
                } else {
                    salsaRange = dimY.getRanges().getRange(config.getKey() - 1);
                }
                configRange = config.getValue();
                if (configRange.isStepNumerInit()) {
                    salsaRange.setStepNumber(configRange.getNumberOfSteps());
                }
                // we suppose that there is only one actuator
                final LinearTrajectory traj = (LinearTrajectory) dimY.getTrajectory(dimY.getActuators().getActuator(0),
                        salsaRange);
                if (configRange.isRelative()) {
                    traj.setRelative(configRange.isRelative());
                }
                if (configRange.isFromInit()) {
                    traj.setOriginPosition(configRange.getFrom());
                }
                if (configRange.isToInit()) {
                    traj.setEndPosition(configRange.getTo());
                }
            }
        }
    }

    public void change1DTrajectory(final double from, final double to, final int stepNr, final double integrationTime,
            final boolean relative) {
        final Dimension dim = config.getDimensions().getDimension(0);
        final Range range = dim.getRanges().getRange(0);
        range.setStepNumber(stepNr);
        ((LinearRange) range).setIntegrationTime(integrationTime);
        final LinearTrajectory traj = (LinearTrajectory) dim.getTrajectory(dim.getActuators().getActuator(0), range);
        // we suppose that there is only one actuator
        traj.setRelative(relative);
        traj.setOriginPosition(from);
        traj.setEndPosition(to);
    }

    public void change2DTrajectory(final double xFrom, final double xTo, final int xStepNr, final boolean xRelative,
            final double yFrom, final double yTo, final int yStepNr, final boolean yRelative,
            final double integrationTime) {
        // first dimension
        final Dimension dim = config.getDimensions().getDimension(0);
        final Range range = dim.getRanges().getRange(0);
        range.setStepNumber(xStepNr);
        ((LinearRange) range).setIntegrationTime(integrationTime);
        final LinearTrajectory traj = (LinearTrajectory) dim.getTrajectory(dim.getActuators().getActuator(0), range);
        // we suppose that there is only one actuator
        traj.setRelative(xRelative);
        traj.setOriginPosition(xFrom);
        traj.setEndPosition(xTo);
        // second dimension
        final Dimension dimY = config.getDimensions().getDimension(1);
        final Range rangeY = dimY.getRanges().getRange(0);
        rangeY.setStepNumber(yStepNr);
        final LinearTrajectory trajY = (LinearTrajectory) dimY
                .getTrajectory(dimY.getActuators().getActuator(0), rangeY);
        // we suppose that there is only one actuator
        trajY.setRelative(yRelative);
        trajY.setOriginPosition(yFrom);
        trajY.setEndPosition(yTo);
    }

    public boolean is1DScan() {
        if (config.getDimensions().size() == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void scan() throws ProcessingException {
        try {
            model.getScanManager().setCurrentScan(config, false);
            scanServer.start();
            // TangoUtil.waitEndState(dev, DevState.MOVING, 1000);
            boolean isScanning = true;
            final String deviceName = dev.get_name();
            final TangoCommand cmd = new TangoCommand(deviceName, "State");
            DevState currentState;
            while (isScanning) {
                waitTask = new WaitStateTask(deviceName, DevState.MOVING, 1000, false);
                waitTask.run();
                if (waitTask.hasFailed()) {
                    throw waitTask.getDevFailed();
                }
                // is the scan paused? so wait end pause
                currentState = (DevState) cmd.executeExtract(null);
                if (currentState.equals(DevState.STANDBY)) {
                    waitTask = new WaitStateTask(deviceName, DevState.STANDBY, 1000, false);
                    waitTask.run();
                    if (waitTask.hasFailed()) {
                        throw waitTask.getDevFailed();
                    }
                } else {
                    isScanning = false;
                }
            }
            // Check state of scan server. If fault, something's wrong.
            currentState = (DevState) cmd.executeExtract(null);
            if (currentState == DevState.FAULT) {
                final String runName = model.getScanManager().getScanServer().getRunName();
                if (runName.equals("")) {
                    ExecutionTracerService.trace(actor, "This an known bug in Salsa, cannot do anything :(");
                }
                ExceptionUtil.throwProcessingException(dev.status(), dev);
            }
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(actor, e);
        } catch (final ConnectionException ce) {
            ExceptionUtil.throwProcessingException(ce.getMessage(), dev, ce);
        }
    }

    public void cancelWaitEndScan() {
        if (waitTask != null) {
            waitTask.cancel();
        }
    }

    public void stop() throws DevFailed {
        if (dev != null) {
            // final DevState state = dev.state();
            // if (state != null && state == DevState.MOVING) {
            if (TangoAccess.isCurrentStateEqualStateRequired(dev.get_name(), DevState.MOVING)) {
                scanServer.abort();
                ExecutionTracerService.trace(actor, "Scan aborted");
            }
        }
    }

    private void configureDR() {
        if (DataRecorder.getInstance().isSaveActive(actor)) {
            scanServer.setRecordingSessionManaged(false);
            // System.out.println("setRecordingSessionManaged(false");
        } else {
            scanServer.setRecordingSessionManaged(true);
            // System.out.println("setRecordingSessionManaged(true");
        }
    }

    public String getSalsaExtension() {
        return ScanManager.SALSA_EXTENSION;
    }
}
