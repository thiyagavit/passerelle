/*
 * Synchrotron Soleil
 * 
 * File : AttributeReader.java
 * 
 * Project : passerelle-soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 19 mai 2005
 * 
 * Revision: Author:
 * Date: State:
 * 
 * Log: AttributeReader.java,v
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
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * Read the write part (the set point) of a Tango attribute.
 * 
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class AttributeWritePartReader extends ATangoAttributeActor {

    private final static Logger logger = LoggerFactory.getLogger(AttributeReader.class);

    // /** The input ports */
    // public Port inputTrigger;
    //
    // /** The output ports */
    // public Port outputAttribute;

    // private String datarecorderName;
    // private boolean save = false;
    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public AttributeWritePartReader(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // inputTrigger = PortFactory.getInstance().createInputPort(this,
        // "Trigger", null);
        // outputAttribute =
        // PortFactory.getInstance().createOutputPort(this,"Attribute");

        input.setName("Trigger");
        output.setName("Attribute");

    }

    // @Override
    // protected void doFire(final ManagedMessage message)
    // throws ProcessingException {
    // if (logger.isTraceEnabled()) {
    // logger.trace(getInfo() + " doFire() - entry");
    // }
    // String value = "";
    // // read attribute
    // if (isMockMode()) {
    // ExecutionTracerService
    // .trace(this, "MOCK - read write part of attribute "
    // + getAttributeName());
    // sendOutputMsg(output, PasserelleUtil.createContentMessage(this,
    // 10.0));
    // } else {
    // try {
    // getTangoAttribute().read();
    // value = getTangoAttribute().extractWrite(String.class);
    // if (getTangoAttribute().getAttributeProxy().get_info().data_format ==
    // AttrDataFormat.SCALAR) {
    // ExecutionTracerService.trace(this,
    // "read write part of Attribute "
    // + getAttributeName() + ", value: " + value);
    // } else if (getTangoAttribute().getAttributeProxy().get_info().data_format
    // == AttrDataFormat.SPECTRUM) {
    // ExecutionTracerService.trace(this, "read Attribute "
    // + getAttributeName() + " (spectrum) ");
    // } else if (getTangoAttribute().getAttributeProxy().get_info().data_format
    // == AttrDataFormat.IMAGE) {
    // ExecutionTracerService.trace(this, "read Attribute "
    // + getAttributeName() + " (image) ");
    // }
    //
    // // save data if necessary
    // if (isRecordData()) {
    // DataRecorder.saveDevice(this, getTangoAttribute()
    // .getDeviceName());
    // }
    //
    // } catch (final DevFailed e) {
    // throw new DevFailedProcessingException(e, this);
    // } catch (final PasserelleException e) {
    // throw new ProcessingException(e.getMessage(),
    // getAttributeName(), e);
    // }
    //
    // // send attribute to output
    // sendOutputMsg(output, PasserelleUtil.createContentMessage(this,
    // value));
    // }
    // if (logger.isTraceEnabled()) {
    // logger.trace(getInfo() + " doFire() - exit");
    // }
    // }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - entry");
        }
        String value = "";
        // read attribute
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - read write part of attribute " + getAttributeName());
            // sendOutputMsg(output, PasserelleUtil.createContentMessage(this,
            // 10.0));
            response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, 10.0));

        } else {
            try {
                value = getTangoAttribute().readWritten(String.class);
                if (getTangoAttribute().getAttributeProxy().get_info().data_format == AttrDataFormat.SCALAR) {
                    ExecutionTracerService.trace(this, "read write part of Attribute " + getAttributeName()
                            + ", value: " + value);
                } else if (getTangoAttribute().getAttributeProxy().get_info().data_format == AttrDataFormat.SPECTRUM) {
                    ExecutionTracerService.trace(this, "read Attribute " + getAttributeName() + " (spectrum) ");
                } else if (getTangoAttribute().getAttributeProxy().get_info().data_format == AttrDataFormat.IMAGE) {
                    ExecutionTracerService.trace(this, "read Attribute " + getAttributeName() + " (image) ");
                }

                // save data if necessary
                if (isRecordData()) {
                    DataRecorder.getInstance().saveDevice(this, getTangoAttribute().getDeviceName());
                }

            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            } catch (final PasserelleException e) {
                ExceptionUtil.throwProcessingException(e.getMessage(), getAttributeName(), e);
            }

            // send attribute to output
            // sendOutputMsg(output, PasserelleUtil.createContentMessage(this,
            // value));
            response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, value));

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
