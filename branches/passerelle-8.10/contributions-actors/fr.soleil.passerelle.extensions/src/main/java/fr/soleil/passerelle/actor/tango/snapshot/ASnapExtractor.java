package fr.soleil.passerelle.actor.tango.snapshot;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.tango.utils.TangoUtil;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public abstract class ASnapExtractor extends Transformer {

    private SnapExtractorProxy extractor;

    public Parameter attributeNameParam;
    private String attributeName;
    private String[] attributeNames;

    public Port writePort;

    private String snapID;
    private String snapExtractorName;

    public Parameter snapExtractionTypeParam;
    private String snapExtractionType;

    private ExtractionType extractionType;

    public ASnapExtractor(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        attributeNameParam = new StringParameter(this, "Attribute to extract");
        attributeNameParam.setExpression("name");
        new TextStyle(attributeNameParam, "paramsTextArea");
        output.setName("read value");

        writePort = PortFactory.getInstance().createOutputPort(this, "write value");

        snapExtractionTypeParam = new StringParameter(this, "Extraction type");
        snapExtractionTypeParam.addChoice(ExtractionType.READ.toString());
        snapExtractionTypeParam.addChoice(ExtractionType.WRITE.toString());
        snapExtractionTypeParam.addChoice(ExtractionType.READ_WRITE.toString());
        snapExtractionTypeParam.setExpression(ExtractionType.READ.toString());

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
    protected void doInitialize() throws InitializationException {
        if (isMockMode()) {
            attributeNames = new String[] { "mock1", "mock2" };
        } else {
            try {
                final List<String> list = new ArrayList<String>();
                final String[] tempAttr = attributeName.split("\n");
                for (final String attr : tempAttr) {
                    if (!attr.startsWith("#")) {
                        final String attrOnly = TangoUtil.getAttributeName(attr);
                        final String devicePattern = TangoUtil.getfullDeviceNameForAttribute(attr);
                        final String[] deviceNames = TangoUtil.getDevicesForPattern(devicePattern);
                        for (final String string : deviceNames) {
                            list.add(string + "/" + attrOnly);
                        }
                    }
                }
                attributeNames = list.toArray(new String[list.size()]);
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
            try {
                extractor = new SnapExtractorProxy();
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    /**
     * Retrieve read and write values from snap for a list of attributes. Send them to the 2 output
     * ports
     * 
     * @throws DevFailed
     * @throws ProcessingException
     */
    protected void getAndSendValues() throws DevFailed, ProcessingException {
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - snap values for snap ID " + snapID + " : [read = " + 1
                    + "] [write = " + 2 + "]");
            sendOutputMsg(output, PasserelleUtil.createContentMessage(this, 1));
            sendOutputMsg(writePort, PasserelleUtil.createContentMessage(this, 2));
        } else {
            String[] snapReadValues = null;
            String[] snapWriteValues = null;

            switch (extractionType) {
                case READ:
                    snapReadValues = extractor.getReadValues(snapID, attributeNames);
                    break;
                case WRITE:
                    snapWriteValues = extractor.getWriteValues(snapID, attributeNames);
                    break;
                default:
                    snapReadValues = extractor.getReadValues(snapID, attributeNames);
                    snapWriteValues = extractor.getWriteValues(snapID, attributeNames);
                    break;
            }
            int i = 0;
            for (final String name : attributeNames) {

                switch (extractionType) {
                    case READ:
                        ExecutionTracerService.trace(this, name + " snap values for snap ID " + snapID + " : [read = "
                                + snapReadValues[i] + "]");
                        sendOutputMsg(output, PasserelleUtil.createContentMessage(this, snapReadValues[i]));
                        break;
                    case WRITE:
                        ExecutionTracerService.trace(this, name + " snap values for snap ID " + snapID + " : [write = "
                                + snapWriteValues[i] + "]");
                        sendOutputMsg(writePort, PasserelleUtil.createContentMessage(this, snapWriteValues[i]));
                        break;
                    default:
                        ExecutionTracerService.trace(this, name + " snap values for snap ID " + snapID + " : [read = "
                                + snapReadValues[i] + "] [write = " + snapWriteValues[i] + "]");
                        sendOutputMsg(output, PasserelleUtil.createContentMessage(this, snapReadValues[i]));
                        sendOutputMsg(writePort, PasserelleUtil.createContentMessage(this, snapWriteValues[i]));
                        break;
                }
                i++;
                if (isFinishRequested()) {
                    break;
                }
            }
        }
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == attributeNameParam) {
            attributeName = PasserelleUtil.getParameterValue(attributeNameParam);
        } else if (attribute == snapExtractionTypeParam) {
            snapExtractionType = PasserelleUtil.getParameterValue(snapExtractionTypeParam);
            if (snapExtractionType.equalsIgnoreCase(ExtractionType.READ.toString())) {
                extractionType = ExtractionType.READ;
            } else if (snapExtractionType.equalsIgnoreCase(ExtractionType.WRITE.toString())) {
                extractionType = ExtractionType.WRITE;
            } else {
                extractionType = ExtractionType.READ_WRITE;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    public String getSnapID() {
        return snapID;
    }

    /**
     * Configure snap ID with last snapshot of a context
     * 
     * @param contextID
     * @throws DevFailed
     */
    public void setSnapIDFromContext(final String contextID) throws DevFailed {
        if (!isMockMode()) {
            snapID = extractor.getLastSnapID(contextID);
        }
    }

    public void setSnapID(final String snapID) {
        this.snapID = snapID;
    }

    /**
     * SoleilUtilities.getDevicesFromClass("SnapExtractor")[0] has been integrated in the extractor
     */
    @Deprecated
    public String getSnapExtractorName() throws DevFailed {
        if (snapExtractorName == null) {
        		snapExtractorName =  TangoAccess.getFirstDeviceExportedForClass("SnapExtractor") ;
        }
        return snapExtractorName;
    }

    public SnapExtractorProxy getGetSnapExtractor() {
        return extractor;
    }

    public ExtractionType getExtractionType() {
        return extractionType;
    }

}
