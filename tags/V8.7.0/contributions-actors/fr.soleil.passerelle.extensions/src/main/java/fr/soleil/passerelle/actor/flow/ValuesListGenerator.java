/*	Synchrotron Soleil 
 *  
 *   File          :  ValuesGenerator.java
 *  
 *   Project       :  passerelle-soleil
 *  
 *   Description   :  
 *  
 *   Author        :  ABEILLE
 *  
 *   Original      :  26 mai 2005 
 *  
 *   Revision:  					Author:  
 *   Date: 							State:  
 *  
 *   Log: ValuesGenerator.java,v 
 *
 */
/*
 * Created on 26 mai 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * @author ABEILLE
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@SuppressWarnings("serial")
public class ValuesListGenerator extends Transformer {

	private final static Logger LOGGER = LoggerFactory.getLogger(ValuesListGenerator.class);
	
	public Parameter valuesListParam;
	String[] valuesList;

	String currentValue = "";
	int currentIndex = 0;
	/**
	 * @param container
	 * @param name
	 * @throws ptolemy.kernel.util.NameDuplicationException
	 * @throws ptolemy.kernel.util.IllegalActionException
	 */
	public ValuesListGenerator(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		input.setName("Trigger");
		output.setName("Value");

		valuesListParam = new StringParameter(this,
				"Values List (sep by commas)");
		valuesListParam.setExpression("1,3,5,10");
		registerConfigurableParameter(valuesListParam);

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute)
	 */
	public void attributeChanged(Attribute arg0) throws IllegalActionException {

		if (arg0 == valuesListParam) {
			String[] table = ((StringToken)valuesListParam.getToken()).stringValue().trim().split(",");
			valuesList = new String[table.length];
			for (int i = 0; i < table.length; i++) {
				valuesList[i] = table[i];
			}
		} else
			super.attributeChanged(arg0);
	}
	
	@Override
	protected void doInitialize() throws InitializationException {
		currentIndex = 0;
		currentValue = valuesList[currentIndex];	
		super.doInitialize();
	}
	
	protected Logger getLogger() {
	    return LOGGER;
	  }
	protected void doFire(ManagedMessage message) throws ProcessingException {
		getLogger().trace("{} doFire() - entry",getFullName());
		
		currentValue = valuesList[currentIndex];
		ExecutionTracerService.trace(this, "Generating value :"+ currentValue);
					
		if (currentIndex == valuesList.length - 1)
			currentIndex = 0;
		else
			currentIndex++;
		
		sendOutputMsg(output,PasserelleUtil.createContentMessage(this, currentValue));
		
		getLogger().trace("{} doFire() - exit",getFullName());
	}

}
