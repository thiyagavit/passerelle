package fr.soleil.passerelle.actor.calculation;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.TransformerV3;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.DevFailedValidationException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * Allows operation on one Image Attribue
 * 
 * @author PIERREJOSEPH
 * 
 */
@SuppressWarnings("serial")
public class AttributeImageCalculation extends TransformerV3 {

    private final static Logger logger = LoggerFactory.getLogger(AttributeImageCalculation.class);

    private static final String STORED_ATTRIBUTE_PARAM_NAME = "Stored Attribute Name ";
    /**
     * The name of the attribute to write (optional)
     */
    @ParameterName(name = STORED_ATTRIBUTE_PARAM_NAME)
    public Parameter storedAttributeNameParam;
    private String storedAttributeName = "";
    private TangoAttribute storedTangoAttribute;

    enum Operation {
        ACCUMULATE;
    }

    private static final String OPERATION_PARAM_NAME = "Operation";
    /**
     * Calculation to realize (optional)
     */
    @ParameterName(name = OPERATION_PARAM_NAME)
    public Parameter operationParam;
    private Operation operation;

    // Use to cumulate the image data : when the image come from Tango, it can
    // be exploited as a simple array
    // If optimization is required accumul should be a primitive type (double) and converted
    // with ArrayUtils.toObject(array) before using in the write_image method
    // private Double[] accumul;
    private double[] accumul;

    public AttributeImageCalculation(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setName("Image (AttributeProxy)");

        operationParam = new StringParameter(this, OPERATION_PARAM_NAME);
        operationParam.addChoice(Operation.ACCUMULATE.toString());
        operationParam.setExpression(Operation.ACCUMULATE.toString());

        storedAttributeNameParam = new StringParameter(this, STORED_ATTRIBUTE_PARAM_NAME);
        storedAttributeNameParam.setExpression(storedAttributeName);
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {

        if (arg0 == storedAttributeNameParam) {
            storedAttributeName = PasserelleUtil.getParameterValue(storedAttributeNameParam);
        }
        else if (arg0 == operationParam) {
            // final String value = PasserelleUtil.getParameterValue(operationParam);
            // if (value.equals(Operation.ACCUMULATE.toString())) {
            operation = Operation.ACCUMULATE;
            // }
        }
        else
            super.attributeChanged(arg0);
    }

    @Override
    /*
     * * storedTangoAttribute control of its validity
     */
    protected void validateInitialization() throws ValidationException {
        super.validateInitialization();
        accumul = null;
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " validateInitialization() - entry");
        }
        if (!isMockMode() && !storedAttributeName.isEmpty()) {
            try {
                storedTangoAttribute = new TangoAttribute(storedAttributeName);

                if (!storedTangoAttribute.isImage()) {
                    throw new ValidationException("Invalid Stored Attribute Format", this, null);
                }

                if (!storedTangoAttribute.isNumber() && !storedTangoAttribute.isBoolean()) {
                    throw new ValidationException("Invalid Stored Attribute Type", this, null);
                }

                if (!storedTangoAttribute.isWritable()) {
                    throw new ValidationException("Invalid Stored Attribute is not Writable", this,
                            null);
                }

            }
            catch (DevFailed e) {
                throw new DevFailedValidationException(e, this);
            }
            catch (PasserelleException e) {
                throw new ValidationException(e.getMessage(), this, e);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " validateInitialization() - exit");
        }
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request,
            final ProcessResponse response) throws ProcessingException {

        final ManagedMessage message = request.getMessage(input);
        final Object inputValue = PasserelleUtil.getInputValue(message);

        TangoAttribute attrHelp = null;
        double[] newValue = null;

        try {
            // TangoAttribute is read and it must be a Image of number
            if (inputValue instanceof TangoAttribute) {
                attrHelp = (TangoAttribute) inputValue;
            }
            else {
                throw new ProcessingExceptionWithLog(this, Severity.FATAL,
                        "the input data must be a TangoAttribute.", this, null);
            }

            // Format
            if (!attrHelp.isImage()) {
                throw new ProcessingExceptionWithLog(this, Severity.FATAL,
                        "the input data must be an Image.", this, null);
            }

            // Type
            if (!attrHelp.isNumber() && !attrHelp.isBoolean()) {
                throw new ProcessingExceptionWithLog(this, Severity.FATAL,
                        "the input data must contain numericals values.", this, null);
            }
            // Extract image value : The input TangoAttribute must contain the image value
            newValue = (double[]) attrHelp.extractArray(double.class);

        }
        catch (final DevFailed e) {
            throw new DevFailedProcessingException(e, this);
        }

        // Accumulation table initialization
        if (accumul == null) {
            accumul = new double[newValue.length];
            Arrays.fill(accumul, 0.0);
        }
        else if (newValue.length != accumul.length) {
            throw new ProcessingExceptionWithLog(this, Severity.FATAL, "Size of input must be "
                    + accumul.length, this, null);
        }

        // accumulation realization
        for (int i = 0; i < newValue.length; i++) {
            accumul[i] += newValue[i];
        }

        if (!storedAttributeName.isEmpty()) {
            try {

                int dimx = attrHelp.getDimX();
                int dimy = attrHelp.getDimY();
                // System.out.println("DimX = " + dimx + ", dimy = " + dimy + ", accumul.length" +
                // accumul.length);

                storedTangoAttribute
                        .writeImage(dimx, dimy, (Object[]) ArrayUtils.toObject(accumul));
                ExecutionTracerService.trace(this, "write Attribute " + storedAttributeName);

                // storedTangoAttribute.writeImage(dimx, dimy, accumul);
            }
            catch (DevFailed e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new DevFailedProcessingException(e, Severity.FATAL, this);
            }
        }
        ExecutionTracerService.trace(this, "Calculation is done ");
        response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, accumul));
    }
}
