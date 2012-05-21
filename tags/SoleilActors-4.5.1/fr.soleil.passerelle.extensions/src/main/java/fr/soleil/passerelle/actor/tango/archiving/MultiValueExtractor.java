package fr.soleil.passerelle.actor.tango.archiving;

import java.text.SimpleDateFormat;
import java.util.Date;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.diagnosis.Event;
import com.isencia.passerelle.diagnosis.impl.entities.ResultItemImpl;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.ptolemy.DateTimeParameter;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarDoubleStringArray;
import fr.soleil.passerelle.util.DateUtils;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class MultiValueExtractor extends AArchivingExtractor {

    public Parameter startDateParam;
    private Date startDate;

    public Parameter endDateParam;
    private Date endDate;

    private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy H:m:s");

    public MultiValueExtractor(final CompositeEntity container, final String name) throws IllegalActionException,
	    NameDuplicationException {
	super(container, name);

	commandNameParam.setExpression("ExtractBetweenDates");

	startDateParam = new DateTimeParameter(this, "start date");
	endDateParam = new DateTimeParameter(this, "end date");
    }

    @Override
    protected void process(final ActorContext ctx, final ProcessRequest request, final ProcessResponse response)
	    throws ProcessingException {
	// TODO: allow to extract write part
	try {
	    final String[] attributeNames = getAttributeNames();
	    final TangoCommand command = getTangoCommand();
	    for (final String attributeName : attributeNames) {

		if (isMockMode()) {
		    command.execute(attributeName, new DevVarDoubleStringArray(new double[] { 10 },
			    new String[] { attributeName }));
		} else {
		    command.execute(attributeName, format.format(startDate), format.format(endDate));
		    final double[] timestamps = command.getNumDoubleMixArrayArgout();
		    final String[] values = command.getStringMixArrayArgout();
		    for (int i = 0; i < values.length; i++) {
			final Date date = new Date((long) timestamps[i]);
			final Event evt = new ResultItemImpl(attributeName, values[i], null, date);
			ExecutionTracerService.trace(
				this,
				attributeName + " extraction is : [timestamp= "
					+ DateUtils.format(date, "yyyy/MM/dd HH:mm:ss") + ",value=" + values[i] + "]");
			final ManagedMessage message = createMessage(evt, ManagedMessage.objectContentType);
			response.addOutputMessage(0, output, message);
			if (isFinishRequested()) {
			    break;
			}
		    }
		}
	    }
	} catch (final DevFailed e) {
	    throw new DevFailedProcessingException(e, this);
	} catch (final PasserelleException e) {
	    throw new ProcessingException(e.getMessage(), getDeviceName(), e);
	}

    }

    @Override
    public void attributeChanged(final Attribute attr) throws IllegalActionException {
	if (attr == startDateParam) {
	    startDate = ((DateTimeParameter) startDateParam).getDateValue();
	} else if (attr == endDateParam) {
	    endDate = ((DateTimeParameter) endDateParam).getDateValue();
	} else {
	    super.attributeChanged(attr);
	}
    }
}
