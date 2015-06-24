package fr.soleil.passerelle.tango.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;
import fr.soleil.passerelle.actor.flow.ComparatorHelper;
import fr.soleil.passerelle.actor.flow.ComparatorHelper.ComparisonNature;
import fr.soleil.tango.clientapi.TangoAttribute;

public class WaitAttributeTask extends CancellableTangoTask {

    private final static Logger logger = LoggerFactory.getLogger(WaitAttributeTask.class);

    final TangoAttribute attribute;
    final double tolerance;
    final double timeout;
    final int pollingPeriod;
    final String value;
    private final boolean waitForValue;

    public WaitAttributeTask(final TangoAttribute attribute,
	    final double tolerance, final double timeout,
	    final int pollingPeriod, final String value,
	    final boolean waitForValue) {
	this.attribute = attribute;
	this.tolerance = tolerance;
	this.timeout = timeout;
	this.pollingPeriod = pollingPeriod;
	this.value = value;
	this.waitForValue = waitForValue;
    }

    @Override
    public void run() {

	hasFailed = false;
	devFailed = null;
	boolean wait = true;
	double waitTime = 0;
	String readValue;
	try {
	    do {
		try {
		    Thread.sleep(pollingPeriod);
		} catch (final InterruptedException e) {
		}
		waitTime = waitTime + 1;
		readValue = attribute.read(String.class);
		boolean comparisonOK = false;
		if (waitForValue) {
		    comparisonOK = ComparatorHelper.compare(readValue, value,
			    ComparisonNature.EQ, tolerance);
		} else {
		    final String valueWrite = attribute
			    .readWritten(String.class);
		    if (readValue == null || valueWrite == null) {
			// impossible to compare since there no both parts
			break;
		    }
		    // wait for read part is equals to write part +- tolerance
		    comparisonOK = ComparatorHelper.compare(readValue,
			    valueWrite, ComparisonNature.EQ, tolerance);
		}
		wait = !comparisonOK && waitTime <= timeout;
	    } while (wait && !cancelled);

	    if (waitTime >= timeout) {
		Except
			.throw_exception(
				"TIMEOUT",
				"the attribute had never reach the written value",
				"fr.soleil.passerelle.util.TangoUtil.waitReadAttributeEqualsWrite");
	    }
	} catch (final DevFailed e) {
	    hasFailed = true;
	    devFailed = e;
	}
	logger.debug("WaitAttributeTask finished");
    }

}
