package fr.soleil.passerelle.tango.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.core.ErrorCode;

import fr.soleil.passerelle.actor.tango.acquisition.scan.ScanUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.salsa.api.SalsaAPI;
import fr.soleil.salsa.entity.IConfig;
import fr.soleil.salsa.entity.IScanStatus;
import fr.soleil.salsa.entity.ScanState;
import fr.soleil.salsa.exception.SalsaDeviceException;
import fr.soleil.salsa.exception.SalsaException;
import fr.soleil.salsa.exception.SalsaLoggingException;
import fr.soleil.salsa.exception.SalsaScanConfigurationException;

public class ScanTask implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(ScanTask.class);
    private volatile boolean cancelled = false;
    private volatile Exception exception;
    private volatile boolean hasFailed;
    private volatile boolean isRunning;

    private final IConfig<?> conf;
    private final int pollingPeriod;

    public ScanTask(final IConfig<?> config, final int poll) {

        conf = config;
        pollingPeriod = poll;
    }

    public void run() {
        final SalsaAPI currentSalsaApi = ScanUtil.getCurrentSalsaApi();
        isRunning = true;
        hasFailed = false;

        try {
            // long start = System.currentTimeMillis();
            try {
                currentSalsaApi.startScan(conf);
                logger.info("Scan is running");
            } catch (final SalsaLoggingException e) {
                e.printStackTrace();
            } catch (final SalsaDeviceException e) {
                logger.error("SalsaDeviceException : Scan not started, because {} ", e.getMessage());
                throw e;
            } catch (final SalsaScanConfigurationException e) {
                logger.error("SalsaScanConfigurationException : Scan not started, because {} ", e.getMessage());
                throw e;
            }
        } catch (final SalsaException e) {
            hasFailed = true;
            exception = e;
        }
        if (!hasFailed) {
            // long end = System.currentTimeMillis();
            // System.out.println("Duration = " + (end - start));
            try {

                ScanState currentState = currentSalsaApi.getScanState();
                while ((currentState == ScanState.RUNNING || currentState == ScanState.PAUSED) && !cancelled) {

                    try {
                        Thread.sleep(pollingPeriod);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentState = currentSalsaApi.getScanState();
                    // System.out.println("CurrentState = " + currentState);
                }

                if (currentSalsaApi.getScanState() == ScanState.ABORT) {
                    hasFailed = true;
                    final IScanStatus status = currentSalsaApi.getStatus();
                    ExceptionUtil.throwPasserelleException(ErrorCode.FATAL,
                            "The Scan has been interrupted with status: " + status.getStatus(), this);
                }

            } catch (final SalsaDeviceException e) {
                logger.error("SalsaDeviceException : Scan has been interrupted on getState, because {} ",
                        e.getMessage());
                hasFailed = true;
                exception = e;

            } catch (final Exception e) {
                hasFailed = true;
                exception = e;
            }
        }

        isRunning = false;
    }

    public void cancel() {
        cancelled = true;
        logger.debug("Scan task cancelled " + cancelled);
    }

    public boolean hasFailed() {
        return hasFailed;
    }

    public Exception getError() {
        return exception;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
