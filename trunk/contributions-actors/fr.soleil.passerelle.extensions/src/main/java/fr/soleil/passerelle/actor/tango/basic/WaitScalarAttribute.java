/*
 * Synchrotron Soleil
 * 
 * File : WaitAttribute.java
 * 
 * Project : passerelle-soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 26 mai 2005
 * 
 * Revision: Author:
 * Date: State:
 * 
 * Log: WaitAttribute.java,v
 */
package fr.soleil.passerelle.actor.tango.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoAttributeActor;
import fr.soleil.passerelle.tango.util.CancellableTangoTask;
import fr.soleil.passerelle.tango.util.WaitAttributeTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * Wait for an attribute to reach a value. This value can be :<br>
 * <ul>
 * <li>The written part (set point)</li>
 * <li>A value specified by user</li>
 * </ul>
 * 
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class WaitScalarAttribute extends ATangoAttributeActor {

    private static final String WAIT_READ_PART_EQUALS_TO = "Wait read part equals to";

    private static final String WAIT_READ_PART_EQUALS_WRITE_PART = "Wait read part equals write part";

    private static final String TIMEOUT = "Timeout";

    private static final String TOLERANCE = "Tolerance";

    private final static Logger logger = LoggerFactory.getLogger(WaitScalarAttribute.class);

    /**
     * Check it to wait for the write part to be equals to the read part
     */
    @ParameterName(name = WAIT_READ_PART_EQUALS_WRITE_PART)
    public Parameter waitReadPartParam;
    private boolean waitReadPart;

    /**
     * If not waiting for write part, the value to wait
     */
    @ParameterName(name = WAIT_READ_PART_EQUALS_TO)
    public Parameter waitValueParam;
    private String waitValue;

    /**
     * The absolute tolerance.
     */
    @ParameterName(name = TOLERANCE)
    public Parameter toleranceParam;
    private double tolerance;

    /**
     * The absolute tolerance.
     */
    @ParameterName(name = TIMEOUT)
    public Parameter timeoutParam;
    private double timeout;

    private CancellableTangoTask waitTask;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public WaitScalarAttribute(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        // inputTrigger = PortFactory.getInstance().createInputPort(this,
        // "Trigger", null);
        // outputAttribute =
        // PortFactory.getInstance().createOutputPort(this,"Attribute");

        input.setName("Trigger");
        output.setName("Attribute");

        toleranceParam = new StringParameter(this, TOLERANCE);
        toleranceParam.setExpression("0.5");

        timeoutParam = new StringParameter(this, TIMEOUT);
        timeoutParam.setExpression("10");

        waitReadPartParam = new Parameter(this, WAIT_READ_PART_EQUALS_WRITE_PART, new BooleanToken(true));
        waitReadPartParam.setTypeEquals(BaseType.BOOLEAN);

        waitValueParam = new StringParameter(this, WAIT_READ_PART_EQUALS_TO);
        waitValueParam.setExpression("1");

    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == toleranceParam) {
            tolerance = PasserelleUtil.getParameterDoubleValue(toleranceParam);
        } else if (arg0 == timeoutParam) {
            timeout = PasserelleUtil.getParameterDoubleValue(timeoutParam);
        } else if (arg0 == waitReadPartParam) {
            waitReadPart = PasserelleUtil.getParameterBooleanValue(waitReadPartParam);
        } else if (arg0 == waitValueParam) {
            waitValue = PasserelleUtil.getParameterValue(waitValueParam);
        } else {
            super.attributeChanged(arg0);
        }

    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - entry");
        }
        if (isMockMode()) {
            if (waitReadPart) {
                ExecutionTracerService
                        .trace(this, "MOCK - waiting for " + getAttributeName() + " equals to write part");
            } else {
                ExecutionTracerService.trace(this, "MOCK - waiting for " + getAttributeName() + " equals to "
                        + waitValue);
            }
            // sendOutputMsg(output, PasserelleUtil
            // .createContentMessage(this, 5.0));
            response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, 5.0));

        } else {
            try {
                final TangoAttribute attr = getTangoAttribute();
                if (waitReadPart) {
                    ExecutionTracerService.trace(this, "waiting for " + getAttributeName() + " equals to write part");
                    waitTask = new WaitAttributeTask(attr, tolerance, timeout, 1000, null, false);
                } else {
                    ExecutionTracerService.trace(this, "waiting for " + getAttributeName() + " equals to " + waitValue);
                    waitTask = new WaitAttributeTask(attr, tolerance, timeout, 1000, waitValue, true);

                }
                waitTask.run();
                if (waitTask.hasFailed()) {
                    throw waitTask.getDevFailed();
                }
                ExecutionTracerService.trace(this, "value reached");
                // output attribute
                // sendOutputMsg(output,
                // PasserelleUtil.createContentMessage(this,
                // attr));
                attr.update();
                response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, attr));

            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            } catch (final PasserelleException e) {
                ExceptionUtil.throwProcessingException(e.getMessage(), this, e);
            }
        }
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
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
        final WaitScalarAttribute copy = (WaitScalarAttribute) super.clone(workspace);
        copy.waitTask = null;
        return copy;
    }

    @Override
    protected String getExtendedInfo() {
        return getAttributeName();
    }

}
