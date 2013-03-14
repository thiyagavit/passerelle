/*
 * Created on 27 avr. 2005
 *
 */
package fr.soleil.passerelle.actor.tango.debug;

import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.soleil.passerelle.util.AttrScalarPanel;
import fr.soleil.passerelle.util.AttrSpectrumPanel;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * @author ABEILLE
 *
 * Actor to plot a Tango spectrum Attribute (refresh only once). 
 * Inputs: 1 input, the AttributeProxy of the attribute to plot.
 * Outputs: default ones.
 */
@SuppressWarnings("serial")
public class SpectrumViewer extends Sink{
	
	private final static Logger logger = LoggerFactory.getLogger(SpectrumViewer.class);
	
    private JFrame frame;
    private TangoAttribute ap = null;
    AttrSpectrumPanel asp ;
    AttrScalarPanel mockPanel;
    /**
     * @param arg0
     * @param arg1
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public SpectrumViewer(CompositeEntity arg0, String arg1)
            throws NameDuplicationException, IllegalActionException {
        super(arg0, arg1);
        input.setName("Spectrum Attribute");
    }
    
    @Override
	protected void doInitialize() throws InitializationException {
		frame = new JFrame();
   	    frame.setLocation(new Point(250, 50));
		if(isMockMode()){
			mockPanel = new AttrScalarPanel();
		}else{
			asp = new AttrSpectrumPanel();
		}
		super.doInitialize();
	}
    
    /* (non-Javadoc)
     * @see com.isencia.passerelle.actor.Sink#sendMessage(com.isencia.passerelle.message.ManagedMessage)
     */
    protected void sendMessage(ManagedMessage arg0) throws ProcessingException {
        
    	if (logger.isTraceEnabled())
			logger.trace(getInfo()+" sendMessage() - entry");
        
        String className = "";
        if(isMockMode()){
        	 frame.setTitle("MOCK - Spectrum Viewer");
        	 mockPanel.setValue("MOCK");
        	 mockPanel.setIsAttribute(false);
             frame.getContentPane().add(mockPanel);
             try {
            	 mockPanel.postInitGUI();
 	        } catch (ConnectionException e1) {
 	            e1.printStackTrace();
 	            throw new ProcessingException(
 	                    "Cannot start panel for "+  ap.getAttributeProxy().fullName(), this.getName(), e1);
 	        }
        }else{
	        try {
	            if(!(arg0.getBodyContent() instanceof TangoAttribute)){
	                className = arg0.getBodyContent().getClass().getName();
	                throw new ProcessingException(PasserelleException.Severity.FATAL,
	                        "Input message must of type AttributeProxy (not "+className+")", 
							this.getName(),null);}
	            ap = (TangoAttribute)arg0.getBodyContent();
	        } catch (MessageException e) {
	            throw new ProcessingException(
	                    "Cannot get input message" , this.getName(), e);
	        }
	       
	        AttrDataFormat data_format;
	        try {
	            data_format = ap.getAttributeProxy().get_info().data_format;
	        } catch (DevFailed e) {
	        	throw new DevFailedProcessingException(e, this);
	        }
	        if(!data_format.equals(AttrDataFormat.SPECTRUM))
	        {
	            Exception e = new Exception();
	            throw new ProcessingException("Attribute is not a spectrum" ,  ap.getAttributeProxy().fullName(), e);
	        }   
	        asp.setAttributeName( ap.getAttributeProxy().fullName());    
	        
	        try {
	            asp.postInitGUI();
	        } catch (ConnectionException e1) {
	            throw new ProcessingException(
	                    "Cannot start panel for "+  ap.getAttributeProxy().fullName(), this.getName(), e1);
	        }
	        frame.setTitle("Spectrum Viewer of " + ap.getAttributeProxy().fullName());
	        frame.getContentPane().add(asp);
        }
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        
        if (logger.isTraceEnabled())
			logger.trace(getInfo()+" sendMessage() - exit");
    }
    /* (non-Javadoc)
     * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
     */
    protected String getExtendedInfo() {
        return " Plot a spectrum Tango attribute";
    }
}
