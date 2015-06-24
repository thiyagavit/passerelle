/*
 * Synchrotron Soleil
 * 
 * File : Positioner.java
 * 
 * Project : soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 4 d�c. 2006
 * 
 * Revision: Author:
 * Date: State:
 * 
 * Log: Positioner.java,v
 */
/*
 * Created on 4 d�c. 2006
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.tango.control;

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
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

/**
 * <<<<<<< OpenClosePositioner.java Control Tango Devices that have Open/Close
 * commands. This actor will wait for .
 * 
 * ======= Control Tango Devices that have Open/Close commands. This actor will
 * wait for .
 * 
 * >>>>>>> 1.10
 * 
 * @author Gwenaelle Abeille
 */
@SuppressWarnings("serial")
public class OpenClosePositioner extends ATangoDeviceActor {

    public Parameter actionParam;
    private String action = "Close";

    private TangoCommand comHelp;
    private WaitStateTask waitTask;

    public OpenClosePositioner(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {

        super(arg0, arg1);

        actionParam = new StringParameter(this, "Action");
        actionParam.setExpression(action);

        input.setName("trigger");
        output.setName("trigger out");

        final URL url = this.getClass().getResource("/fr/soleil/valve-32x32.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:cyan;stroke:black\"/>\n"
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
                if (action.equalsIgnoreCase("Close")) {
                    comHelp = new TangoCommand(getDeviceName(), "Close");
                } else {
                    comHelp = new TangoCommand(getDeviceName(), "Open");
                }
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute
     * )
     */
    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == actionParam) {
            action = ((StringToken) actionParam.getToken()).stringValue();
        } else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        try {
            final String deviceName = getDeviceName();
            if (isMockMode()) {
                if (action.equalsIgnoreCase("Close")) {
                    ExecutionTracerService.trace(this, "MOCK - closing " + deviceName);
                    ExecutionTracerService.trace(this, "MOCK - " + deviceName + " is closed");
                } else {
                    ExecutionTracerService.trace(this, "MOCK - opening " + deviceName);
                    ExecutionTracerService.trace(this, "MOCK - " + deviceName + " is opened");
                }
            } else {
                comHelp.execute();
                // wait for correct state
                comHelp.getDeviceProxy();

                if (action.equalsIgnoreCase("Close")) {
                    ExecutionTracerService.trace(this, "closing " + deviceName);
                    waitTask = new WaitStateTask(deviceName, DevState.RUNNING, 1000, false);
                    waitTask.run();
                    if (waitTask.hasFailed()) {
                        throw waitTask.getDevFailed();
                    }
                    // Bug 22954
                    if (TangoAccess.isCurrentStateEqualStateRequired(deviceName, DevState.CLOSE)) {
                        ExecutionTracerService.trace(this, deviceName + " is closed");
                    } else {
                        ExecutionTracerService.trace(this, deviceName + " was not closed correctly");
                    }
                } else {
                    ExecutionTracerService.trace(this, "opening " + deviceName);
                    waitTask = new WaitStateTask(deviceName, DevState.RUNNING, 1000, false);
                    waitTask.run();
                    if (waitTask.hasFailed()) {
                        throw waitTask.getDevFailed();
                    }

                    // Bug 22954
                    if (TangoAccess.isCurrentStateEqualStateRequired(deviceName, DevState.OPEN)) {
                        ExecutionTracerService.trace(this, deviceName + " is opened");
                    } else {
                        ExecutionTracerService.trace(this, deviceName + " was not opened correctly");
                    }

                }
            }
            // sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
            response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());

        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        }
    }

    @Override
    protected void doStop() {
        if (waitTask != null) {
            waitTask.cancel();
        }
        super.doStop();
    }

}
