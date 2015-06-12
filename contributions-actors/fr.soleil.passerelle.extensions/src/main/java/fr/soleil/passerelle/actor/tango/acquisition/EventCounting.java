package fr.soleil.passerelle.actor.tango.acquisition;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class EventCounting extends ATangoDeviceActor {

    public Parameter integrationTimeParam;
    private double integrationTime = 1;// secs

    private TangoCommand cmd;
    private TangoAttribute intTime;

    private WaitStateTask waitTask;

    public EventCounting(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);

        // inputTrigger = PortFactory.getInstance().createInputPort(this,
        // "trigger", null);
        // outputEnd = PortFactory.getInstance().createOutputPort(this,"end");

        integrationTimeParam = new StringParameter(this, "Integration Time");
        integrationTimeParam.setExpression("1");
        input.setName("trigger");
        output.setName("end");
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
                cmd = new TangoCommand(getDeviceName(), "Start");
                intTime = new TangoAttribute(getDeviceName() + "/integrationTime");
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
            ExecutionTracerService.trace(this, "MOCK - Start counting ");
            ExecutionTracerService.trace(this, "MOCK - End counting ");

        } else {
            try {
                final String deviceName = getDeviceName();
                intTime.write(integrationTime);
                ExecutionTracerService.trace(this, "Start counting for " + integrationTime);
                cmd.execute();
                waitTask = new WaitStateTask(deviceName, DevState.RUNNING, 100, false);
                waitTask.run();
                if (waitTask.hasFailed()) {
                    throw waitTask.getDevFailed();
                }
                ExecutionTracerService.trace(this, "End counting");
                if (isRecordData()) {
                    DataRecorder.getInstance().saveDevice(this, deviceName);
                }
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        // sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());
    }

    @Override
    protected void doStop() {
        if (waitTask != null) {
            waitTask.cancel();
        }
        super.doStop();
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == integrationTimeParam) {
            integrationTime = Double.valueOf(((StringToken) integrationTimeParam.getToken()).stringValue());
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
        final EventCounting copy = (EventCounting) super.clone(workspace);
        copy.waitTask = null;
        copy.cmd = null;
        copy.intTime = null;
        return copy;
    }

}
