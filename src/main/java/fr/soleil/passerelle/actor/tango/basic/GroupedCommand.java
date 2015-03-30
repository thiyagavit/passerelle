package fr.soleil.passerelle.actor.tango.basic;

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
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.FilterHelper;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoGroupCommand;

/**
 * Execute a command on a group of devices.
 * 
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class GroupedCommand extends Transformer {

    private static final String COMMAND_NAME = "Command Name";
    private static final String DEVICE_LIST = "Device List";

    /**
     * The devices on which to execute the command.
     */
    @ParameterName(name = DEVICE_LIST)
    public Parameter deviceNameParam;
    private String deviceNames;
    private String[] deviceNameList;

    /**
     * The command name.
     */
    @ParameterName(name = COMMAND_NAME)
    public Parameter commandNameParam;
    private String commandName = "";
    TangoGroupCommand group;

    public GroupedCommand(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);
        deviceNameParam = new StringParameter(this, DEVICE_LIST);
        deviceNameParam.setExpression("device1,device2");
        commandNameParam = new StringParameter(this, COMMAND_NAME);
        commandNameParam.setExpression(commandName);
        input.setName("argin");
        input.setExpectedMessageContentType(String.class);

        final URL url = this.getClass().getResource("/fr/soleil/tango/tango.jpg");
        _attachText("_iconDescription", "<svg>\n"
                + " <image x=\"-20\" y=\"-20\" width =\"40\" height=\"40\" xlink:href=\"" + url + "\"/>\n"
                + "<line x1=\"-20\" y1=\"20\" x2=\"-20\" y2=\"-20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-20\" y1=\"-20\" x2=\"20\" y2=\"-20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"19\" x2=\"-19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"18\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-20\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-19\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "</svg>\n");

    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
                if (deviceNames.contains(",")) {
                    deviceNameList = deviceNames.split(",");
                } else {
                    deviceNameList = FilterHelper.getDevicesForPatternAsArray(deviceNames);
                }
                group = new TangoGroupCommand("Command", commandName, deviceNameList);
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - executing " + commandName + " on:");
            for (final String deviceName : deviceNameList) {
                ExecutionTracerService.trace(this, "\tMOCK - " + deviceName);
            }
        } else {
            try {
                // -------------------insert argin---------------------------
                if (group.isArginScalar()) {
                    // scalar
                    final String argin = (String) PasserelleUtil.getInputValue(arg0);
                    group.insert(argin);
                    ExecutionTracerService.trace(this, "executing " + commandName + " with value " + argin + " on:");
                } else if (group.isArginSpectrum()) {
                    // simple spectrum
                    final String argin = (String) PasserelleUtil.getInputValue(arg0);
                    Object[] arginArray = argin.split(",");
                    group.insert(arginArray);
                    ExecutionTracerService.trace(this, "executing " + commandName + " with value " + argin + " on:");
                } else if (group.isArginMixFormat()) {
                    // mix spectrum
                    ExceptionUtil.throwProcessingExceptionWithLog(this, "command argin type not supported", this);

                } else {
                    // void argin
                    ExecutionTracerService.trace(this, "executing " + commandName + " on:");
                }
                for (final String deviceName : deviceNameList) {
                    ExecutionTracerService.trace(this, "\t - " + deviceName);
                }
                // -------------------execute---------------------------
                group.execute();
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == deviceNameParam) {
            deviceNames = PasserelleUtil.getParameterValue(deviceNameParam);
        } else if (arg0 == commandNameParam) {
            commandName = ((StringToken) commandNameParam.getToken()).stringValue();
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
