/*	Synchrotron Soleil 
 *  
 *   File          :  AttributeReader.java
 *  
 *   Project       :  passerelle-soleil
 *  
 *   Description   :  
 *  
 *   Author        :  ABEILLE
 *  
 *   Original      :  19 mai 2005 
 *  
 *   Revision:  					Author:  
 *   Date: 							State:  
 *  
 *   Log: AttributeReader.java,v 
 *
 */
/*
 * Created on 19 mai 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.tango.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoAttributeActor;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * Reads a Tango attribute and output the value
 * 
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class AttributeReader extends ATangoAttributeActor {

    private final static Logger logger = LoggerFactory.getLogger(AttributeReader.class);

    // /** The input ports */
    // public Port inputTrigger;
    //	
    // /** The output ports */
    // public Port outputAttribute;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public AttributeReader(final CompositeEntity container, final String name)
	    throws NameDuplicationException, IllegalActionException {
	super(container, name);

	input.setName("Trigger");
	output.setName("Attribute");
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request,
	    final ProcessResponse response) throws ProcessingException {

	if (logger.isTraceEnabled()) {
	    logger.trace(getName() + " doFire() - entry");
	}

	if (isMockMode()) {
	    ExecutionTracerService.trace(this, "MOCK - read attribute " + getAttributeName());
	    // sendOutputMsg(output, PasserelleUtil.createContentMessage(this,
	    // 10.0));
	    response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, 10.0));

	} else {
	    // read attribute
	    try {
		getTangoAttribute().update();
		if (getTangoAttribute().getAttributeProxy().get_info().data_format == AttrDataFormat.SCALAR) {
		    ExecutionTracerService.trace(this, "read Attribute " + getAttributeName()
			    + ", value: " + getTangoAttribute().readAsString(",", ""));
		} else if (getTangoAttribute().getAttributeProxy().get_info().data_format == AttrDataFormat.SPECTRUM) {
		    ExecutionTracerService.trace(this, "read Attribute " + getAttributeName()
			    + ", value: " + getTangoAttribute().readAsString(",", ""));
		} else if (getTangoAttribute().getAttributeProxy().get_info().data_format == AttrDataFormat.IMAGE) {
		    ExecutionTracerService.trace(this, "read Attribute " + getAttributeName()
			    + " (image) ");
		}

		// save data if necessary
		if (isRecordData()) {
		    try {
			final String deviceName = getTangoAttribute().getDeviceName();
			DataRecorder.getInstance().saveDevice(this, deviceName);
			DataRecorder.getInstance().saveExperimentalData(this, deviceName);
		    } catch (final DevFailed e) {
			// since some devices are not registered in the
			// datarecorder config
			// either as technical data or Experimental data, it is
			// not an error.
			TangoToPasserelleUtil.getDevFailedString(e, this);
			// throw new DevFailedProcessingException(e,this);
		    }
		}

		// send attribute to output
		// sendOutputMsg(output,
		// PasserelleUtil.createContentMessage(this,
		// getTangoAttribute()));
		response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this,
			getTangoAttribute()));

	    } catch (final DevFailed e) {
	        ExceptionUtil.throwProcessingException(this, e);
	    } catch (final PasserelleException e) {
	        ExceptionUtil.throwProcessingException(e.getMessage(), getAttributeName(),e);
	    }
	}

	if (logger.isTraceEnabled()) {
	    logger.trace(getName() + " doFire() - exit");
	}
    }

    @Override
    protected String getExtendedInfo() {
	return this.getName();
    }
}
