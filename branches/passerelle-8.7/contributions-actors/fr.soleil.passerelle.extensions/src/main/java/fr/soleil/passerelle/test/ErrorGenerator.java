package fr.soleil.passerelle.test;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.util.ExceptionUtil;

@SuppressWarnings("serial")
public class ErrorGenerator extends Transformer {

    public Parameter errorTypeParam;
    public String errorType = "init";
    public Parameter severityParam;
    public String severity = ExceptionUtil.FATAL_ERROR;

    public ErrorGenerator(CompositeEntity container, String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        errorTypeParam = new StringParameter(this, "error type");
        errorTypeParam.setExpression(errorType);
        errorTypeParam.addChoice("init");
        errorTypeParam.addChoice("fire");

        severityParam = new StringParameter(this, "severity");
        severityParam.setExpression(severity);
        severityParam.addChoice(ExceptionUtil.FATAL_ERROR);
        severityParam.addChoice(ExceptionUtil.NON_FATAL_ERROR);

    }

    @Override
    protected void doInitialize() throws InitializationException {
        // System.out.println( "error init actor");
        super.doInitialize();
        if (errorType.compareTo("init") == 0) {
            ExecutionTracerService.trace(this, "error init actor");
            ErrorCode errorCode = null;
            if (ExceptionUtil.FATAL_ERROR.equals(severity)) {
                errorCode = ErrorCode.FATAL;
            }
            ExceptionUtil.throwInitializationException(errorCode, "TEST ERROR INIT", this);
        }

    }

    @Override
    protected void doFire(ManagedMessage message) throws ProcessingException {
        if (errorType.compareTo("fire") == 0) {
            System.out.println("error doFire actor ");
            ExecutionTracerService.trace(this, "error doFire actor");
            ErrorCode errorCode = null;
            if (ExceptionUtil.FATAL_ERROR.equals(severity)) {
                errorCode = ErrorCode.FATAL;
            }
            ExceptionUtil.throwProcessingException(errorCode, "TEST ERROR", this);
        }
    }

    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == errorTypeParam)
            errorType = ((StringToken) errorTypeParam.getToken()).stringValue();
        else if (attribute == severityParam)
            severity = ((StringToken) severityParam.getToken()).stringValue();
        else
            super.attributeChanged(attribute);
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
