package fr.soleil.passerelle.actor.tango.snapshot;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class EquipmentsSetterWithCommand extends Transformer {

    private final static Logger logger = LoggerFactory.getLogger(EquipmentsSetterWithCommand.class);

    private static final String EXTRACTION_TYPE = "Extraction type";
    private static final String COMMAND_NAME = "Command Name";
    /**
     * The command name
     */
    @ParameterName(name = COMMAND_NAME)
    public Parameter commandNameParam;
    private String commandName;

    @ParameterName(name = EXTRACTION_TYPE)
    public Parameter snapExtractionTypeParam;
    private String snapExtractionType;

    private TangoCommand setEquipments;

    String snapManagerName;

    public EquipmentsSetterWithCommand(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        commandNameParam = new StringParameter(this, COMMAND_NAME);
        commandNameParam.setExpression(commandName);

        snapExtractionTypeParam = new StringParameter(this, EXTRACTION_TYPE);
        snapExtractionTypeParam.addChoice(ExtractionType.READ.toString());
        snapExtractionTypeParam.addChoice(ExtractionType.WRITE.toString());
        snapExtractionTypeParam.setExpression(ExtractionType.READ.toString());

        input.setExpectedMessageContentType(String.class);
        input.setName("SnapID");

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
            	snapManagerName = TangoAccess.getFirstDeviceExportedForClass("SnapManager");
                logger.debug(snapManagerName);
                setEquipments = new TangoCommand(snapManagerName, "SetEquipmentsWithCommand");
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK");
        } else {
            try {
                final String snapID = (String) PasserelleUtil.getInputValue(arg0);
                setEquipments.execute(commandName, snapExtractionType, snapID);
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == commandNameParam) {
            commandName = PasserelleUtil.getParameterValue(commandNameParam);
        } else if (arg0 == snapExtractionTypeParam) {
            final String tmp = PasserelleUtil.getParameterValue(snapExtractionTypeParam);
            if (tmp.compareTo(ExtractionType.READ.toString()) == 0) {
                snapExtractionType = ExtractionType.READ.getArginName();
            } else {
                snapExtractionType = ExtractionType.WRITE.getArginName();
            }
        } else {
            super.attributeChanged(arg0);
        }
    }

}
