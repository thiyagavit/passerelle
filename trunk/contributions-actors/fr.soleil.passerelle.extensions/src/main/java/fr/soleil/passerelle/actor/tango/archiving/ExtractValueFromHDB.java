package fr.soleil.passerelle.actor.tango.archiving;

import static fr.soleil.passerelle.actor.tango.archiving.HdbExtractorProxy.DATE_FORMAT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.Level;
import com.isencia.passerelle.util.ptolemy.DateTimeParameter;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoActorV5;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class ExtractValueFromHDB extends ATangoActorV5 {

    /**
     * the label of parameter {@link fr.soleil.passerelle.actor.tango.archiving.ExtractValueFromHDB.dateParam}
     */
    public static final String DATE_PARAM_NAME = "timestamp";

    /**
     * the label of the parameter
     * {@link fr.soleil.passerelle.actor.tango.archiving.ExtractValueFromHDB.completeAttributeNameParam}
     */
    public static final String COMPLETE_ATTR_NAME = "Complete attribute Name";

    /**
     * the label of the parameter
     * {@link fr.soleil.passerelle.actor.tango.archiving.ExtractValueFromHDB.extractionTypeParam}
     */
    public static final String EXTRACTION_TYPE = "Extraction type";

    /**
     * the label of the parameter
     * {@link fr.soleil.passerelle.actor.tango.archiving.ExtractValueFromHDB.throwExceptionOnErrorParam}
     */
    public static final String THROW_EXCEPTION_ON_ERROR = "Throw exception On Error";

    /**
     * the error message when complete attribute name is empty
     */
    public static final String ERROR_COMPLETE_ATTR_NAME_IS_EMPTY = COMPLETE_ATTR_NAME + " must not be empty";

    /**
     * the Complete Attribute Name to extract
     */
    @ParameterName(name = COMPLETE_ATTR_NAME)
    public Parameter completeAttributeNameParam;
    private String completeAttributeName = "domain/family/member/attribute_name";

    /**
     * the nearest date of the value to extract (only needed when ExtractionType is Nearest)
     */
    @ParameterName(name = DATE_PARAM_NAME)
    public DateTimeParameter dateParam;
    private Date date;

    /**
     * the way to extract the attribute value. Can only be:
     * <ul>
     * <li>lasted value</li>
     * <li>nearest value</li>
     * </ul>
     * 
     */
    @ParameterName(name = EXTRACTION_TYPE)
    public Parameter extractionTypeParam;
    private ExtractionType extractionType;

    /**
     * flag that indicate how manage error happening during extraction. If it's true then an
     * exception is raise, otherwise an empty message is send to output port
     */
    @ParameterName(name = THROW_EXCEPTION_ON_ERROR)
    public Parameter throwExceptionOnErrorParam;
    private boolean throwExceptionOnError = true;

    /**
     * the proxy use to communicate with the HDB extraction device
     */
    private HdbExtractorProxy extractorProxy;

    public ExtractValueFromHDB(CompositeEntity container, String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        completeAttributeNameParam = new StringParameter(this, COMPLETE_ATTR_NAME);
        completeAttributeNameParam.setExpression(completeAttributeName);

        extractionTypeParam = new StringParameter(this, EXTRACTION_TYPE);
        for (final ExtractionType extractionType : ExtractionType.values()) {
            extractionTypeParam.addChoice(extractionType.getDescription());
        }
        extractionTypeParam.setExpression(ExtractionType.LASTED.getDescription());

        throwExceptionOnErrorParam = new Parameter(this, THROW_EXCEPTION_ON_ERROR, new BooleanToken(
                throwExceptionOnError));
        throwExceptionOnErrorParam.setTypeEquals(BaseType.BOOLEAN);

        dateParam = new DateTimeParameter(this, DATE_PARAM_NAME, HdbExtractorProxy.DATE_FORMAT);
        dateParam.setExpression(HdbExtractorProxy.DATE_FORMAT.format(new Date()));

    }

    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {

        if (attribute == completeAttributeNameParam) {
            completeAttributeName = extractAndValidateCompleteAttrName();

        } else if (attribute == dateParam) {
            date = extractAndValidateDate();

        } else if (attribute == extractionTypeParam) {
            extractionType = extractAndValidateExtrationType();

        } else if (attribute == throwExceptionOnErrorParam) {
            throwExceptionOnError = ((BooleanToken) throwExceptionOnErrorParam.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * extract the date from the parameter and ensure that match the date format
     * 
     * @return the date as an Object
     * @throws IllegalActionException is thrown if the date format is invalid
     */
    private Date extractAndValidateDate() throws IllegalActionException {
        Date result;
        result = dateParam.getDateValue();

        // if the date format is not valid the method dateParam.getDateValue() returns null
        if (result == null)
            throw new IllegalActionException(this, "date must be filled");
        return result;
    }

    /**
     * extract the ExtractionType from the parameter and ensure its valid
     * 
     * @return the ExtractionType as an Enum instance
     * @throws IllegalActionException is thrown if the ExtractionType is unknown
     */
    private ExtractionType extractAndValidateExtrationType() throws IllegalActionException {
        // throw an IllegalActionException if extraction is unknown
        return ExtractionType.fromDescription(PasserelleUtil.getParameterValue(extractionTypeParam).trim());
    }

    /**
     * extract the complete attribute name and ensure is not empty
     * 
     * @return the complete attribute name
     * @throws IllegalActionException is thrown if he complete attribute name is empty
     */
    private String extractAndValidateCompleteAttrName() throws IllegalActionException {
        String completeAttrName = PasserelleUtil.getParameterValue(completeAttributeNameParam);
        if (completeAttrName.isEmpty())
            throw new IllegalActionException(ERROR_COMPLETE_ATTR_NAME_IS_EMPTY);
        return completeAttrName;
    }

    @Override
    protected void validateInitialization() throws ValidationException {

        try {
            completeAttributeName = extractAndValidateCompleteAttrName();
            date = extractAndValidateDate();
            extractionType = extractAndValidateExtrationType();
            if (extractorProxy == null) {
                extractorProxy = new HdbExtractorProxy();
                ExecutionTracerService.trace(this, "using hdb Extractor " + extractorProxy.getHdbExtractorName(),
                        Level.DEBUG);
            }
        } catch (DevFailed e) {
            ExceptionUtil.throwValidationException(this, e);
        } catch (IllegalActionException e) {
            throw new ValidationException(ErrorCode.FLOW_VALIDATION_ERROR, e.getMessage(), this, e);
        }
        super.validateInitialization();
    }

    @Override
    protected void process(ActorContext context, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        switch (extractionType) {
            case LASTED:
                extractLastedValue(context, request, response);
                break;

            case NEAREST:
                extractNearestValue(context, request, response);
                break;

            default: // should not happen
                ExceptionUtil.throwProcessingExceptionWithLog(this, ErrorCode.FATAL, "Unknown extraction type \""
                        + extractionType.getDescription() + "\"", this);

        }
    }

    /**
     * extract the nearest value from the hdb and send it on output port
     * 
     * if an error occurred during extraction there are 2 possibilities
     * <ul>
     * <li>throwExceptionOnError = false ==> an exception is raised</li>
     * <li>throwExceptionOnError = true ==> an empty message is sent on output port</li>
     * </ul>
     * 
     * @param provided by process method see it for more details
     * @param provided by process method see it for more details
     * @param provided by process method see it for more details
     * @throws ProcessingException is thrown if an error occurred during extraction and
     *             throwExceptionOnError = false
     */
    private void extractNearestValue(ActorContext context, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        try {
            String formatedDate = DATE_FORMAT.format(date);
            String extractedValue = extractorProxy.getNearestScalarAttrValue(completeAttributeName, formatedDate);
            ExecutionTracerService.trace(this, "The nearest value of " + completeAttributeName + " at " + formatedDate
                    + " is \"" + extractedValue + "\"");
            sendOutputMsg(output, PasserelleUtil.createContentMessage(this, extractedValue));
        } catch (DevFailed devFailed) {
            if (throwExceptionOnError) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, completeAttributeName
                        + " is not in Hdb or can not be read: ", context, devFailed);
            } else {
                sendOutputMsg(output, PasserelleUtil.createContentMessage(this, ""));
            }
        }

    }

    /**
     * extract the lasted value from the hdb and send it on output port
     * 
     * if an error occurred during extraction there are 2 possibilities
     * <ul>
     * <li>throwExceptionOnError = false ==> an exception is raised</li>
     * <li>throwExceptionOnError = true ==> an empty message is sent on output port</li>
     * </ul>
     * 
     * @param provided by process method see it for more details
     * @param provided by process method see it for more details
     * @param provided by process method see it for more details
     * @throws ProcessingException is thrown if an error occurred during extraction and
     *             throwExceptionOnError = false
     */
    private void extractLastedValue(ActorContext context, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        try {
            // TODO c manage alias ???

            String extractedValue = extractorProxy.getLastScalarAttrValue(completeAttributeName);

            ExecutionTracerService.trace(this, "The lasted value of " + completeAttributeName + " is \""
                    + extractedValue + "\"");

            sendOutputMsg(output, PasserelleUtil.createContentMessage(this, extractedValue));

        } catch (DevFailed devFailed) {
            if (throwExceptionOnError) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, completeAttributeName
                        + " cannot be extracted from hdb:", context, devFailed);
            } else {
                sendOutputMsg(output, PasserelleUtil.createContentMessage(this, ""));
            }
        }

    }

    /**
     * this method is only used in test to use an mock of HdbExtractorProxy
     * 
     * @param extractorProxy the proxy use to extract value from hdb
     */
    public void setExtractorProxy(HdbExtractorProxy extractorProxy) {
        this.extractorProxy = extractorProxy;
    }

    /**
     * this ennum contains the different extraction strategies of hdb values
     * 
     * @author gramer
     * 
     */
    public static enum ExtractionType {
        /**
         * the Lasted Value contains in hdb
         */
        LASTED("Lasted Value"),
        /**
         * Nearest Value from a specify date contains in hdb
         */
        NEAREST("Nearest Value");

        /**
         * the description of the enum instance. It's use as possible value for parameter
         * {@link fr.soleil.passerelle.actor.tango.archiving.ExtractValueFromHDB.extractionTypeParam}
         */
        private String descprition;

        /**
         * a hashMap that map descprition to the associated Enum instance. It use to build Enum from
         * a String
         */
        private static final Map<String, ExtractionType> DescriptionMap = new HashMap<String, ExtractionType>();
        static {
            for (final ExtractionType operation : values()) {
                DescriptionMap.put(operation.getDescription(), operation);
            }
        }

        private ExtractionType(final String descprition) {
            this.descprition = descprition;
        }

        public String getDescription() {
            return descprition;
        }

        /**
         * build enum from the descprition
         * 
         * @param desc the descprition use to build the enum
         * @return the enum associated to the descprition
         * @throws IllegalActionException is thrown if no enum match the descprition
         */
        public static ExtractionType fromDescription(final String desc) throws IllegalActionException {
            final ExtractionType value = DescriptionMap.get(desc);
            if (value != null)
                return value;
            throw new IllegalActionException("Unknown extraction description: \"" + desc + "\"");
        }
    }
}
