package fr.soleil.passerelle.actor.tango.control.motor.dataProviders;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.core.Port;

import fr.esrf.Tango.DevFailed;

/**
 * When attribute is not needed (eg for GalilAxisV5 actor, we don't care about offset)
 */
// TODO ADD log
public class NoAttributeDataProvider extends AttributeDataProvider {

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
        return null; // no need
    }

    @Override
    public boolean attributeIsAvailable() {
        return false;
    }
}
