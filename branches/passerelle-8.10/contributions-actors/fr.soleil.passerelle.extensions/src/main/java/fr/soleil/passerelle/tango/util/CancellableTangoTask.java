package fr.soleil.passerelle.tango.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.Tango.DevFailed;

public abstract class CancellableTangoTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CancellableTangoTask.class);
    protected volatile boolean cancelled = false;
    protected volatile DevFailed devFailed;
    protected volatile boolean hasFailed;
    protected volatile boolean isRunning;

    public void cancel() {
        cancelled = true;
        logger.debug("task cancelled " + cancelled);
    }

    public boolean hasCancelled() {
        return cancelled;
    }

    public DevFailed getDevFailed() {
        return devFailed;
    }

    public boolean hasFailed() {
        return hasFailed;
    }

    public boolean isRunning() {
        return isRunning;
    }

}
