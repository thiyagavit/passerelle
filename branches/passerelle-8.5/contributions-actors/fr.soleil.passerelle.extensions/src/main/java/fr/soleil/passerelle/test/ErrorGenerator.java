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
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

@SuppressWarnings("serial")
public class ErrorGenerator extends Transformer{

	public Parameter errorTypeParam;
	public String errorType ="init";
	public Parameter severityParam;
	public String severity = Severity.FATAL.toString();
	
	public ErrorGenerator(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		errorTypeParam = new StringParameter(this, "error type");
		errorTypeParam.setExpression(errorType);
		errorTypeParam.addChoice("init");
		errorTypeParam.addChoice("fire");
		
		severityParam = new StringParameter(this, "severity");
		severityParam.setExpression(severity);
		severityParam.addChoice(Severity.FATAL.toString());
		severityParam.addChoice(Severity.NON_FATAL.toString());
		
	}

	@Override
	protected void doInitialize() throws InitializationException {
	//System.out.println( "error init actor");
		super.doInitialize();
		if(errorType.compareTo("init")==0){
			ExecutionTracerService.trace(this, "error init actor");
			Severity s = Severity.NON_FATAL;
	        if(Severity.FATAL.toString().equals(severity)) {
	            s = Severity.FATAL;
	        }
			throw new InitializationException(s,"TEST ERROR INIT",null,null);
		}
		
	}

	@Override
	protected void doFire(ManagedMessage message) throws ProcessingException {	
		if(errorType.compareTo("fire")==0){
			System.out.println( "error doFire actor ");
			ExecutionTracerService.trace(this, "error doFire actor");
			Severity s = Severity.NON_FATAL;
	        if(Severity.FATAL.toString().equals(severity)) {
	            s = Severity.FATAL;
	        }
			throw new ProcessingException(s,"TEST ERROR",null,null);
		}
		//sendOutputMsg(output, MessageFactory.getInstance().createTriggerMessage());
	}
	
	@Override
	public void attributeChanged(Attribute attribute) throws IllegalActionException {
		if(attribute == errorTypeParam)
			errorType = ((StringToken) errorTypeParam.getToken()).stringValue();
		else if(attribute == severityParam)
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
