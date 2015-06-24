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
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.actor.TransformerV3;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class LaunchSnapShot extends TransformerV3 {

    private final static Logger logger = LoggerFactory.getLogger(LaunchSnapShot.class);

    private TangoCommand launchCmd;
    private TangoCommand commentCmd;
    private TangoCommand getSnapIdCmd;

    public Parameter snapShotCommentParam;
    private String snapShotComment;

    private WaitStateTask waitTask = null;

    public LaunchSnapShot(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);

        snapShotCommentParam = new StringParameter(this, "SnapShot Comment");

        input.setName("ContextID");
        input.setExpectedMessageContentType(String.class);

        output.setName("SnapShotID");

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
        super.doInitialize();
        if (!isMockMode()) {
            try {
                final String snapManagerName = TangoAccess.getFirstDeviceExportedForClass("SnapManager");
                launchCmd = new TangoCommand(snapManagerName, "LaunchSnapShot");
                commentCmd = new TangoCommand(snapManagerName, "UpdateSnapComment");
                getSnapIdCmd = new TangoCommand(snapManagerName, "GetSnapShotResult");
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }

    }

    @Override
    protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        final ManagedMessage message = request.getMessage(input);
        final String contextID = (String) PasserelleUtil.getInputValue(message);

        Double snapShotID = null;
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - launching snapshot for context " + contextID);
        } else {
            try {
                logger.debug("context id " + contextID);

                // Modif due to request in
                // http://controle/mantis/view.php?id=19587
                // LaunchSnapShot execution
                launchCmd.execute(Double.class, contextID);

                // Wait for the end of Running State for the SnapManager
                ExecutionTracerService.trace(this, "waiting end of state Running on SnapManager");
                waitTask = new WaitStateTask(launchCmd.getDeviceProxy().get_name(), DevState.RUNNING, 100, false, 60,
                        "The SnapManager is blocked", "LaunchSnapShotWaitEndState");
                waitTask.run();

                if (waitTask.hasFailed()) {
                    throw waitTask.getDevFailed();
                }

                if (!isFinishRequested()) {
                    ExecutionTracerService.trace(this, "waiting end of state Running on SnapManager finished");

                    // Get last Snap id for this context if no other snap has
                    // been requested in this context !
                    snapShotID = getSnapIdCmd.execute(Double.class, contextID);

                    // Snap comment modification
                    if (!snapShotComment.isEmpty()) {
                        final double[] arg1 = { snapShotID.longValue() };
                        final String[] arg2 = { snapShotComment };
                        commentCmd.insertMixArgin(arg1, arg2);
                        commentCmd.execute();
                    }
                    ExecutionTracerService.trace(this, "launching snapshot for context " + contextID);
                } else {
                    ExecutionTracerService.trace(this, "waiting end of state Running on SnapManager cancelled");
                }
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }

        response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, snapShotID));
    }

    @Override
    protected void doStop() {
        if (waitTask != null) {
            if (waitTask.isRunning()) {
                waitTask.cancel();
                ExecutionTracerService.trace(this, "Snap has been stopped");
            }
        }
        super.doStop();
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == snapShotCommentParam) {
            snapShotComment = PasserelleUtil.getParameterValue(snapShotCommentParam);
        } else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
