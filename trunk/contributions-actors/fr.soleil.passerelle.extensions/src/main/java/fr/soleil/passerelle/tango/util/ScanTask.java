package fr.soleil.passerelle.tango.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleException.Severity;
import fr.soleil.passerelle.actor.tango.acquisition.scan.ScanUtil;
import fr.soleil.salsa.api.SalsaAPI;
import fr.soleil.salsa.entity.IConfig;
import fr.soleil.salsa.entity.ScanState;
import fr.soleil.salsa.exception.SalsaException;
import fr.soleil.salsa.exception.SalsaLoggingException;

public class ScanTask implements Runnable {

	private final static Logger logger = LoggerFactory
			.getLogger(ScanTask.class);
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
			} catch (final SalsaLoggingException e) {
				e.printStackTrace();
			} catch (final Exception e) {
				throw e;
			}
			// long end = System.currentTimeMillis();
			// System.out.println("Duration = " + (end - start));

			ScanState currentState = currentSalsaApi.getScanState();
			while ((currentState == ScanState.RUNNING || currentState == ScanState.PAUSED)
					&& !cancelled) {

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
				exception = new PasserelleException(Severity.FATAL,
						"The Scan has been interrupted", null, null);
			}

		} catch (final SalsaException e) {
			hasFailed = true;
			exception = e;
		} catch (final Exception e) {
			hasFailed = true;
			exception = e;
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
