package fr.soleil.passerelle.actor.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

@SuppressWarnings("serial")
public class ErrorGenerator extends Transformer {

    private final static Logger logger = LoggerFactory.getLogger(ErrorGenerator.class);
    public Parameter severityParam;
    public String severity = Severity.FATAL.toString();

    public Parameter messageParam;
    public String message;

    public ErrorGenerator(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        messageParam = new StringParameter(this, "message");
        messageParam.setExpression("An error occured");

        severityParam = new StringParameter(this, "severity");
        severityParam.setExpression(severity);
        severityParam.addChoice(Severity.FATAL.toString());
        severityParam.addChoice(Severity.NON_FATAL.toString());

    }

    @Override
    protected void doFire(final ManagedMessage message) throws ProcessingException {
        logger.debug("error doFire actor ");
        ExecutionTracerService.trace(this, "Error message: " + this.message);
        Severity s = Severity.NON_FATAL;
        if (Severity.FATAL.toString().equals(severity)) {
            s = Severity.FATAL;
        }
        throw new ProcessingException(s, this.message, null, null);
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == messageParam) {
            message = ((StringToken) messageParam.getToken()).stringValue();
        } else if (attribute == severityParam) {
            severity = ((StringToken) severityParam.getToken()).stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}