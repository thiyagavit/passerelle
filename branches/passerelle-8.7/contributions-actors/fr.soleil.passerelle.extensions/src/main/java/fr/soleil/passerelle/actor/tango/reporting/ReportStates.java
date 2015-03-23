package fr.soleil.passerelle.actor.tango.reporting;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoDs.TangoConst;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.tango.util.FilterHelper;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class ReportStates extends ATangoDeviceActor {

    private final static Logger logger = LoggerFactory.getLogger(ReportStates.class);

    public ReportStates(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        super.setCreateDeviceProxy(false);
        recordDataParam.setVisibility(Settable.EXPERT);

        final URL url = this.getClass().getResource(
                "/org/tango-project/tango-icon-theme/32x32/mimetypes/x-office-spreadsheet.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:lightgray;stroke:black\"/>\n"
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
        logger.debug("device name " + getDeviceName());
        if (!isMockMode()) {
            try {
                ApiUtil.get_db_obj();
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }

        super.doInitialize();
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - Get states OK");
            response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());
        } else {
            final String deviceName = getDeviceName();
            List<String> devicesToCheck = new ArrayList<String>();
            try {
                devicesToCheck = FilterHelper.getDevicesForPattern(deviceName);
            } catch (final DevFailed e1) {
                ExceptionUtil.throwProcessingException(this, e1);
            }
            if (devicesToCheck.isEmpty()) {
                ExecutionTracerService.trace(this, "no device found for pattern: " + deviceName);
            }
            for (final String device : devicesToCheck) {
                if (isFinishRequested()) {
                    break;
                }
                DevState state = null;
                try {
                    // bug 22954
                    state = TangoAccess.getCurrentState(device);
                    ExecutionTracerService.trace(this,
                            device + " state: " + TangoConst.Tango_DevStateName[state.value()]);
                } catch (final DevFailed e) {
                    ExecutionTracerService.trace(this, device + " is stopped");
                }
            }
        }
        // sendOutputMsg(output, PasserelleUtil.createCopyMessage(this,
        // message));
        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());
    }
}
