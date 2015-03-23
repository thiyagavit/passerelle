package fr.soleil.passerelle.actor.tango.basic;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class GetDeviceProperty extends DeviceProperty {

    public GetDeviceProperty(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
    }

    @Override
    protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        try {
            final DbDatum result = getDeviceProxy().get_property(propertyName);
            final String value = result.extractString();
            ExecutionTracerService.trace(this, "property " + propertyName + " of " + getDeviceName() + " is " + value);
            sendOutputMsg(output, PasserelleUtil.createContentMessage(this, value));
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        } catch (final PasserelleException e) {
            ExceptionUtil.throwProcessingExceptionWithLog(this, "cannot get property", this, e);
        }

    }
}
