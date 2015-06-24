package fr.soleil.passerelle.tango.util;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.DeviceState;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.Group.Group;
import fr.esrf.TangoApi.Group.GroupCmdReply;
import fr.esrf.TangoApi.Group.GroupCmdReplyList;
import fr.esrf.TangoDs.Except;

public class WaitStateOnGroupTask extends CancellableTangoTask {

    private final static Logger logger = LoggerFactory.getLogger(WaitStateOnGroupTask.class);

    private final DevState state;
    private final int pollingPeriod;
    private final Group group;

    private String originErrorMsg;
    private String errorMsg;
    private double timeOut = -1;

    public WaitStateOnGroupTask(final Group group, final DevState state, final int pollingPeriod) {
        this.group = group;
        this.state = state;
        this.pollingPeriod = pollingPeriod;
    }

    /**
     * construct a new task which will wait all devices passed in parameter
     * "group" will be in a defined state
     * 
     * @param group
     *            of devices
     * @param state
     *            will wait for all devices to be in this state.
     * @param pollingPeriod
     *            The polling period in ms
     * @param timeOut
     *            The absolute tolerance.
     * @param errorMsg
     *            Error message has to show whether timeOut is exceeded
     * @param originErrorMsg
     *            origin of error whether timeOut is exceeded.
     */
    public WaitStateOnGroupTask(final Group group, final DevState state, final int pollingPeriod, final double timeOut,
            final String errorMsg, final String originErrorMsg) {
        this.group = group;
        this.state = state;
        this.pollingPeriod = pollingPeriod;

        // convert timeOut in ms
        this.timeOut = timeOut * 1000;
        this.errorMsg = errorMsg;
        this.originErrorMsg = originErrorMsg;
    }

    public void run() {
        isRunning = true;
        devFailed = null;
        hasFailed = false;
        boolean allStatesReached = false;
        double waitTime = 0;
        try {
            do {
                try {
                    final GroupCmdReplyList replyList = group.command_inout("State", true);
                    if (replyList.has_failed()) {
                        Except.throw_exception("TANGO_COMMUNICATION_ERROR", "impossible to execute State",
                                "TangoUtil.waitForStateOnGroup");
                    }
                    final Iterator<?> iterator = replyList.iterator();
                    boolean continueTest = true;
                    while (iterator.hasNext() && continueTest == true && !cancelled) {
                        final GroupCmdReply reply = (GroupCmdReply) iterator.next();
                        final DeviceData data = reply.get_data();
                        final DevState currentState = data.extractDevState();
                        logger.debug("state for " + reply.dev_name() + " is " + DeviceState.toString(currentState));
                        if (!state.equals(currentState)) {
                            continueTest = false;
                            allStatesReached = false;
                        } else {
                            allStatesReached = true;
                        }
                    }
                } catch (final DevFailed e) {
                    hasFailed = true;
                    devFailed = e;
                    break;
                }
                try {
                    Thread.sleep(pollingPeriod);
                } catch (final InterruptedException e) {
                    // ignore
                }

                // the test is divided in two part to not increment infinitely
                // waitTime if timeOut is <0.
                if (timeOut > 0) {
                    waitTime += pollingPeriod;
                    if (waitTime > timeOut) {
                        Except.throw_exception("TIMEOUT", errorMsg, originErrorMsg);
                    }
                }
            } while (!allStatesReached && !cancelled);
        } catch (final DevFailed e) {
            hasFailed = true;
            devFailed = e;
        }
        isRunning = false;
        logger.debug("End run");
    }

}
