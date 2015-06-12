/*	Synchrotron Soleil 
 *  
 *   File          :  StringsGenerator.java
 *  
 *   Project       :  passerelle-soleil
 *  
 *   Description   :  
 *  
 *   Author        :  ABEILLE
 *  
 *   Original      :  30 mars 2006 
 *  
 *   Revision:  					Author:  
 *   Date: 							State:  
 *  
 *   Log: StringsGenerator.java,v 
 *
 */
 /*
 * Created on 30 mars 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * @author ABEILLE
 *
 */
@SuppressWarnings("serial")
public class StringsGenerator extends Transformer {
	
	private final static Logger logger = LoggerFactory.getLogger(StringsGenerator.class);
	
	Parameter stringsListParam;
	String[] stringsList;
	int currentIndex = 0;
	
	public StringsGenerator(CompositeEntity arg0, String arg1)
			throws NameDuplicationException, IllegalActionException {
		super(arg0, arg1);
		
		input.setName("Trigger");
		output.setName("String");
		
		stringsListParam = new StringParameter(this,
		"Strings List (separated by commas)");
		stringsListParam.setExpression("s1,s2,s3");
		registerConfigurableParameter(stringsListParam);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute)
	 */
	public void attributeChanged(Attribute arg0) throws IllegalActionException {
		if (arg0 == stringsListParam) {
			stringsList = stringsListParam.getExpression().trim().split(",");	
			currentIndex = 0;
		} else
			super.attributeChanged(arg0);
	}
	protected void doFire(ManagedMessage arg0) throws ProcessingException {
		
		if (logger.isTraceEnabled())
			logger.trace(getName()+" doFire() - entry");
		
		String currentString = stringsList[currentIndex];
		if (currentIndex == stringsList.length - 1)
			currentIndex = 0;
		else
			currentIndex++;	
		ExecutionTracerService.trace(this, "Generating string :"+ currentString);
		sendOutputMsg(output,PasserelleUtil.createContentMessage(this, currentString));
		
		
		if (logger.isTraceEnabled())
			logger.trace(getName()+" doFire() - exit");

	}

	protected String getExtendedInfo() {
		return this.getName();
	}

}
