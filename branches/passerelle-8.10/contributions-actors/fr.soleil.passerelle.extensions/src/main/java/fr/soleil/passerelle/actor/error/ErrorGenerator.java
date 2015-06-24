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
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.util.ExceptionUtil;

@SuppressWarnings("serial")
public class ErrorGenerator extends Transformer {

    private final static Logger logger = LoggerFactory.getLogger(ErrorGenerator.class);
    public Parameter severityParam;
    public String severity = ExceptionUtil.FATAL_ERROR;

    public Parameter messageParam;
    public String message;

    public ErrorGenerator(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        messageParam = new StringParameter(this, "message");
        messageParam.setExpression("An error occured");

        severityParam = new StringParameter(this, "severity");
        severityParam.setExpression(severity);
        severityParam.addChoice(ExceptionUtil.FATAL_ERROR);
        severityParam.addChoice(ExceptionUtil.NON_FATAL_ERROR);

    }

    @Override
    protected void doFire(final ManagedMessage message) throws ProcessingException {
        logger.debug("error doFire actor ");
        ExecutionTracerService.trace(this, "Error message: " + this.message);
        ErrorCode error = ErrorCode.INFO;
        if (ExceptionUtil.FATAL_ERROR.equals(severity)) {
            error = ErrorCode.FATAL;
            ExceptionUtil.throwProcessingException(error, this.message,this);
        }
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
   
}