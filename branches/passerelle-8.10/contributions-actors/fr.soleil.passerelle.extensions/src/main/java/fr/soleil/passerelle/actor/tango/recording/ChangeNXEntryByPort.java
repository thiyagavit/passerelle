package fr.soleil.passerelle.actor.tango.recording;

import java.net.URL;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class ChangeNXEntryByPort extends Transformer {

    // public Parameter suffixParam;
    // private String suffix ="";

    public ChangeNXEntryByPort(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);
        output.setName("input copy");

        /*
         * suffixParam = new StringParameter(this, "Acquisition Name");
         * suffixParam.setExpression(suffix);
         */

        input.setName("AcquisitionName");

        final URL url = this.getClass().getResource(
                "/org/tango-project/tango-icon-theme/32x32/categories/applications-multimedia.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:red;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\"" + url + "\"/>\n" + "</svg>\n");
    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - Change NXEntry");
        } else {
            try {
                final String suffix = (String) PasserelleUtil.getInputValue(arg0);
                ExecutionTracerService.trace(this, "change NXEntry to " + suffix);
                // set the current nxentry name in the nexus file
                DataRecorder.getInstance().setNxEntryNameAndSaveContext(this, suffix);
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        sendOutputMsg(output, PasserelleUtil.createCopyMessage(this, arg0));
    }

    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }
}