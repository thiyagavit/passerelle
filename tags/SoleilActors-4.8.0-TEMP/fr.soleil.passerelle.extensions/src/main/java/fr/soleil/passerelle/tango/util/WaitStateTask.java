package fr.soleil.passerelle.tango.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Except;
import fr.soleil.tango.clientapi.TangoCommand;

/**
 * Task to wait for a tango device to either: <br>
 * - do a transition to the desired state <br>
 * - get out of the desired state
 * 
 * @author ABEILLE
 * 
 */
public class WaitStateTask extends CancellableTangoTask {
    private static final Logger logger = LoggerFactory.getLogger(WaitStateTask.class);
    private final String devName;
    private final DevState state;
    private final int pollingPeriod;
    private final boolean waitForState;
    private double timeOut = -1;
    private String errorMsg;
    private String originErrorMsg;

    /**
     * 
     * @param dev
     * @param state
     *            - if waitForState is true, the waiting state <br>
     *            - else, The state to get out
     * @param pollingPeriod
     *            The polling period in ms
     * @param waitForState
     *            - If true, will wait for the device to be in state equals to
     *            param state. <br>
     *            - If false, will wait for the device to be out of param state
     */
    public WaitStateTask(final String devName, final DevState state, final int pollingPeriod,
            final boolean waitForState) {
        this.devName = devName;
        this.state = state;
        this.pollingPeriod = pollingPeriod;
        this.waitForState = waitForState;
    }

    /**
     * 
     * @param dev
     * @param state
     *            - if waitForState is true, the waiting state <br>
     *            - else, The state to get out
     * @param pollingPeriod
     *            The polling period in ms
     * @param waitForState
     *            - If true, will wait for the device to be in state equals to
     *            param state. <br>
     *            - If false, will wait for the device to be out of param state
     * @param timeOut
     *            The absolute tolerance in s.
     * @param errorMsg
     *            Error message has to show whether timeOut is exceeded
     * @param originErrorMsg
     *            origin of error whether timeOut is exceeded.
     */
    public WaitStateTask(final String devName, final DevState state, final int pollingPeriod,
            final boolean waitForState, final double timeOut, final String errorMsg,
            final String orginErrorMsg) {
        
        this(devName,state, pollingPeriod, waitForState);

        // convert timeOut in ms
        this.timeOut = timeOut * 1000;
        this.errorMsg = errorMsg;
        this.originErrorMsg = orginErrorMsg;
    }

    @Override
    public void run() {
        isRunning = true;
        DevState currentState;
        devFailed = null;
        hasFailed = false;
        double waitTime = 0;

        boolean stateCondition = true;
        try {                       
            final TangoCommand cmd = new TangoCommand(this.devName, "State");

            do {
                try {
                    // bug 22954
                    currentState = TangoAccess.getCurrentState(cmd);                
                } catch (final DevFailed e1) {
                    devFailed = e1;
                    hasFailed = true;
                    e1.printStackTrace();
                    return;
                }
                stateCondition = waitForState ? !currentState.equals(state) : currentState
                        .equals(state);
                if (!stateCondition || cancelled) {
                    break;
                }
                try {
                    Thread.sleep(pollingPeriod);
                } catch (final InterruptedException e) {
                    return;
                }

                // the test is divided in two part to not increment infinitely
                // waitTime if timeOut is <0.
                if (timeOut > 0) {
                    waitTime += pollingPeriod;

                    if (waitTime > timeOut) {
                        Except.throw_exception("TIMEOUT", errorMsg, originErrorMsg);
                    }
                }

            } while (true);
        } catch (final DevFailed e) {
            hasFailed = true;
            devFailed = e;
        }
        logger.debug("WaitForStateTask finished");
        isRunning = false;
    }
}
