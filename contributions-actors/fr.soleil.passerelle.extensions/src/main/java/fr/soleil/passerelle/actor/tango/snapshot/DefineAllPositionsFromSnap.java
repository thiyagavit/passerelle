package fr.soleil.passerelle.actor.tango.snapshot;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.utils.TangoUtil;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfiguration;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

@SuppressWarnings("serial")
public class DefineAllPositionsFromSnap extends Transformer {

    private static final String GALIL_AXIS = "GalilAxis";
    private final static Logger logger = LoggerFactory.getLogger(DefineAllPositionsFromSnap.class);
    private static final String EXTRACTION_TYPE = "Extraction type";
    @ParameterName(name = EXTRACTION_TYPE)
    public Parameter snapExtractionTypeParam;
    String snapExtractorName;
    TangoCommand getSnap;
    TangoCommand removeDynAttrs;
    boolean getReadPart;

    public DefineAllPositionsFromSnap(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        snapExtractionTypeParam = new StringParameter(this, EXTRACTION_TYPE);
        snapExtractionTypeParam.addChoice(ExtractionType.READ.toString());
        snapExtractionTypeParam.addChoice(ExtractionType.WRITE.toString());
        snapExtractionTypeParam.setExpression(ExtractionType.READ.toString());
        input.setName("SnapID");
        input.setExpectedMessageContentType(String.class);

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
    /*
     * @throws InitializationException
     */
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
            	snapExtractorName =  TangoAccess.getFirstDeviceExportedForClass("SnapExtractor");
                getSnap = new TangoCommand(snapExtractorName, "GetSnap");
                removeDynAttrs = new TangoCommand(snapExtractorName, "RemoveDynAttrs");
                logger.debug(snapExtractorName);
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    // FIXME it's not guaranteed that snapExtract return offset before Position
    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK");
        } else {
            try {
                final String snapID = (String) PasserelleUtil.getInputValue(arg0);
                final String[] result = getSnap.execute(String[].class, snapID);
                for (int i = 0; i < result.length; i = i + 3) {
                    final String attributeName = result[i];

                    String equipmentDeviceName = null;
                    Double snapValue = null;
                    if (attributeName.endsWith("offset") || attributeName.endsWith("position")) {

                        final String snapValueAttr = (getReadPart) ? result[i + 1] : result[i + 2];

                        try {
                            final TangoAttribute attr = new TangoAttribute(snapExtractorName + "/" + snapValueAttr);
                            snapValue = attr.read(Double.class);

                            equipmentDeviceName = TangoUtil.getfullDeviceNameForAttribute(attributeName);

                            if (ProxyFactory.getInstance().createDeviceProxy(equipmentDeviceName).get_class()
                                    .equals(GALIL_AXIS)) {
                                final MotorConfiguration motorConf = new MotorConfiguration(equipmentDeviceName);

                                // init devices if necessary
                                motorConf.initMotor(this);

                                // set offset
                                if (attributeName.endsWith("offset")) {
                                    final TangoAttribute offsetAttr = new TangoAttribute(equipmentDeviceName
                                            + "/offset");

                                    ExecutionTracerService.trace(this, "Writing offset " + snapValue + " to "
                                            + equipmentDeviceName);

                                    offsetAttr.write(snapValue);

                                } else {
                                    // define position only for attribute
                                    // position
                                    final TangoCommand setterCommand = new TangoCommand(equipmentDeviceName,
                                            "DefinePosition");

                                    ExecutionTracerService.trace(this, "Defining " + snapValue + " to "
                                            + equipmentDeviceName);

                                    setterCommand.execute(snapValue);
                                    // wait for the completion of define pos
                                    sleep(100);
                                }

                            } else {
                                ExecutionTracerService.trace(this, equipmentDeviceName
                                        + " is not a GalilAxis, nothing done");
                            }

                        } catch (final DevFailed e) {
                            final String completeAttrName = (equipmentDeviceName == null) ? attributeName
                                    : equipmentDeviceName;

                            ExecutionTracerService.trace(this, "DefinePosition error for " + completeAttrName);
                            TangoToPasserelleUtil.getDevFailedString(e, this);

                        } catch (final ProcessingException e) {
                            final String completeAttrName = (equipmentDeviceName == null) ? attributeName
                                    : equipmentDeviceName;

                            ExecutionTracerService.trace(this,
                                    "DefinePosition error for " + completeAttrName + " " + e.getMessage());
                        }
                    }

                    removeDynAttrs.execute(result[i + 1], result[i + 2]);
                }
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        sendOutputMsg(output, PasserelleUtil.createTriggerMessage());

    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == snapExtractionTypeParam) {
            final String snapExtractionType = PasserelleUtil.getParameterValue(snapExtractionTypeParam);
            if (snapExtractionType.compareTo(ExtractionType.READ.toString()) == 0) {
                getReadPart = true;
            } else {
                getReadPart = false;
            }
        } else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * temporarily cease execution for the specified number of milliseconds. Its the same as
     * Thread.sleep(...) but Exception is caught
     * 
     * @param millis time to sleep
     */
    private void sleep(final long millis) {
        try {
            Thread.sleep(100);
        } catch (final InterruptedException e) {
        }
    }
}
