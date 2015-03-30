package fr.soleil.passerelle.recording;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.TangoConst;
import fr.soleil.passerelle.domain.RecordingDirector;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;

public class DataRecorder {

    private static final Logger logger = LoggerFactory.getLogger(DataRecorder.class);
    private static boolean firstRecord = false;
    private static boolean asyncMode = false;
    private static DataRecorder instance = new DataRecorder();
    private static WaitStateTask waitTask;
    private static boolean cancel = false;

    private boolean startRecording = false;
    
    private DataRecorder() {

    }

    public static DataRecorder getInstance() {
        return instance;
    }

    public synchronized void setAsyncMode(final String dataRecorderName, final boolean asyncMode) throws DevFailed {
        final TangoAttribute att = new TangoAttribute(dataRecorderName + "/asynchronousWrite");
        // PASSERELLE-79 : conversion is not useful
        /*double val = 0;
        if (asyncMode) {
            val = 1;
        }
        att.write(val);*/
        att.write(asyncMode);
        DataRecorder.asyncMode = asyncMode;
    }

    private void waitEndMoving(final DeviceProxy proxy) throws DevFailed {
        waitTask = new WaitStateTask(proxy.get_name(), DevState.MOVING, 100, false);
        waitTask.run();
        if (waitTask.hasFailed()) {
            throw waitTask.getDevFailed();
        }
    }

    public synchronized void startSession() {
        cancel = false;
    }

    public synchronized void cancel() {
        if (waitTask != null) {
            logger.debug("cancelling waiting for DR");
            waitTask.cancel();
        }
        cancel = true;
    }

    public synchronized void saveDevice(final Actor actor, final String deviceName) throws DevFailed {
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            if (isRecordingStarted(actor)) {
                final boolean autoChangeNX = ((RecordingDirector) actor.getDirector()).isAutoChangeNxEntry();
                if (autoChangeNX) {
                    incNxEntryNameAndSaveContext(actor);
                }
                final String dataRecorderName = ((RecordingDirector) actor.getDirector()).getDataRecorderName();
                ExecutionTracerService.trace(actor, "saving device " + deviceName + " on datarecorder "
                        + dataRecorderName);
                final TangoCommand command = new TangoCommand(dataRecorderName, "WriteTangoDeviceData");
                command.execute(deviceName);
                if (DataRecorder.asyncMode) {
                    // Bug 22954
                    final TangoCommand commandState = new TangoCommand(dataRecorderName, "State");
                    logger.debug("DR state :"
                            + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                    waitEndMoving(command.getDeviceProxy());
                    logger.debug("DR state :"
                            + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                }
            }
        }
    }

    public synchronized void saveExperimentalData(final Actor actor, final String deviceName) throws DevFailed {
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            if (isRecordingStarted(actor)) {
                final String dataRecorderName = ((RecordingDirector) actor.getDirector()).getDataRecorderName();
                ExecutionTracerService.trace(actor, "saving experimental data for " + deviceName + " on datarecorder "
                        + dataRecorderName);
                final TangoCommand command = new TangoCommand(dataRecorderName, "WriteExperimentalData");

                command.execute(deviceName);
                if (DataRecorder.asyncMode) {
                    // Bug 22954
                    final TangoCommand commandState = new TangoCommand(dataRecorderName, "State");
                    logger.debug("DR state :"
                            + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                    waitEndMoving(command.getDeviceProxy());
                    logger.debug("DR state :"
                            + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                }
            }
        }
    }

    /**
     * does not exist anymore on datarecorder
     * 
     * @param actor
     * @param scanDeviceName
     * @param scan1DDeviceName
     * @param scan2DDeviceName
     * @throws DevFailed
     */
    @Deprecated
    public synchronized void saveScan(final Actor actor, final String scanDeviceName, final String scan1DDeviceName,
            final String scan2DDeviceName) throws DevFailed {
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            if (isRecordingStarted(actor)) {
                final String dataRecorderName = ((RecordingDirector) actor.getDirector()).getDataRecorderName();
                ExecutionTracerService.trace(actor, "saving scan data on datarecorder " + dataRecorderName);
                final TangoCommand command = new TangoCommand(dataRecorderName, "WriteScanData");

                final Object[] devicesNames = { scanDeviceName, scan1DDeviceName, scan2DDeviceName };
                command.execute(devicesNames);
                if (DataRecorder.asyncMode) {
                    waitEndMoving(command.getDeviceProxy());
                }
                logger.debug("saved scan data");
            }
        }
    }

    public synchronized void setSymbol(final Actor actor, final String symbolName, final String symbolValue)
            throws DevFailed {
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            if (isRecordingStarted(actor)) {
                final String dataRecorderName = ((RecordingDirector) actor.getDirector()).getDataRecorderName();
                logger.debug("Setting symbol " + symbolName + " on " + dataRecorderName);
                final TangoCommand command = new TangoCommand(dataRecorderName, "SetSymbol");
                final Object[] argin = { symbolName, symbolValue };
                command.execute(argin);
            }
        }
    }

