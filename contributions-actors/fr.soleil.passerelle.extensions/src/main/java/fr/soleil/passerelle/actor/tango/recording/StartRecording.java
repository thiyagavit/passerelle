package fr.soleil.passerelle.actor.tango.recording;

import java.net.URL;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TriggeredSource;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.ExceptionUtil;

@SuppressWarnings("serial")
public class StartRecording extends TriggeredSource {

    private boolean messageSent = false;

    public StartRecording(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);

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
    protected void doInitialize() throws InitializationException {
        messageSent = false;
        super.doInitialize();

    }

    @Override
    protected ManagedMessage getMessage() throws ProcessingException {
        ManagedMessage result = null;
        if (!messageSent || isTriggerConnected()) {
            try {
                if (isMockMode()) {
                    ExecutionTracerService.trace(this, "MOCK - start recording");
                } else {
                    ExecutionTracerService.trace(this, "start recording");
                    DataRecorder.getInstance().startRecording(this);
                }
                final ManagedMessage resultMsg = MessageFactory.getInstance().createTriggerMessage();

                result = resultMsg;

            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(TangoToPasserelleUtil.getDevFailedString(e, this),
                        "StartRecording", e);
            } finally {
                messageSent = true;
            }
        }
        return result;
    }

    @Override
    protected boolean mustWaitForTrigger() {
        return true;
    }

    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }

}
