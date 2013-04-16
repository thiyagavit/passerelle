package fr.soleil.passerelle.actor.tango.control.motor.dataProviders;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.core.Port;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * get the position from an input port
 */
public class PortAttributeDataProvider extends AttributeDataProvider {

    public PortAttributeDataProvider() {
    }

    @Override
    public void setDeviceName(String deviceName) {
        // no need
    }

    @Override
    public void init(Actor actor) throws DevFailed {
        // no need
    }

    @Override
    public String getData(Actor actor, ProcessRequest request, Port inputPort)
            throws ProcessingException {
        return (String) PasserelleUtil.getInputValue(request.getMessage(inputPort));
    }

    @Override
    public boolean attributeIsAvailable() {
        return true;
    }
}
