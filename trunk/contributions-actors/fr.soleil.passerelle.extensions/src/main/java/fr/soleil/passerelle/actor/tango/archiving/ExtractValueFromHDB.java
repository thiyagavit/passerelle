package fr.soleil.passerelle.actor.tango.archiving;

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
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.Level;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoActorV5;
import fr.soleil.passerelle.util.DevFailedValidationException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;

public class ExtractValueFromHDB extends ATangoActorV5 {

    private HdbExtractorProxy extractorProxy;

    public static final String COMPLETE_ATTR_NAME = "Complete attribute Name";
    public static final String ERROR_COMPLETE_ATTR_NAME_IS_EMPTY = COMPLETE_ATTR_NAME
            + " must not be empty";

    public static final String EXTRACTION_TYPE = "Extraction type";

    public static final String THROW_EXCEPTION_ON_ERROR = "Throw exception On Error";

    /**
     * the Complete Attribute Name to extract
     */
    @ParameterName(name = COMPLETE_ATTR_NAME)
    public Parameter completeAttributeNameParam;
    private String completeAttributeName = "domain/family/member/attribute_name";

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

    @ParameterName(name = THROW_EXCEPTION_ON_ERROR)
    public Parameter throwExceptionOnErrorParam;
    private boolean throwExceptionOnError = true;

    public ExtractValueFromHDB(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        completeAttributeNameParam = new StringParameter(this, COMPLETE_ATTR_NAME);
        completeAttributeNameParam.setExpression(completeAttributeName);

        extractionTypeParam = new StringParameter(this, EXTRACTION_TYPE);
        for (final ExtractionType extractionType : ExtractionType.values()) {
            extractionTypeParam.addChoice(extractionType.getDescription());
        }
        extractionTypeParam.setExpression(ExtractionType.LASTED.getDescription());

        throwExceptionOnErrorParam = new Parameter(this, THROW_EXCEPTION_ON_ERROR,
                new BooleanToken(throwExceptionOnError));
        throwExceptionOnErrorParam.setTypeEquals(BaseType.BOOLEAN);

    }

    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {

        if (attribute == completeAttributeNameParam) {
            completeAttributeName = extractAndValidateCompleteAttrName();

        } else if (attribute == extractionTypeParam) {
            extractionType = extractAndValidateExtrationType();

        } else if (attribute == throwExceptionOnErrorParam) {
            throwExceptionOnError = ((BooleanToken) throwExceptionOnErrorParam.getToken())
                    .booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    private ExtractionType extractAndValidateExtrationType() throws IllegalActionException {
        // throw an IllegalActionException if extraction is unknown
        return ExtractionType.fromDescription(PasserelleUtil.getParameterValue(extractionTypeParam)
                .trim());
    }

    private String extractAndValidateCompleteAttrName() throws IllegalActionException {
        String completeAttrName = PasserelleUtil.getParameterValue(completeAttributeNameParam);
        if (completeAttrName.isEmpty()) {
            throw new IllegalActionException(ERROR_COMPLETE_ATTR_NAME_IS_EMPTY);
        }
        return completeAttrName;
    }

    @Override
    protected void validateInitialization() throws ValidationException {

        try {
            completeAttributeName = extractAndValidateCompleteAttrName();

            extractionType = extractAndValidateExtrationType();
            if (extractorProxy == null) {// FIXME == null if we are in prod env
                extractorProxy = new HdbExtractorProxy(true);
                ExecutionTracerService.trace(this,
                        "using hdb Extractor " + extractorProxy.getHdbExtractorName(), Level.DEBUG);
            }
        }
        catch (DevFailed e) {
            throw new DevFailedValidationException(e, this);
        }
        catch (IllegalActionException e) {
            throw new ValidationException(ErrorCode.FLOW_VALIDATION_ERROR, e.getMessage(), this, e);
        }
        super.validateInitialization();
    }

    @Override
    protected void process(ActorContext context, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        switch (extractionType) {// TODO remove switch => implements extract value in Enum
            case LASTED:
                extractLastedValue(context, request, response);
                break;
            // TODO implement nearest value
            default:// should not append
                throw new ProcessingExceptionWithLog(this, Severity.FATAL,
                        "Unknown extraction type \"" + extractionType.getDescription() + "\"",
                        this, null);
        }

    }

    private void extractLastedValue(ActorContext context, ProcessRequest request,
            ProcessResponse response) throws ProcessingException {

        try {
            int index = completeAttributeName.lastIndexOf("/");

            // TODO change this to use an api + manage alias ???
            String deviceName = completeAttributeName.substring(0, index);
            String attributeName = completeAttributeName.substring(index + 1);

            String extractedValue = extractorProxy
                    .getLastScalarAttrValue(deviceName, attributeName);
            sendOutputMsg(output, PasserelleUtil.createContentMessage(this, extractedValue));
        }
        catch (DevFailed devFailed) {
            if (throwExceptionOnError) {
                throw new ProcessingExceptionWithLog(this, completeAttributeName
                        + " is not in Hdb or can not be read: ", context, devFailed);
            } else {
                sendOutputMsg(output, PasserelleUtil.createContentMessage(this, ""));
            }
        }

    }

    public void setExtractorProxy(HdbExtractorProxy extractorProxy) {
        this.extractorProxy = extractorProxy;
    }

    public static enum ExtractionType {
        LASTED("Lasted Value"), NEAREST("Nearest Value");

        private String descprition;

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

        public static ExtractionType fromDescription(final String desc)
                throws IllegalActionException {
            final ExtractionType value = DescriptionMap.get(desc);
            if (value != null) {
                return value;
            }
            throw new IllegalActionException("Unknown extraction description: \"" + desc + "\"");
        }
    }
}
