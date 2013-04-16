package fr.soleil.passerelle.actor.tango.control.motor.dataProviders;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.core.Port;

import fr.esrf.Tango.DevFailed;

public abstract class AttributeDataProvider {

    protected boolean throwExceptionIfAttributeIsNotInSource = false;

    public AttributeDataProvider() {
    }

    public AttributeDataProvider(boolean throwExceptionIfAttributeIsNotInSource) {
        this.throwExceptionIfAttributeIsNotInSource = throwExceptionIfAttributeIsNotInSource;
    }

    public abstract void setDeviceName(String deviceName);

    public abstract void init(Actor actor) throws DevFailed;

    /**
     * return the value of the "attribute"
     * 
     * @param actor the name of the actor which use the data. It's can be use in implemtation to
     *            trace message thanks to ExecutionTraceService
     * @param request the ProcessRequest given by the method process. it's use to extract message
     *            from port (like snapid)
     * @param inputPort the input port of actor use to retrieve data.
     * 
     * @return the extracted data if all goes well. Otherwise, there three possibilities:
     *         <ul>
     *         <li>the source can not be joined => throw ProcessingException</li>
     *         <li>the source can be joined, but data is not in source and
     *         throwExceptionIfAttributeIsNotInSource = true => throw ProcessingException</li>
     *         <li>the source can be joined, but data is not in source and
     *         throwExceptionIfAttributeIsNotInSource = false => return null</li>
     *         </ul>
     * 
     * @throws ProcessingException if an error occurred when we try to "connect" to source data or
     *             if throwExceptionIfAttributeIsNotInSource = true and data is not in source
     */
    public abstract String getData(Actor actor, ProcessRequest request, Port inputPort)
            throws ProcessingException;

    public abstract boolean attributeIsAvailable();
}
