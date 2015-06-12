package fr.soleil.passerelle.actor.tango.reporting;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
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
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.tango.util.FilterHelper;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

@SuppressWarnings("serial")
public class ReportAttributes extends ATangoDeviceActor {

    private final static Logger logger = LoggerFactory.getLogger(ReportAttributes.class);

    public Parameter attributeNameParam;
    private String attributeName;

    public ReportAttributes(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        super.setCreateDeviceProxy(false);
        attributeNameParam = new StringParameter(this, "attribute name");
        attributeNameParam.setExpression("*");

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
            ExecutionTracerService.trace(this, "MOCK - Get attributes OK");
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
                DeviceProxy dev = null;
                boolean started = true;
                try {
                    dev = ProxyFactory.getInstance().createDeviceProxy(device);
                    // see bug 22954
                    new TangoCommand(device, "State").execute();

                } catch (final DevFailed e) {
                    started = false;
                }
                if (!started) {
                    ExecutionTracerService.trace(this, device + " is stopped");
                } else {
                    String attributeVal = "";
                    try {
                        final String[] attrList = dev.get_attribute_list();
                        final Pattern pattern = Pattern.compile(FilterHelper.wildcardToRegex(attributeName),
                                Pattern.CASE_INSENSITIVE);
                        for (final String element : attrList) {
                            logger.debug("***test matching for " + device + "/" + element);
                            final Matcher matcher = pattern.matcher(element);
                            if (matcher.matches()) {
                                logger.debug("read " + device + "/" + element);
                                try {
                                    final TangoAttribute attr = new TangoAttribute(device + "/" + element);
                                    attributeVal = attr.readAsString("", "");
                                    ExecutionTracerService.trace(this, device + "/" + element + " value: "
                                            + attributeVal);
                                } catch (final DevFailed e) {
                                    ExecutionTracerService.trace(this, device + "/" + element + " ERROR ");
                                    TangoToPasserelleUtil.getDevFailedString(e, this);
                                }
                            }
                        }
                    } catch (final DevFailed e) {
                        ExecutionTracerService.trace(this, device + " ERROR ");
                        TangoToPasserelleUtil.getDevFailedString(e, this);
                    }
                }
            }
        }
        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == attributeNameParam) {
            attributeName = PasserelleUtil.getParameterValue(attributeNameParam);
        } else {
            super.attributeChanged(attribute);
        }
    }
}
