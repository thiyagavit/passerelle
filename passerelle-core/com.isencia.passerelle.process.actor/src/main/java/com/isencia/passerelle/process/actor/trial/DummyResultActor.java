/**
 * 
 */
package com.isencia.passerelle.process.actor.trial;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.edm.actor.common.SynchronousTaskBasedActor;
import com.isencia.passerelle.edm.engine.api.DiagnosisEntityFactory;
import com.isencia.passerelle.edm.engine.api.service.ServicesRegistry;
import com.isencia.passerelle.process.actor.activator.Activator;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * @author delerw
 *
 */
public class DummyResultActor extends SynchronousTaskBasedActor {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DummyResultActor.class);
	
	public StringParameter resultTypeParam;			// NOSONAR

	public StringParameter resultItemsParameter;		// NOSONAR

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public DummyResultActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		resultTypeParam = new StringParameter(this, RESULT_TYPE);
		resultTypeParam.setExpression(name);

        resultItemsParameter = new StringParameter(this, "Result items");
        new TextStyle(resultItemsParameter, "paramsTextArea");
	}

	@Override
	protected Logger getLogger() {
		return LOGGER ;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.edm.actor.common.AbstractTaskBasedActor#handle(java.lang.Class, com.isencia.passerelle.diagnosis.Context, java.util.Map)
	 */
	@Override
	protected Context handle(Context taskContext, Context flowContext, Map<String, String> actorAttributes) throws ProcessingException {
		try {
			DiagnosisEntityFactory diagnosisEntityFactory = ServicesRegistry.getInstance().getDiagnosisEntityFactory();
			ResultBlock rb = diagnosisEntityFactory.createResultBlock(resultTypeParam.getExpression(), (Task)taskContext.getRequest());
	
			String paramDefs = ((StringToken)resultItemsParameter.getToken()).stringValue();
	    	BufferedReader reader = new BufferedReader(new StringReader(paramDefs));
	    	String paramDef = null;
	    	while ((paramDef = reader.readLine()) != null) {
	   			String[] paramKeyValue = paramDef.split("=");
	   			if (paramKeyValue.length == 2) {
	   				diagnosisEntityFactory.createResultItem(paramKeyValue[0],paramKeyValue[1],null,rb);
	   			} else {
	   				ExecutionTracerService.trace(this, "Invalid mapping definition: " + paramDef);
	   			}
	    	}
		} catch (Exception e) {
			throw new ProcessingException("Error generating dummy results for "+resultTypeParam.getExpression(), taskContext, e);
		}
		
		return (Context)ContextManagerProxy.notifyFinished(taskContext);
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.edm.actor.common.AbstractTaskBasedActor#initActivatorOutsideOSGi()
	 */
	@Override
	protected void initActivatorOutsideOSGi() {
		Activator.initOutsideOSGi();
	}

}
