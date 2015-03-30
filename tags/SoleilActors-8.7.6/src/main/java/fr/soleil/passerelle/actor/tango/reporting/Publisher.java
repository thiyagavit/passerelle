package fr.soleil.passerelle.actor.tango.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoAttributeActor;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class Publisher extends ATangoAttributeActor {

    private final static Logger logger = LoggerFactory.getLogger(Publisher.class);

    public Publisher(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        input.setExpectedMessageContentType(String.class);
        recordDataParam.setVisibility(Settable.EXPERT);
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {
        logger.debug("process - in");
        final ManagedMessage message = request.getMessage(input);
        final Object inputValue = PasserelleUtil.getInputValue(message);

        try {
            final TangoAttribute attr = getTangoAttribute();
            final String attrName = attr.getAttributeProxy().name();
            if (attr.isScalar()) {
                if (!(inputValue instanceof String)) {
                    ExceptionUtil.throwProcessingException("cannot publish input of type " + inputValue.getClass(),
                            attrName);
                }
                final String value = (String) inputValue;
                ExecutionTracerService.trace(this, "Publishing " + value + " to " + attrName);
                attr.write(value);

            } else if (attr.isSpectrum()) {
                final List<String> values = new ArrayList<String>();

                values.add(attrName);
                if (inputValue instanceof String) {
                    final List<String> value = Arrays.asList(((String) inputValue).split(","));
                    ExecutionTracerService.trace(this, "Publishing " + value + " to " + attrName);
                    values.addAll(value);
                } else if (inputValue.getClass().isArray()) {
                    final List<String> value = Arrays.asList((String[]) inputValue);
                    ExecutionTracerService.trace(this, "Publishing " + value + " to " + attrName);
                    values.addAll(value);
                } else {
                    ExceptionUtil.throwProcessingException("cannot publish input of type " + inputValue.getClass(),
                            attrName);
                }

                final TangoCommand publish = new TangoCommand(attr.getDeviceName(), "WriteSpectrumAttribute");
                Object[] argin = values.toArray(new String[] {});
                publish.execute(argin);
            } else {
                // not supported because it is impossible to guess the
                // dimensions.
                ExceptionUtil.throwProcessingException("publish on image not supported", attrName);
            }

            response.addOutputMessage(0, output, PasserelleUtil.createCopyMessage(this, message));

            logger.debug("process - out");
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        } catch (final PasserelleException e) {
            ExceptionUtil.throwProcessingException(e.getMessage(), this, e);
        }

    }

}
