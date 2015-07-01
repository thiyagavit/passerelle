/*
 * Synchrotron Soleil
 * 
 * File : AttributeWriterValueIn.java
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
 * Log: AttributeWriterValueIn.java,v
 */
package fr.soleil.passerelle.actor.tango.basic;

import java.util.Arrays;

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

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoAttributeActor;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.CancellableTangoTask;
import fr.soleil.passerelle.tango.util.WaitAttributeTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * Write a Tango attribute, read back it and output the value. Option to check
 * that the read part has reached the write part.
 * 
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class AttributeWriter extends ATangoAttributeActor {

    private static final String TIMEOUT = "Timeout";
    private static final String TOLERANCE = "Tolerance";
    private static final String WAIT_READ_PART_EQUALS_WRITE_PART = "Wait read part equals write part";
    private static final String SEPARATOR = "Separator";
    private final static Logger logger = LoggerFactory.getLogger(AttributeWriter.class);

    /**
     * Select it if the actor has to wait for the read part to reach to write
     * part
     */
    @ParameterName(name = WAIT_READ_PART_EQUALS_WRITE_PART)
    public Parameter waitReadPartParam;
    private boolean waitReadPart;
    
    @ParameterName(name = SEPARATOR)
    public Parameter separatorParam;
    private String separator;

    /**
     * The absolute tolerance (only use with param
     * "Wait read part equals write part")
     */
    @ParameterName(name = TOLERANCE)
    public Parameter toleranceParam;
    private double tolerance;

    /**
     * The absolute tolerance (only use with param
     * "Wait read part equals write part")
     */
    @ParameterName(name = TIMEOUT)
    public Parameter timeoutParam;
    private double timeout;

    double readValue;
    double writtenValue;

    private CancellableTangoTask waitTask;

    // /** The input ports */
    // public Port inputTrigger;
    //
    // /** The output ports */
    // public Port outputAttribute;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public AttributeWriter(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        // inputTrigger = PortFactory.getInstance().createInputPort(this,
        // "Value (Scalar only)", null);
        // outputAttribute =
        // PortFactory.getInstance().createOutputPort(this,"Attribute Written");

        input.setName("Value (Scalar only)");
        input.setExpectedMessageContentType(String.class);
        output.setName("Attribute Written");

        waitReadPartParam = new Parameter(this, WAIT_READ_PART_EQUALS_WRITE_PART, new BooleanToken(false));
        waitReadPartParam.setTypeEquals(BaseType.BOOLEAN);

        toleranceParam = new StringParameter(this, TOLERANCE);
        toleranceParam.setExpression("0.5");
        registerConfigurableParameter(toleranceParam);
        
        separatorParam = new StringParameter(this, SEPARATOR);
        separatorParam.setExpression(",");

        timeoutParam = new StringParameter(this, TIMEOUT);
        timeoutParam.setExpression("10");
        registerConfigurableParameter(timeoutParam);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute
     * )
     */
    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == toleranceParam) {
            tolerance = PasserelleUtil.getParameterDoubleValue(toleranceParam);
        } 
        if (arg0 == separatorParam) {
            separator = PasserelleUtil.getParameterValue(separatorParam);
        } else if (arg0 == timeoutParam) {
            timeout = PasserelleUtil.getParameterDoubleValue(timeoutParam);
        } else if (arg0 == waitReadPartParam) {
            waitReadPart = PasserelleUtil.getParameterBooleanValue(waitReadPartParam);
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
            final String input = (String) PasserelleUtil.getInputValue(message);
            ExecutionTracerService.trace(this, "MOCK - writing attribute " + getAttributeName());
            // sendOutputMsg(output, PasserelleUtil.createContentMessage(this,
            // input));
            response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, input));
        } else {
            try {
                final TangoAttribute attr = getTangoAttribute();
                final AttrDataFormat dataFormat = attr.getAttributeProxy().get_info().data_format;
                final Object obj = PasserelleUtil.getInputValue(message);
                if (dataFormat.equals(AttrDataFormat.SPECTRUM)) {
                    Object[] table;
                    if (obj.getClass().isArray()) {
                        table = (String[]) obj;
                    } else {
                        table = ((String) obj).split(separator);
                    }
                    ExecutionTracerService.trace(this, "writing attribute " + getAttributeName() + " with value: "
                            + Arrays.toString(table));
                    attr.writeSpectrum(table);
                } else if (dataFormat.equals(AttrDataFormat.SCALAR)) {
                    ExecutionTracerService.trace(this, "writing attribute " + getAttributeName() + " with value: "
                            + obj);
                    attr.write(obj.toString());

                } else {
                    // TODO: gerer ecriture
                    ExceptionUtil.throwProcessingException("Cannot write on image attributes", getAttributeName());
                }

                if (attr.getAttributeProxy().get_info().writable != AttrWriteType.WRITE) {
                    if (waitReadPart) {
                        ExecutionTracerService.trace(this, "waiting attribute " + getAttributeName() + " equals " + obj
                                + "+-" + tolerance);
                        waitTask = new WaitAttributeTask(attr, tolerance, timeout, 1000, null, false);
                        waitTask.run();
                        if (waitTask.hasFailed()) {
                            throw waitTask.getDevFailed();
                        }
                        ExecutionTracerService.trace(this,
                                "attribute " + getAttributeName() + " has been to " + attr.readAsString(" ", ""));
                    }

                }

                // save data if necessary
                if (isRecordData()) {
                    DataRecorder.getInstance().saveDevice(this, attr.getDeviceName());
                }
                // sendOutputMsg(output,
                // PasserelleUtil.createContentMessage(this,
                // attr));
                attr.update();
                response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, attr));

            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            } catch (final PasserelleException e) {
                ExceptionUtil.throwProcessingException(e.getMessage(), getAttributeName(), e);
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
    protected String getExtendedInfo() {
        return this.getName();
    }

}
