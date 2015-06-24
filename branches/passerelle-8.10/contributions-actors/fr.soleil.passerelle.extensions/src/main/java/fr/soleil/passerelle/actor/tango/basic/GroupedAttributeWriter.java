package fr.soleil.passerelle.actor.tango.basic;

import org.tango.utils.TangoUtil;

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
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoActor;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.FilterHelper;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoGroupAttribute;

/**
 * Write a value on a group of devices
 * 
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class GroupedAttributeWriter extends ATangoActor {

    private static final String ATTRIBUTE_NAMES = "Attribute Names";

    // /** The input ports */
    // public Port inputValue;
    //
    // /** The output ports */
    // public Port output;

    /**
     * The list of attributes to write
     */
    @ParameterName(name = ATTRIBUTE_NAMES)
    public Parameter attributeNameParam;
    private String attributeNames;
    private String[] attributeNameList;

    private TangoGroupAttribute attr;

    public GroupedAttributeWriter(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);

        // inputValue = PortFactory.getInstance().createInputPort(this, "Value",
        // null);
        // output = PortFactory.getInstance().createOutputPort(this,"Output");

        input.setName("Value");
        input.setExpectedMessageContentType(String.class);

        attributeNameParam = new StringParameter(this, ATTRIBUTE_NAMES);
        attributeNameParam.setExpression("tango/tangotest/1/short_scalar,tango/tangotest/1/double_scalar");
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
                if (attributeNames.contains(",")) {
                    attributeNameList = attributeNames.split(",");
                } else {
                    attributeNameList = FilterHelper.getDevicesForPatternAsArray(attributeNames);
                }
                attr = new TangoGroupAttribute(attributeNameList);
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        final ManagedMessage message = request.getMessage(input);

        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - Writing attributes");
        } else {
            try {
                ExecutionTracerService.trace(this, "Writing attributes on ");
                for (final String attributeName : attributeNameList) {
                    ExecutionTracerService.trace(this, "\t - " + attributeName);
                }
                final String inputValue = (String) PasserelleUtil.getInputValue(message);
                attr.write(inputValue);
                // GroupReplyList reply = grp.write_attribute_reply(10);
                // ExecutionTracerService.trace(this,"has failed:
                // "+reply.has_failed());
                /*
                 * GroupAttrReplyList reply = grp.read_attribute_reply();
                 * ExecutionTracerService .trace(this,"has failed:
                 * "+reply.has_failed()); GroupAttrReply r1 =
                 * (GroupAttrReply)reply.get(0); DeviceAttribute da1 =
                 * r1.get_data(); ExecutionTracerService.trace (this,"result:
                 * "+da1.extractShort()); GroupAttrReply r =
                 * (GroupAttrReply)reply.get(1); DeviceAttribute da =
                 * r.get_data(); ExecutionTracerService.trace(this,"result:
                 * "+da. extractDouble());
                 */
                if (isRecordData()) {
                    for (final String attributeName : attributeNameList) {
                        final String deviceName = TangoUtil.getfullDeviceNameForAttribute(attributeName);
                        DataRecorder.getInstance().saveDevice(this, deviceName);
                        if (isFinishRequested()) {
                            break;
                        }
                    }
                }
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        // sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == attributeNameParam) {
            attributeNames = PasserelleUtil.getParameterValue(attributeNameParam);
        } else {
            super.attributeChanged(arg0);
        }
    }
}
