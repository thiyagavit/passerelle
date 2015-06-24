/**
 *
 */
package fr.soleil.passerelle.actor.tango.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.DeviceState;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.Group.Group;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.tango.util.CancellableTangoTask;
import fr.soleil.passerelle.tango.util.FilterHelper;
import fr.soleil.passerelle.tango.util.WaitStateOnGroupTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

/**
 * Will wait for a specified Tango State on a list of devices.
 * 
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class FilteredWaitState extends ATangoDeviceActor {

    private static final String TIMEOUT = "Time out(s)";
    private static final String STATE_TO_WAIT = "State to wait";
    // private static final String DEVICE_NAME = "Device Name";
    private final static Logger logger = LoggerFactory.getLogger(FilteredWaitState.class);

    /**
     * The tango state that this actor will wait for.
     */
    @ParameterName(name = STATE_TO_WAIT)
    public Parameter stateParam;

    // /**
    // * The Tango device names with filter, * and ? allowed.
    // */
    // @ParameterName(name = DEVICE_NAME)
    // public Parameter deviceNameParam;
    // String deviceName;
    private String state;

    private DevState waitingState;
    private Group devGroupProxy;
    private CancellableTangoTask waitState;
    String[] deviceList;

    /**
     * The absolute tolerance.
     */
    @ParameterName(name = TIMEOUT)
    public Parameter timeOutParam;
    private double timeOut;

    /**
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public FilteredWaitState(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        output.setName("input copy");
        setCreateDeviceProxy(false);
        // deviceNameParam = new StringParameter(this, DEVICE_NAME);
        // deviceNameParam.setExpression("*");

        stateParam = new StringParameter(this, STATE_TO_WAIT);
        for (final DeviceState stateName : DeviceState.values()) {
            stateParam.addChoice(stateName.toString());
        }
        stateParam.setExpression(DeviceState.STANDBY.toString());
        waitingState = DeviceState.STANDBY.getDevState();

        recordDataParam.setVisibility(Settable.EXPERT);

        timeOutParam = new StringParameter(this, TIMEOUT);
        timeOutParam.setExpression("-1");
    }

    @Override
    protected void doInitialize() throws InitializationException {

        logger.debug(getName() + " doInitialize() - entry");

        if (!isMockMode()) {
            try {
                final String param = getDeviceName();
                String[] tempDevices;
                if (param.contains(",")) {
                    tempDevices = param.split(",");
                } else {
                    tempDevices = new String[1];
                    tempDevices[0] = param;
                }
                final List<String> devices = new ArrayList<String>();
                for (final String tempDevice : tempDevices) {
                    final String[] temp = FilterHelper.getDevicesForPatternAsArray(tempDevice);
                    devices.addAll(Arrays.asList(temp));
                }
                logger.debug("monitoring devices " + devices);
                deviceList = devices.toArray(new String[] {});
                devGroupProxy = ProxyFactory.getInstance().createGroup("waitStates", deviceList);
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
        logger.debug(getName() + " doInitialize() - exit");

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.isencia.passerelle.actor.Transformer#doFire(com.isencia.passerelle.
     * message.ManagedMessage)
     */
    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        logger.debug(getName() + " doFire() - entry");

        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - waiting state: " + DeviceState.toString(waitingState));
            ExecutionTracerService.trace(this, "MOCK - waiting state: " + DeviceState.toString(waitingState)
                    + " finished");
        } else {
            try {
                ExecutionTracerService.trace(this, "waiting state: " + DeviceState.toString(waitingState) + " on :");
                for (final String deviceName : deviceList) {
                    ExecutionTracerService.trace(this, "\t - " + deviceName);
                }
                waitState = new WaitStateOnGroupTask(devGroupProxy, waitingState, 1000, timeOut,
                        "All devices nerver change their state to " + DeviceState.toString(waitingState),
                        "FilteredWaitState");
                waitState.run();
                if (waitState.hasFailed()) {
                    throw waitState.getDevFailed();
                }
                ExecutionTracerService
                        .trace(this, "waiting state: " + DeviceState.toString(waitingState) + " finished");
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());
    }

    @Override
    protected void doStop() {
        if (waitState != null) {
            logger.debug("doStop - cancel waiting");
            waitState.cancel();
        }
        super.doStop();
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == stateParam) {
            state = ((StringToken) stateParam.getToken()).stringValue();
            waitingState = DeviceState.toDevState(state);
        } else if (arg0 == timeOutParam) {
            timeOut = PasserelleUtil.getParameterDoubleValue(timeOutParam);
        } else {
            super.attributeChanged(arg0);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
     */
    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