    public synchronized void startRecording(final Actor actor) throws DevFailed {
        firstRecord = true;
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            final String deviceName = ((RecordingDirector) actor.getDirector()).getDataRecorderName();
            final TangoCommand comHelp = new TangoCommand(deviceName, "StartRecording");
            comHelp.execute();
            startRecording = true;
        } else {
            ExecutionTracerService.trace(actor, "WARNING - there is no Recording director");
        }
    }

    public synchronized boolean isStartRecording() {
        return startRecording;
    }

    public synchronized void endRecording(final String dataRecorderName) throws DevFailed {
        if (isRecordingStarted(dataRecorderName)) {
            logger.debug("EndRecording is called");
            final TangoCommand comHelp = new TangoCommand(dataRecorderName, "EndRecording");
            comHelp.execute();            
        }
        startRecording = false;
    }

    public synchronized void endRecording(final Actor actor) throws DevFailed {
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            if (isRecordingStarted(actor)) {
                final String deviceName = ((RecordingDirector) actor.getDirector()).getDataRecorderName();
                final TangoCommand comHelp = new TangoCommand(deviceName, "EndRecording");
                comHelp.execute();
            }
        }
    }

    public synchronized void incNxEntryNameAndSaveContext(final Actor actor) throws DevFailed {
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            if (isRecordingStarted(actor)) {
                final String dataRecorderName = ((RecordingDirector) actor.getDirector()).getDataRecorderName();
                final TangoCommand commandState = new TangoCommand(dataRecorderName, "State");
                /*
                 * ExecutionTracerService.trace(actor, "incrementing experiment
                 * and acquisition indexes");
                 */
                logger.debug("incrementing experiment and acquisition indexes");
                if (!firstRecord) {
                    // write post technical data after the first loop
                    final TangoCommand commandWritePostTechnicalData = new TangoCommand(dataRecorderName,
                            "WritePostTechnicalData");
                    commandWritePostTechnicalData.execute();
                    if (DataRecorder.asyncMode) {
                        // Bug 22954
                        logger.debug("DR state :"
                                + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);

                        waitEndMoving(commandWritePostTechnicalData.getDeviceProxy());
                        logger.debug("DR state :"
                                + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                    }
                } else {
                    firstRecord = false;
                }

                // increment indexes
                final TangoCommand commandIncAcq = new TangoCommand(dataRecorderName, "IncAcquisitionIndex");
                commandIncAcq.execute();

                final TangoCommand commandExpAcq = new TangoCommand(dataRecorderName, "IncExperimentIndex");
                commandExpAcq.execute();

                // write user data
                // System.out.println("user data ");
                final TangoCommand commandWriteUserData = new TangoCommand(dataRecorderName, "WriteUserData");
                commandWriteUserData.execute();
                if (DataRecorder.asyncMode) {
                    waitEndMoving(commandWriteUserData.getDeviceProxy());
                }

                // write pre technical data
                // System.out.println("write pre technical data");
                final TangoCommand commandWritePreTechnicalData = new TangoCommand(dataRecorderName,
                        "WritePreTechnicalData");
                commandWritePreTechnicalData.execute();
                if (DataRecorder.asyncMode) {
                    logger.debug("DR state :"
                            + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                    waitEndMoving(commandWritePreTechnicalData.getDeviceProxy());
                    logger.debug("DR state :"
                            + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                }
            }
        }
    }

    public synchronized void setNxEntryNameAndSaveContext(final Actor actor, final String suffix) throws DevFailed {
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            if (isRecordingStarted(actor)) {
                final String dataRecorderName = ((RecordingDirector) actor.getDirector()).getDataRecorderName();
                final TangoCommand commandState = new TangoCommand(dataRecorderName, "State");
                /*
                 * ExecutionTracerService.trace(actor, "incrementing experiment
                 * and acquisition indexes");
                 */

                if (!firstRecord) {
                    // write post technical data after the first loop
                    logger.debug("write post technical data");
                    final TangoCommand commandWritePostTechnicalData = new TangoCommand(dataRecorderName,
                            "WritePostTechnicalData");
                    commandWritePostTechnicalData.execute();
                    if (DataRecorder.asyncMode) {
                        logger.debug("DR state :"
                                + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                        waitEndMoving(commandWritePostTechnicalData.getDeviceProxy());
                        logger.debug("DR state :"
                                + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                    }
                } else {
                    firstRecord = false;
                }

                if (cancel) {
                    cancel = false;
                    return;
                }

                // increment indexes
                logger.debug("incrementing experiment and acquisition indexes");
                final TangoCommand commandIncAcq = new TangoCommand(dataRecorderName, "IncAcquisitionIndex");
                commandIncAcq.execute();

                final TangoCommand commandExpAcq = new TangoCommand(dataRecorderName, "IncExperimentIndex");
                commandExpAcq.execute();

                // change acquisition name
                final TangoAttribute acqName = new TangoAttribute(dataRecorderName + "/acquisitionName");
                acqName.write(suffix);

                if (cancel) {
                    cancel = false;
                    return;
                }

                // write user data
                logger.debug("user data ");
                final TangoCommand commandWriteUserData = new TangoCommand(dataRecorderName, "WriteUserData");
                commandWriteUserData.execute();
                if (DataRecorder.asyncMode) {
                    waitEndMoving(commandWriteUserData.getDeviceProxy());
                }

                if (cancel) {
                    cancel = false;
                    return;
                }

                // write pre technical data
                logger.debug("write pre technical data");
                final TangoCommand commandWritePreTechnicalData = new TangoCommand(dataRecorderName,
                        "WritePreTechnicalData");
                commandWritePreTechnicalData.execute();
                if (DataRecorder.asyncMode) {
                    logger.debug("DR state :"
                            + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                    waitEndMoving(commandWritePreTechnicalData.getDeviceProxy());
                    logger.debug("DR state :"
                            + TangoConst.Tango_DevStateName[TangoAccess.getCurrentState(commandState).value()]);
                }
            }
        }
    }

    public synchronized void savePostContext(final Actor actor) throws DevFailed {
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            if (isRecordingStarted(actor)) {
                // nxEntryCounter++;
                final String dataRecorderName = ((RecordingDirector) actor.getDirector()).getDataRecorderName();
                // write post technical data after the first loop
                final TangoCommand commandWritePostTechnicalData = new TangoCommand(dataRecorderName,
                        "WritePostTechnicalData");
                commandWritePostTechnicalData.execute();
                if (DataRecorder.asyncMode) {
                    waitEndMoving(commandWritePostTechnicalData.getDeviceProxy());
                }

            }
        }
    }

    public synchronized boolean isRecordingStarted(final String dataRecorderName) throws DevFailed {
        // PASSERELLE-78
        /*boolean result = false;        
        final DeviceProxy dev = ProxyFactory.getInstance().createDeviceProxy(
        	dataRecorderName);
        if (dev != null) {
        if (TangoAccess.isCurrentStateEqualStateRequired(dataRecorderName, DevState.ON)) {
            result = false;
        } else {
            result = true;
        }
        }
        return result;
        */
        return !TangoAccess.isCurrentStateEqualStateRequired(dataRecorderName, DevState.ON);
        
    }

    public synchronized boolean isRecordingStarted(final Actor actor) throws DevFailed {
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            final String dataRecorderName = ((RecordingDirector) actor.getDirector()).getDataRecorderName();

            // bug 22954
            if (TangoAccess.isCurrentStateEqualStateRequired(dataRecorderName, DevState.ON)) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    public synchronized boolean isSaveActive(final Actor actor) {
        final Director dir = actor.getDirector();
        if (dir instanceof RecordingDirector) {
            return true;
        } else {
            return false;
        }
    }

}
