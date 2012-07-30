package fr.soleil.passerelle.actor.error;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.Actor;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.util.ExecutionTracerService;

@SuppressWarnings("serial")
public class ErrorGeneratorV3 extends Actor {

    public Parameter severityParam;
    public String severity = Severity.FATAL.toString();

    public Port input;
    public Port output;

    public Parameter messageParam;
    public String message;

    public ErrorGeneratorV3(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        messageParam = new StringParameter(this, "message");
        messageParam.setExpression("An error occured");

        severityParam = new StringParameter(this, "severity");
        severityParam.setExpression(severity);
        severityParam.addChoice(Severity.FATAL.toString());
        severityParam.addChoice(Severity.NON_FATAL.toString());

        input = PortFactory.getInstance().createInputPort(this, "in", null);
        input.setMode(PortMode.PUSH);
        output = PortFactory.getInstance().createOutputPort(this, "out");

    }

    @Override
    protected void process(final ActorContext arg0, final ProcessRequest arg1,
            final ProcessResponse arg2) throws ProcessingException {

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