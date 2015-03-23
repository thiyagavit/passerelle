package fr.soleil.passerelle.actor.tango.snapshot;

import static fr.soleil.passerelle.util.PasserelleUtil.createContentMessage;

import java.net.URL;

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
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.Level;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * Allow to extract an attribute from the snapshot database.
 * 
 * The id of the snaphot is provided by the input port "snapid" and the attribute by the parameter
 * "attribute name". The attribute name must be complete (ie domain/family/member/attrName) and not
 * empy
 * 
 * The parameter ExtractionType is used to select which part of the attribute must be extracted :
 * <ul>
 * <li>the read part, in this case the actor have only one output port: "read value"</li>
 * <li>the write part, in this case the actor have only one output port: "write value"</li>
 * <li>the read and write part, in this case the actor have the 2 previous port</li>
 * </ul>
 * 
 * if an error occurred during extraction, then there are 2 possibilities:
 * 
 * <ul>
 * <li>
 * {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.throwExceptionOnError} is true then an
 * exception is throw</li>
 * <li>
 * {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.throwExceptionOnError} is false then an
 * empty message is send to outport</li>
 * </ul>
 * 
 * the outport are dynamically changed when the extraction type changed ({@link
 * fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.attributeChanged(Attribute)} thanks to field
 * {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.outputPorts} and
 * methods {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.createPort(int,
 * String)}, {@link
 * fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.deletePort(int, String)}
 * 
 * @author gramer
 * 
 */
@SuppressWarnings("serial")
public class ExtractValueFromSnapIDV2 extends Actor {

    /**
     * the index of read port in {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.outputPorts}
     */
    public static final int READ_PORT = 0;

    /**
     * the index of write port in {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.outputPorts}
     */
    public static final int WRITE_PORT = 1;

    /**
     * the label of write port
     */
    public static final String WRITE_PORT_LABEL = "write value";

    /**
     * the label of read port
     */
    public static final String READ_PORT_LABEL = "read value";

    /**
     * the label of throw on error parameter
     */
    public static final String THROW_EXCEPTION_ON_ERROR_LABEL = "Throw exception On Error";
    /**
     * the label of extratiobnType parameter
     */
    public static final String EXTRACTION_TYPE_LABEL = "Extraction type";
    /**
     * the label of attribute Name parameter
     */
    public static final String ATTRIBUTE_NAME_LABEL = "Attribute to extract";

    /**
     * the error message when attribute Name parameter is empty (its used for unit test)
     */
    public static final String ERROR_ATTR_NAME_PARAM_EMPTY = ATTRIBUTE_NAME_LABEL + " can not be empty";

    /**
     * Manage all request to the snapshot db see {@link fr.soleil.passerelle.actor.tango.snapshot.SnapExtractorProxy}
     * for more details
     */
    private SnapExtractorProxy extractor;

    /**
     * the complete name (ie domain/family/member/attrName) of the attribute to extract
     */
    @ParameterName(name = ATTRIBUTE_NAME_LABEL)
    public Parameter attributeNameParam;
    private String attributeName;

    /**
     * Array that contains the out ports of the actor
     */
    public Port[] outputPorts;

    /**
     * the input port of the actor. You must send the snapId (integer >0)
     */
    public Port inputPort;

    /**
     * the Extraction type param indicate which part of the attribute will be extracted
     * {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractionType} for more details
     */
    @ParameterName(name = EXTRACTION_TYPE_LABEL)
    public Parameter extractionTypeParam;
    private ExtractionType extractionType;

    /**
     * flag that indicate if we must raise an exception if the extraction failed
     */
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

        extractionTypeParam = new StringParameter(this, EXTRACTION_TYPE_LABEL);

        for (ExtractionType extractionType : ExtractionType.values()) {
            extractionTypeParam.addChoice(extractionType.getName());
        }
        extractionTypeParam.setExpression(ExtractionType.READ.getName());

        throwExceptionOnErrorParam = new Parameter(this, THROW_EXCEPTION_ON_ERROR_LABEL, new BooleanToken(
                throwExceptionOnError));
        throwExceptionOnErrorParam.setTypeEquals(BaseType.BOOLEAN);

        final URL url = this.getClass().getResource(
                "/org/tango-project/tango-icon-theme/32x32/devices/camera-photo.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:orange;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\"" + url + "\"/>\n" + "</svg>\n");
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == attributeNameParam) {
            attributeName = extractAndValidateAtrributeName();

        } else if (attribute == extractionTypeParam) {
            // throws IllegalActionException if invalid
            extractionType = ExtractionType.fromDescription(PasserelleUtil.getParameterValue(extractionTypeParam));

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
            throwExceptionOnError = ((BooleanToken) throwExceptionOnErrorParam.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected void validateInitialization() throws ValidationException {
        try {
            attributeName = extractAndValidateAtrributeName();

            if (extractor == null) {// FIXME extractor== null if we are in prod env
                extractor = new SnapExtractorProxy();
            }
            ExecutionTracerService.trace(this, "using snap Extractor " + extractor.getName(), Level.DEBUG);
        } catch (DevFailed e) {
            ExceptionUtil.throwValidationException(this, e);
        } catch (IllegalActionException e) {
            throw new ValidationException(ErrorCode.FLOW_VALIDATION_ERROR, e.getMessage(), this, e);
        }
        super.validateInitialization();
    }

    public String extractAndValidateAtrributeName() throws IllegalActionException {
        String attrName = PasserelleUtil.getParameterValue(attributeNameParam);
        if (attrName.isEmpty()) {
            throw new IllegalActionException(ERROR_ATTR_NAME_PARAM_EMPTY);
        }
        return attrName;
    }

    public void validateAtrributeName() throws IllegalActionException {
        if (attributeName.isEmpty()) {
            throw new IllegalActionException(ERROR_ATTR_NAME_PARAM_EMPTY);
        }
    }

    /**
     * create a port and add it to the actor. If the actor has already this port, then nothing
     * happen
     * 
     * @param portIndex the index of port must equals to
     *            {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.READ_PORT} or
     *            {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.WRITE_PORT}
     * @param name the label of the port. Must be equals to
     *            {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.READ_PORT_LABEL} or
     *            {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.WRITE_PORT_LABEL}
     * 
     * @throws IllegalActionException is thrown port already exists. Normally this should never
     *             happen because we test if the port already exist
     */
    private void createPort(final int portIndex, String name) throws IllegalActionException {
        try {
            if (outputPorts[portIndex] == null) {
                outputPorts[portIndex] = PortFactory.getInstance().createOutputPort(this, name);

            } else if (outputPorts[portIndex].getContainer() == null) {
                outputPorts[portIndex].setContainer(this);
            }
        } catch (NameDuplicationException e) { // normally that should not happen
            throw new IllegalActionException(e.getNameable1(), e.getNameable2(), e, "Error: can create " + name
                    + " port");
        }
    }

    /**
     * delete a port and remove it from the actor. If the actor has not this port, then nothing
     * happen
     * 
     * @param portIndex the index of port. Must equals to
     *            {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.READ_PORT} or
     *            {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.WRITE_PORT}
     * @param name the label of the port. Must be equals to
     *            {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.READ_PORT_LABEL} or
     *            {@link fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.WRITE_PORT_LABEL}
     * 
     * @throws IllegalActionException is thrown port not exists. Normally this should never happen
     *             because we test if the port not exists.
     */
    private void deletePort(final int portIndex, final String name) throws IllegalActionException {
        if (outputPorts[portIndex] != null) {
            try {
                outputPorts[portIndex].setContainer(null);
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(e.getNameable1(), e.getNameable2(), e, "Error: can not remove " + name
                        + " port");
            }
        }
    }

    @Override
    protected void process(ActorContext context, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        String snapID = (String) PasserelleUtil.getInputValue(request.getMessage(inputPort));

        extractAndSendValues(snapID);
    }

    /**
     * throw an exception if the snapID is not an integer >
     * {@link fr.soleil.passerelle.actor.tango.snapshot.SnapExtractorProxy.ID_MIN}
     * 
     * @param snapID the snapId to check
     * @throws ProcessingException is thrown if the snapID is not an integer >=
     *             {@link fr.soleil.passerelle.actor.tango.snapshot.SnapExtractorProxy.ID_MIN}
     */
    private void checkSnapIdIsAnInt(String snapID) throws ProcessingException {
        try {
            int snapIDAsInt = Integer.parseInt(snapID);
            if (snapIDAsInt < SnapExtractorProxy.ID_MIN) {
                ExceptionUtil.throwProcessingException(ErrorCode.FATAL, SnapExtractorProxy.ERROR_SNAP_ID_INF_ID_MIN,
                        this);
            }
        } catch (NumberFormatException e) {
            ExceptionUtil.throwProcessingException(ErrorCode.FATAL, SnapExtractorProxy.ERROR_SNAP_ID_NAN, this);
        }
    }

    /**
     * extract the attribute value(s) and send it (their) on output port(s). If the extraction
     * failed an {@linkfr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.throwExceptionOnError} is true
     * then an exception is thrown, otherwise an empty message is sent
     * 
     * @param snapID the ID of the snapshot
     * @throws ProcessingException is thrown if
     *             {@linkfr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.throwExceptionOnError} is
     *             true and an error occurred during extraction
     * @throws IllegalArgumentException if the snap id is invalid see {@link
     *             fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.
     *             checkSnapIdIsAnInt(String)} for more details
     */
    protected void extractAndSendValues(final String snapID) throws ProcessingException, IllegalArgumentException {

        checkSnapIdIsAnInt(snapID);

        switch (extractionType) {
            case READ:
                extractAndSendReadOrWriteValue(true, snapID);
                break;
            case WRITE:
                extractAndSendReadOrWriteValue(false, snapID);
                break;
            case READ_WRITE:
                extractAndSendReadAndWriteValues(snapID);
                break;

            default:// should not happen
                ExceptionUtil.throwProcessingException(ErrorCode.FATAL,
                        "Unknown extration type: " + extractionType.getName(), this);
        }
    }

    /**
     * extract the read and the write value of the attribute and send it to the output ports. If the
     * extraction failed an
     * {@linkfr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.throwExceptionOnError} is true then an
     * exception is thrown, otherwise an empty message is sent
     * 
     * @param snapID the id of the snapshot
     * @throws ProcessingException is thrown if
     *             {@linkfr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.throwExceptionOnError} is
     *             true and an error occurred during extraction
     * @throws DevFailedProcessingException is thrown if
     *             {@linkfr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.throwExceptionOnError} is
     *             true and an error occurred during extraction
     */
    private void extractAndSendReadAndWriteValues(final String snapID) throws ProcessingException {
        try {
            String[] snapReadAndWriteValues = extractor.getSnapValue(snapID, attributeName);

            sendOutputMsg(outputPorts[READ_PORT], createContentMessage(this, snapReadAndWriteValues[0]));
            sendOutputMsg(outputPorts[WRITE_PORT], createContentMessage(this, snapReadAndWriteValues[1]));
        } catch (DevFailed e) {
            if (throwExceptionOnError) {
                ExceptionUtil.throwProcessingException(this, e);
            } else {
                sendOutputMsg(outputPorts[READ_PORT], createMessage());
                sendOutputMsg(outputPorts[WRITE_PORT], createMessage());
            }
        }
    }

    /**
     * extract the read or wirte part of the attribute according to the read parameter and send
     * extracted value on the ouptport.If the extraction failed an
     * {@linkfr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.throwExceptionOnError} is true then an
     * exception is thrown, otherwise an empty message is sent
     * 
     * @param read flag that indicate which part must be extracted. if its true then the read part
     *            is extracted write part otherwise
     * @param snapID the id of the snaphot
     * @throws ProcessingException is thrown if
     *             {@linkfr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.throwExceptionOnError} is
     *             true and an error occurred during extraction
     * @throws DevFailedProcessingException is thrown if
     *             {@linkfr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.throwExceptionOnError} is
     *             true and an error occurred during extraction
     */
    private void extractAndSendReadOrWriteValue(boolean read, String snapID) throws ProcessingException {
        int portIndex = (read) ? READ_PORT : WRITE_PORT;
        try {
            String[] snapValues = (read) ? extractor.getReadValues(snapID, attributeName) : extractor.getWriteValues(
                    snapID, attributeName);

            sendOutputMsg(outputPorts[portIndex], createContentMessage(this, snapValues[0]));
        } catch (DevFailed e) {
            if (throwExceptionOnError) {
                ExceptionUtil.throwProcessingException(this, e);
            } else {
                sendOutputMsg(outputPorts[portIndex], createMessage());
            }
        }
    }

    public SnapExtractorProxy getGetSnapExtractor() {
        return extractor;
    }

    /**
     * utility method used only for tests. That allow us to mock the extractor
     * 
     * @param extractor
     */
    public void setGetSnapExtractor(SnapExtractorProxy extractor) {
        this.extractor = extractor;
    }

    public ExtractionType getExtractionType() {
        return extractionType;
    }
}
