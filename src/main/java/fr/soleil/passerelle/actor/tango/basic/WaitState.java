/*
 * Synchrotron Soleil
 * 
 * File : WaitState.java
 * 
 * Project : passerelle-soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 27 mai 2005
 * 
 * Revision: Author:
 * Date: State:
 * 
 * Log: WaitState.java,v
 */
/*
 * Created on 27 mai 2005
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.tango.basic;

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
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.StateUtilities;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * Wait for the transition to the wanted state on a Tango device.
 * 
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class WaitState extends ATangoDeviceActor {

    private static final String STATE_TO_WAIT = "State to wait";
    private static final String TIMEOUT = "Time out(s)";

    private final static Logger logger = LoggerFactory.getLogger(WaitState.class);

    /**
     * The wanted state.
     */
    @ParameterName(name = STATE_TO_WAIT)
    public Parameter stateParam;
    private String state;

    /**
     * The absolute tolerance.
     */
    @ParameterName(name = TIMEOUT)
    public Parameter timeOutParam;
    private Double timeOut;

    private DevState waitingState;
    private WaitStateTask waitTask;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public WaitState(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        // input = PortFactory.getInstance().createInputPort(this, "Input",
        // null);
        // outputCopy =
        // PortFactory.getInstance().createOutputPort(this,"Input copy");

        output.setName("input copy");

        stateParam = new StringParameter(this, STATE_TO_WAIT);
        for (final DeviceState stateName : DeviceState.values()) {
            stateParam.addChoice(stateName.toString());
        }
        stateParam.setExpression(DeviceState.STANDBY.toString());
        waitingState = DeviceState.STANDBY.getDevState();

        timeOutParam = new StringParameter(this, TIMEOUT);
        timeOutParam.setExpression("-1");
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

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        final ManagedMessage message = request.getMessage(input);

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - entry");
        }

        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - waiting state: " + DeviceState.toString(waitingState) + " on "
                    + getDeviceName());
            ExecutionTracerService.trace(this, "MOCK - waiting state: " + DeviceState.toString(waitingState)
                    + " finished");
        } else {
            try {
                ExecutionTracerService.trace(this, "waiting for state: " + DeviceState.toString(waitingState) + " on "
                        + getDeviceName());

                waitTask = new WaitStateTask(getDeviceName(), waitingState, 1000, true, timeOut,
                        "the device never change it state to " + StateUtilities.getNameForState(waitingState),
                        "WaitState");
                waitTask.run();
                if (waitTask.hasFailed()) {
                    throw waitTask.getDevFailed();
                }
                ExecutionTracerService
                        .trace(this, "waiting state: " + DeviceState.toString(waitingState) + " finished");
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        // sendOutputMsg(output, PasserelleUtil.createCopyMessage(this,
        // message));
        response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, message));

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - exit");
        }
    }

    @Override
    protected void doStop() {
        if (waitTask != null) {
            waitTask.cancel();
        }
        super.doStop();
    }

    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }

    @Override
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
        final WaitState copy = (WaitState) super.clone(workspace);
        copy.waitTask = null;
        copy.waitingState = null;
        return copy;
    }

}
