package fr.soleil.passerelle.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.utils.DevFailedUtils;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;

/**
 * 
 * This class is no more useful use directly SalsaAPI
 * See JIRA PASSERELLE-91
 *
 */
@Deprecated
public class SimpleScan {

    private String simpleScanName = "";
    private SimpleScanTask localTask = null;

    private static final Logger logger = LoggerFactory.getLogger(SimpleScan.class);

    public SimpleScan(final String simpleScanName, final String salsaConfigName) throws DevFailed {
        this.simpleScanName = simpleScanName;

        final TangoAttribute confName = new TangoAttribute(simpleScanName + "/salsaConfiguration");
        confName.write(salsaConfigName);

        logger.info("SetConfiguration : " + salsaConfigName + "on device " + simpleScanName);
    }

    public void setDataRecorderPartialMode(final boolean dataPartialMode) throws DevFailed {
        final TangoAttribute confName = new TangoAttribute(simpleScanName + "/dataRecorderPartialMode");
        confName.write(dataPartialMode);
    }

    public void startTimeScan(final int nbStepsX, final double intTime) throws DevFailed {
        if (localTask != null) {
            DevFailedUtils.throwDevFailed("Scan is already running");
        }
        String[] args = new String[2];
        args[0] = String.valueOf(nbStepsX);
        args[1] = String.valueOf(intTime);
        localTask = new SimpleScanTask(args);
        localTask.runTimeScan();
        localTask = null;
    }

    public void stopScan() throws DevFailed {
        if (localTask != null ) {
            if (localTask.isRunning()) {
                final TangoCommand command = new TangoCommand(simpleScanName, "Stop");
                command.execute();
                localTask.cancel();
            }
            localTask = null;
        }
    }

    public void pauseScan() throws DevFailed {
        final TangoCommand command = new TangoCommand(simpleScanName, "Pause");
        command.execute();
    }

    public void resumeScan() throws DevFailed {
        final TangoCommand command = new TangoCommand(simpleScanName, "Resume");
        command.execute();
    }

    private class SimpleScanTask {
        private final Object[] args;
        private final int pollingPeriod;

        private volatile boolean isRunning = false;
        private volatile boolean cancelled = false;

        public SimpleScanTask(final String[] args) {
            this.args = args;
            this.pollingPeriod = 1000;
        }

        public void runTimeScan() throws DevFailed {
            runScan();
        }

        private void runScan() throws DevFailed {
            isRunning = true;

            final TangoCommand command = new TangoCommand(simpleScanName, "TScan");
            command.execute(args);

            final TangoCommand cmdState = new TangoCommand(simpleScanName, "State");
            DevState currentState;

            do {
                    // bug 22954
                    currentState = TangoAccess.getCurrentState(cmdState);                
                    try {
                        Thread.sleep(pollingPeriod);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }

            } while ((currentState == DevState.MOVING || currentState == DevState.STANDBY) && !cancelled);
                        
            currentState = TangoAccess.getCurrentState(cmdState);
            if (currentState == DevState.ALARM) {
                final TangoCommand commandStatus = new TangoCommand(simpleScanName, "Status");
                final String logMessage = "Scan interrupted, the status is " + (String) commandStatus.executeExtract(null);
                logger.info(logMessage);
                DevFailedUtils.throwDevFailed(logMessage);                
            } 
            
            logger.info("Scan done");
            isRunning = false;
        }

        public void cancel() {
            cancelled = true;
            logger.info("Scan task cancelled requiered " + cancelled);
        }

        public boolean isRunning() {
            return isRunning;
        }
    }
}
