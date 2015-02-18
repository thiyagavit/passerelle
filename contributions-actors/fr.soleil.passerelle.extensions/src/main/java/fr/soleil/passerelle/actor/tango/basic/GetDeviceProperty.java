package fr.soleil.passerelle.actor.tango.basic;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class GetDeviceProperty extends ATangoDeviceActor {

    public Parameter propertyNameParam;
    private String propertyName;

    public GetDeviceProperty(final CompositeEntity container, final String name)
	    throws NameDuplicationException, IllegalActionException {
	super(container, name);
	propertyNameParam = new StringParameter(this, "property name");
	propertyNameParam.setExpression("prop");

	recordDataParam.setVisibility(Settable.EXPERT);
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request,
	    final ProcessResponse response) throws ProcessingException {
	try {
	    final DbDatum result = getDeviceProxy().get_property(propertyName);
	    final String value = result.extractString();
	    ExecutionTracerService.trace(this, "property " + propertyName + " of "
		    + getDeviceName() + " is " + value);
	    response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, value));
	} catch (final DevFailed e) {
	    throw new DevFailedProcessingException(e, this);
	} catch (final PasserelleException e) {
	    throw new ProcessingException("cannot get property", null, e);
	}

    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
	if (arg0 == propertyNameParam) {
	    propertyName = PasserelleUtil.getParameterValue(propertyNameParam);
	} else {
	    super.attributeChanged(arg0);
	}
    }

}
