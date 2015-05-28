package fr.soleil.passerelle.actor.tango.snapshot;

import java.net.URL;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class EquipementsSetter extends Transformer {

    // public Parameter snapShotIDParam;
    // private String snapShotID = "";

    public Parameter typeParam;
    private String type = "STORED_READ_VALUE";

    private TangoCommand cmd;

    public EquipementsSetter(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);

        // snapShotIDParam = new StringParameter(this, "SnapShot ID");
        // snapShotIDParam.setExpression(snapShotID);

        typeParam = new StringParameter(this, "Type");
        typeParam.setExpression(type);
        typeParam.addChoice("STORED_READ_VALUE");
        typeParam.addChoice("STORED_WRITE_VALUE");

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
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
            	final String snapManagerName = TangoAccess.getFirstDeviceExportedForClass("SnapManager");
                cmd = new TangoCommand(snapManagerName, "SetEquipments");
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        final String snapShotID = (String) PasserelleUtil.getInputValue(arg0);
        if (isMockMode()) {
            ExecutionTracerService
                    .trace(this, "MOCK - setting equipments with snapshot " + snapShotID + " and " + type);
        } else {
            try {
                cmd.execute(snapShotID, type);
                ExecutionTracerService.trace(this, "setting equipments with snapshot " + snapShotID + " and " + type);
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
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        // else if (arg0 == snapShotIDParam)
        // snapShotID = ((StringToken)
        // snapShotIDParam.getToken()).stringValue();
        if (arg0 == typeParam) {
            type = ((StringToken) typeParam.getToken()).stringValue();
        } else {
            super.attributeChanged(arg0);
        }
    }

}
