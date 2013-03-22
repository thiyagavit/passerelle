package fr.soleil.passerelle.actor.tango.control.motor.dataProviders;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.Level;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.snapshot.SnapExtractorProxy;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;
import fr.soleil.util.SoleilUtilities;

public class SnapAttributeDataProvider extends AttributeDataProvider {
    private String completeAttributeName;
    private String attributeName;
    private SnapExtractorProxy extractorProxy;

    public SnapAttributeDataProvider(String attributeName, boolean throwExceptionOnError) {
        super(throwExceptionOnError);
        this.attributeName = attributeName;
    }

    @Override
    public void setDeviceName(String deviceName) {
        completeAttributeName = deviceName + "/" + attributeName;
    }

    @Override
    public void init(Actor actor) throws DevFailed {
        String snapExtractorName = SoleilUtilities.getDevicesFromClass("SnapExtractor")[0];
        ExecutionTracerService.trace(actor, "using snap Extractor " + snapExtractorName,
                Level.DEBUG);

        extractorProxy = new SnapExtractorProxy(snapExtractorName);
    }

    @Override
    public String getData(Actor actor, ProcessRequest request, Port inputPort)
            throws ProcessingException {

        String snapId = (String) PasserelleUtil.getInputValue(request.getMessage(inputPort));

        String attributeValue = null;
        try {
            attributeValue = extractorProxy.getReadValues(snapId, completeAttributeName)[0];

        }
        catch (DevFailed devFailed) {
            throw new DevFailedProcessingException(devFailed, actor);
        }

        if (attributeValue.trim().equalsIgnoreCase("NULL")) {
            if (throwExceptionIfAttributeIsNotInSource) {
                throw new ProcessingExceptionWithLog(actor, attributeName + " is not in snap: "
                        + snapId, null, null);
            } else {
                attributeValue = null;
            }
        }
        return attributeValue;
    }

    @Override
    public boolean attributeIsAvailable() {
        return true;
    }
}
