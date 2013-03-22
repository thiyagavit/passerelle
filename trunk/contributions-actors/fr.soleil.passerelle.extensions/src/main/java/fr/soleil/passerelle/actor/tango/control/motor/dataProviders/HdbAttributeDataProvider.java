package fr.soleil.passerelle.actor.tango.control.motor.dataProviders;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.Level;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.snapshot.HdbExtractorProxy;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;
import fr.soleil.util.SoleilUtilities;

public class HdbAttributeDataProvider extends AttributeDataProvider {
    private String deviceName;
    private String attributeName;
    private HdbExtractorProxy extractorProxy;

    public HdbAttributeDataProvider(String attributeName, boolean throwExceptionOnError) {
        super(throwExceptionOnError);
        this.attributeName = attributeName;
    }

    @Override
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public void init(Actor actor) throws DevFailed {
        String hdbExtractor = SoleilUtilities.getDevicesFromClass("HdbExtractor")[0];
        ExecutionTracerService.trace(actor, "using hdb Extractor " + hdbExtractor, Level.DEBUG);

        extractorProxy = new HdbExtractorProxy(hdbExtractor);
    }

    @Override
    public String getData(Actor actor, ProcessRequest request, Port inputPort)
            throws ProcessingException {
        String positionValue = null;
        try {
            positionValue = extractorProxy.getLastScalarAttrValue(deviceName, attributeName);

        }
        catch (DevFailed devFailed) {
            if (throwExceptionIfAttributeIsNotInSource) {
                throw new ProcessingExceptionWithLog(actor, deviceName + "/" + attributeName
                        + " is not in Hdb or can not be read: ", null, null);
            } else {
                positionValue = null;
            }
        }

        return positionValue;
    }

    @Override
    public boolean attributeIsAvailable() {
        return true;
    }
}
