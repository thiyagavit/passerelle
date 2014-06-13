package fr.soleil.passerelle.actor.tango.basic;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;

@SuppressWarnings("serial")
public class SetDeviceProperty extends DeviceProperty {
    

    public SetDeviceProperty(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setExpectedMessageContentType(String.class);
        input.setName("value");        
    }
    
    @Override
    protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {
        
        try {
            final ManagedMessage message = request.getMessage(input);
            final String value = (String) PasserelleUtil.getInputValue(message);
            
            DbDatum setData = new DbDatum(propertyName,value);
            getDeviceProxy().put_property(setData);
            ExecutionTracerService.trace(this, "property " + propertyName + " of "
                    + getDeviceName() + " is " + value);
            sendOutputMsg(output, PasserelleUtil.createContentMessage(this, value));
        } catch (final DevFailed e) {
            throw new DevFailedProcessingException(e, this);
        } catch (final PasserelleException e) {
            throw new ProcessingExceptionWithLog(this,"cannot get property", null, e);
        }
        
    }
}
