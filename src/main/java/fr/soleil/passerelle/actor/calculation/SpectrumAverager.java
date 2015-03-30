/*	Synchrotron Soleil 
 *  
 *   File          :  SpectrumAverager.java
 *  
 *   Project       :  passerelle-soleil
 *  
 *   Description   :  
 *  
 *   Author        :  ABEILLE
 *  
 *   Original      :  11 mai 2005 
 *  
 *   Revision:  					Author:  
 *   Date: 							State:  
 *  
 *   Log: SpectrumAverager.java,v 
 *
 */
/*
 * Created on 11 mai 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.calculation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * @author ABEILLE
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@SuppressWarnings("serial")
public class SpectrumAverager extends Transformer {

	private final static Logger logger = LoggerFactory.getLogger(SpectrumAverager.class);

	/**
	 * @param container
	 * @param name
	 * @throws ptolemy.kernel.util.NameDuplicationException
	 * @throws ptolemy.kernel.util.IllegalActionException
	 */
	public SpectrumAverager(final CompositeEntity container, final String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		input.setName("Spectrum");
		input.setExpectedMessageContentType(String.class);
		output.setName("Average (Double)");
	}

	@Override
	protected void doFire(final ManagedMessage message)
			throws ProcessingException {
		if (logger.isTraceEnabled()) {
			logger.trace(getName() + " doFire() - entry");
		}

		final String[] values = ((String) PasserelleUtil.getInputValue(message))
				.split(",");
		double average = 0;

		for (final String value : values) {
			average = average + new Double(value);
		}
		average = average / values.length;

		sendOutputMsg(output, PasserelleUtil
				.createContentMessage(this, average));

		if (logger.isTraceEnabled()) {
			logger.trace(getName() + " doFire() - exit");
		}
	}

	@Override
	protected String getExtendedInfo() {
		return this.getName();
	}

}
