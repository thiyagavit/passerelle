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

import ptolemy.actor.NoRoomException;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * @author ABEILLE
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@SuppressWarnings("serial")
public class SpectrumCalculator extends Transformer {

    private final static Logger logger = LoggerFactory.getLogger(SpectrumCalculator.class);

    public Parameter calculParam;
    private String calcul;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public SpectrumCalculator(final CompositeEntity container, final String name)
	    throws NameDuplicationException, IllegalActionException {
	super(container, name);

	input.setName("Spectrum (AttributeProxy)");
	output.setName("Calcul (Double)");

	calculParam = new StringParameter(this, "From(s) (actuators trajectory)");
	calculParam.addChoice("average");
	calculParam.addChoice("maximum");
	calculParam.setExpression("maximum");
	registerConfigurableParameter(calculParam);
    }

    @Override
    protected void doFire(final ManagedMessage message) throws ProcessingException {
	if (logger.isTraceEnabled()) {
	    logger.trace(getName() + " doFire() - entry");
	}

	TangoAttribute attrHelp = null;
	try {
	    final Object obj = message.getBodyContent();
	    if (obj instanceof TangoAttribute) {
		attrHelp = (TangoAttribute) obj;
	    }
	} catch (final MessageException e) {
	    ExceptionUtil.throwProcessingException("Message Exception", this, e);
	}

	double result = 0;

	try {
	    final Double[] values = attrHelp.extractSpecOrImage(Double.class);
	    if (calcul.equalsIgnoreCase("average")) {
		for (final Double value : values) {
		    result = result + value;
		}
		result = result / values.length;
	    } else if (calcul.equalsIgnoreCase("maximum")) {
		for (final Double value : values) {
		    if (value > result) {
			result = value;
		    }
		}
	    }

	} catch (final DevFailed e) {
	    ExceptionUtil.throwProcessingException(TangoToPasserelleUtil.getDevFailedString(e, this),
                    attrHelp.getAttributeProxy().fullName(),e);
	}

	final ManagedMessage resultMsg = createMessage();
	try {
	    resultMsg.setBodyContent(new Double(result), ManagedMessage.objectContentType);
	} catch (final MessageException e1) {
	    ExceptionUtil.throwProcessingException("Message Exception",attrHelp.getAttributeProxy()
                    .fullName(),e1);
        }
	try {
	    sendOutputMsg(output, resultMsg);
	} catch (final NoRoomException e2) {
	    ExceptionUtil.throwProcessingException("No room exception",attrHelp.getAttributeProxy()
                    .fullName(),e2);
	}
	if (logger.isTraceEnabled()) {
	    logger.trace(getName() + " doFire() - exit");
	}
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
	if (arg0 == calculParam) {
	    calcul = calculParam.getExpression().trim();
	} else {
	    super.attributeChanged(arg0);
	}
    }

    @Override
    protected String getExtendedInfo() {
	return this.getName();
    }

}
