package fr.soleil.passerelle.actor.tango.snapshot;

import java.net.URL;

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
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.Level;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.DevFailedValidationException;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class ExtractValueFromSnapIDV2 extends Actor {

    public static final int READ_PORT = 0;
    public static final int WRITE_PORT = 1;

    public static final String WRITE_PORT_LABEL = "write value";
    public static final String READ_PORT_LABEL = "read value";
    public static final String THROW_EXCEPTION_ON_ERROR_LABEL = "Throw exception On Error";
    public static final String EXTRACTION_TYPE_LABEL = "Extraction type";
    public static final String ATTRIBUTE_NAME_LABEL = "Attribute to extract";
    public static final String ERROR_ATTR_NAME_PARAM_EMPTY = ATTRIBUTE_NAME_LABEL
            + " can not be empty";

    private final static Logger logger = LoggerFactory.getLogger(ExtractValueFromSnapIDV2.class);
    private SnapExtractorProxy extractor;

    @ParameterName(name = ATTRIBUTE_NAME_LABEL)
    public Parameter attributeNameParam;
    private String attributeName;

    public Port[] outputPorts;
    public Port inputPort;

    @ParameterName(name = EXTRACTION_TYPE_LABEL)
    public Parameter extractionTypeParam;
    private ExtractionTypeV2 extractionType;

    @ParameterName(name = THROW_EXCEPTION_ON_ERROR_LABEL)
    public Parameter throwExceptionOnErrorParam;
    private boolean throwExceptionOnError = true;

    public ExtractValueFromSnapIDV2(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        attributeNameParam = new StringParameter(this, ATTRIBUTE_NAME_LABEL);
        attributeNameParam.setExpression("name");

        inputPort = PortFactory.getInstance().createInputPort(this, "snapID", null);

        outputPorts = new Port[2];
        outputPorts[READ_PORT] = PortFactory.getInstance().createOutputPort(this, READ_PORT_LABEL);
        // createPort(WRITE_PORT, WRITE_PORT_LABEL);

        // deletePort(WRITE_PORT, WRITE_PORT_LABEL);

        extractionTypeParam = new StringParameter(this, EXTRACTION_TYPE_LABEL);
        for (ExtractionTypeV2 extractionType : ExtractionTypeV2.values()) {
            extractionTypeParam.addChoice(extractionType.getDescription());
        }
        extractionTypeParam.setExpression(ExtractionTypeV2.READ.getDescription());

        throwExceptionOnErrorParam = new Parameter(this, THROW_EXCEPTION_ON_ERROR_LABEL,
                new BooleanToken(throwExceptionOnError));
        throwExceptionOnErrorParam.setTypeEquals(BaseType.BOOLEAN);

        final URL url = this.getClass().getResource(
                "/org/tango-project/tango-icon-theme/32x32/devices/camera-photo.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:orange;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\"" + url
                + "\"/>\n" + "</svg>\n");
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == attributeNameParam) {
            attributeName = PasserelleUtil.getParameterValue(attributeNameParam);
            if (attributeName.isEmpty()) {
                throw new IllegalActionException(ERROR_ATTR_NAME_PARAM_EMPTY);
            }
        } else if (attribute == extractionTypeParam) {
            // throws IllegalActionException if invalid
            extractionType = ExtractionTypeV2.fromDescription(PasserelleUtil
                    .getParameterValue(extractionTypeParam));

            switch (extractionType) {
                case READ:
                    deletePort(WRITE_PORT, WRITE_PORT_LABEL);
                    createPort(READ_PORT, READ_PORT_LABEL);

                    break;
                case WRITE:
                    deletePort(READ_PORT, READ_PORT_LABEL);
                    createPort(WRITE_PORT, WRITE_PORT_LABEL);
                    break;
                case READ_WRITE:
                    // we delete all port to always have the same order. (ie read port is always
                    // upper that write port)
                    deletePort(READ_PORT, READ_PORT_LABEL);
                    deletePort(WRITE_PORT, WRITE_PORT_LABEL);

                    createPort(READ_PORT, READ_PORT_LABEL);
                    createPort(WRITE_PORT, WRITE_PORT_LABEL);
                    break;
            }
        } else if (attribute == throwExceptionOnErrorParam) {
            throwExceptionOnError = ((BooleanToken) throwExceptionOnErrorParam.getToken())
                    .booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected void validateInitialization() throws ValidationException {
        validateAttribute(attributeNameParam);
        validateAttribute(extractionTypeParam);

        try {
            if (extractor == null) {// FIXME == null if we are in prod env
                extractor = new SnapExtractorProxy(true);
                ExecutionTracerService.trace(this, "using snap Extractor " + extractor.getName(),
                        Level.DEBUG);
            }
        }
        catch (DevFailed e) {
            throw new DevFailedValidationException(e, this);
        }
    }

    // TODO move to super class
    /**
     * It's a wrapper of attributeChanged that "convert" IllegalActionException in
     * ValidationException. This method is design to be used in validateInitialization() method to
     * do the static verification on parameters
     * 
     * @param attribute the parameter to check
     * 
     * @throws ValidationException if the parameter is invalid then a ValidationException is raised
     */
    protected void validateAttribute(Attribute attribute) throws ValidationException {
        try {
            attributeChanged(attribute);
        }
        catch (IllegalActionException e) {
            throw new ValidationException(ErrorCode.FLOW_VALIDATION_ERROR, e.getMessage(), this, e);
        }
    }

    private void createPort(final int portIndex, String name) throws IllegalActionException {
        try {
            if (outputPorts[portIndex] == null) {
                outputPorts[portIndex] = PortFactory.getInstance().createOutputPort(this, name);

            } else if (outputPorts[portIndex].getContainer() == null) {
                outputPorts[portIndex].setContainer(this);
            }
        }
        catch (NameDuplicationException e) {
            throw new IllegalActionException(e.getNameable1(), e.getNameable2(), e,
                    "Error: can create read port");
        }
    }

    private void deletePort(final int portIndex, final String name) throws IllegalActionException {
        if (outputPorts[portIndex] != null) {
            try {
                outputPorts[portIndex].setContainer(null);
            }
            catch (NameDuplicationException e) {
                throw new IllegalActionException(e.getNameable1(), e.getNameable2(), e,
                        "Error: can not remove " + name + " port");
            }
        }
    }

    @Override
    protected void process(ActorContext context, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        String snapID = (String) PasserelleUtil.getInputValue(request.getMessage(inputPort));

        extractAndSendValues(snapID);
    }

    private void checkSnapIdIsAnInt(String snapID) throws ProcessingException {
        try {
            int snapIDAsInt = Integer.parseInt(snapID);
            if (snapIDAsInt < SnapExtractorProxy.ID_MIN) {
                throw new ProcessingException(Severity.FATAL,
                        SnapExtractorProxy.ERROR_SNAP_ID_INF_ZERO, this, null);
            }
        }
        catch (NumberFormatException e) {
            throw new ProcessingException(Severity.FATAL, SnapExtractorProxy.ERROR_SNAP_ID_NAN,
                    this, null);
        }
    }

    protected void extractAndSendValues(final String snapID) throws ProcessingException,
            IllegalArgumentException {

        checkSnapIdIsAnInt(snapID);

        switch (extractionType) {
            case READ:
                extractAndSendReadValue(snapID);
                break;
            case WRITE:
                extractAndSendWriteValue(snapID);
                break;
            case READ_WRITE:
                extractAndSendReadAndWriteValues(snapID);
                break;

            default:// should not append
                new ProcessingException(Severity.FATAL, "Unknown extration type: "
                        + extractionType.getDescription(), this, null);
        }
    }

    private void extractAndSendReadAndWriteValues(final String snapID) throws ProcessingException,
            DevFailedProcessingException {
        try {
            String[] snapReadAndWriteValues = extractor.getSnapValue(snapID, attributeName);

            sendOutputMsg(outputPorts[READ_PORT],
                    PasserelleUtil.createContentMessage(this, snapReadAndWriteValues[0]));
            sendOutputMsg(outputPorts[WRITE_PORT],
                    PasserelleUtil.createContentMessage(this, snapReadAndWriteValues[1]));
        }
        catch (DevFailed e) {
            if (throwExceptionOnError) {
                throw new DevFailedProcessingException(e, this);
            } else {
                sendOutputMsg(outputPorts[READ_PORT], createMessage());
                sendOutputMsg(outputPorts[WRITE_PORT], createMessage());
            }
        }
    }

    private void extractAndSendWriteValue(String snapID) throws ProcessingException,
            DevFailedProcessingException {
        try {
            String[] snapWriteValues = extractor.getWriteValues(snapID, attributeName);
            sendOutputMsg(outputPorts[WRITE_PORT],
                    PasserelleUtil.createContentMessage(this, snapWriteValues[0]));
        }
        catch (DevFailed e) {
            if (throwExceptionOnError) {
                throw new DevFailedProcessingException(e, this);
            } else {
                sendOutputMsg(outputPorts[WRITE_PORT], createMessage());
            }
        }
    }

    private void extractAndSendReadValue(String snapID) throws ProcessingException,
            DevFailedProcessingException {
        try {
            String[] snapReadValues = extractor.getReadValues(snapID, attributeName);
            sendOutputMsg(outputPorts[READ_PORT],
                    PasserelleUtil.createContentMessage(this, snapReadValues[0]));
        }
        catch (DevFailed e) {
            if (throwExceptionOnError) {
                throw new DevFailedProcessingException(e, this);
            } else {

                sendOutputMsg(outputPorts[READ_PORT], createMessage());
            }
        }
    }

    public SnapExtractorProxy getGetSnapExtractor() {
        return extractor;
    }

    public void setGetSnapExtractor(SnapExtractorProxy extractor) {
        this.extractor = extractor;
    }

    public ExtractionTypeV2 getExtractionType() {
        return extractionType;
    }
}
