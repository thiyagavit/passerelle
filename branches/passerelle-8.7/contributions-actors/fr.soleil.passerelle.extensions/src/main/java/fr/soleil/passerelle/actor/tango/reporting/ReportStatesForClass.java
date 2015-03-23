package fr.soleil.passerelle.actor.tango.reporting;

import java.net.URL;

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
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoDs.TangoConst;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class ReportStatesForClass extends Transformer {

    private Database db;

    public Parameter classNameParam;
    private String className;

    public ReportStatesForClass(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        classNameParam = new StringParameter(this, "class name");
        classNameParam.setExpression("*");

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
        if (!isMockMode()) {
            try {
                db = ApiUtil.get_db_obj();
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        String[] devices = null;
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - Get states OK");
            sendOutputMsg(output, PasserelleUtil.createCopyMessage(this, arg0));
        } else {
            try {
                devices = db.get_device_name("*", className);
            } catch (final DevFailed e1) {
                ExceptionUtil.throwProcessingException(this, e1);
            }
            if (devices != null) {
                for (final String device : devices) {
                    if (isFinishRequested()) {
                        break;
                    }
                    // DeviceProxy dev = null;
                    // boolean started = true;
                    DevState state = null;
                    try {
                        // dev = ProxyFactory.getInstance().createDeviceProxy(device);
                        // see bug 22954
                        // new TangoCommand(device, "State").execute();
                        state = TangoAccess.getCurrentState(device);
                        ExecutionTracerService.trace(this,
                                device + " state: " + TangoConst.Tango_DevStateName[state.value()]);
                    } catch (final DevFailed e) {
                        // started = false;
                        ExecutionTracerService.trace(this, device + " is stopped");
                    }
                    /*if (!started) {
                    	ExecutionTracerService.trace(this, device + " is stopped");
                    } else {
                            ExecutionTracerService.trace(this, device + " state: "
                                                + TangoConst.Tango_DevStateName[state.value()]);
                    	DevState state = null;
                    	try {
                    		state = dev.state();
                    		ExecutionTracerService.trace(this, device + " state: "
                    				+ TangoConst.Tango_DevStateName[state.value()]);
                    	} catch (final DevFailed e) {
                    		ExecutionTracerService.trace(this, device + " ERROR ");
                    		TangoToPasserelleUtil.getDevFailedString(e, this);
                    	}
                    }*/
                }
            }
        }
        sendOutputMsg(output, PasserelleUtil.createCopyMessage(this, arg0));
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == classNameParam) {
            className = PasserelleUtil.getParameterValue(classNameParam);
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
