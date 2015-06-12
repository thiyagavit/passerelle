/*
 * Synchrotron Soleil
 * 
 * File : SimpleScalarViewer.java
 * 
 * Project : passerelle-soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 29 juin 2005
 * 
 * Revision: Author:
 * Date: State:
 * 
 * Log: SimpleScalarViewer.java,v
 */
/*
 * Created on 29 juin 2005
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.tango.debug;

import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.soleil.passerelle.util.AttrScalarPanel;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * @author ABEILLE
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@SuppressWarnings("serial")
public class ScalarViewer extends Sink {

    private JFrame frame;
    private TangoAttribute ap = null;
    private AttrScalarPanel asp;

    public Parameter titleParam;
    private String title = "";

    private static final String DIM_WIDTH = "Window size-Width";
    private static final String DIM_HEIGHT = "Window size-Height";

    @ParameterName(name = DIM_WIDTH)
    public Parameter dimWidthParam;
    private int dimWidthValue;

    @ParameterName(name = DIM_HEIGHT)
    public Parameter dimHeightParam;
    private int dimHeightValue;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public ScalarViewer(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        input.setName("Scalar value");

        titleParam = new StringParameter(this, "title");
        titleParam.setExpression(title);

        dimWidthParam = new StringParameter(this, DIM_WIDTH);
        dimWidthParam.setExpression(String.valueOf(AttrScalarPanel.DEFAULT_DIM_WIDTH));
        registerExpertParameter(dimWidthParam);

        dimHeightParam = new StringParameter(this, DIM_HEIGHT);
        dimHeightParam.setExpression(String.valueOf(AttrScalarPanel.DEFAULT_DIM_HEIGHT));
        registerExpertParameter(dimHeightParam);
    }

    @Override
    protected void doInitialize() throws InitializationException {
        frame = new JFrame();
        frame.setLocation(new Point(250, 50));
        asp = new AttrScalarPanel(dimWidthValue, dimHeightValue);
        super.doInitialize();
    }

    @Override
    protected void sendMessage(final ManagedMessage outgoingMessage) throws ProcessingException {
        if (isMockMode()) {
            String value = null;
            try {
                value = outgoingMessage.getBodyContentAsString();
            } catch (final MessageException e) {
                ExceptionUtil.throwProcessingException("Cannot get input message", this.getName(), e);
            }
            asp.setIsAttribute(false);
            asp.setValue(value);

            frame.setTitle("MOCK -" + title);
            try {
                asp.postInitGUI();
            } catch (final ConnectionException e1) {
                ExceptionUtil.throwProcessingException("Cannot start panel", this.getName(), e1);
            }
        } else {
            try {
                if (!(outgoingMessage.getBodyContent() instanceof TangoAttribute)) {
                    /*
                     * className =
                     * outgoingMessage.getBodyContent().getClass().getName();
                     * throw new
                     * ProcessingException(PasserelleException.Severity.FATAL,
                     * "Input message must of type AttributeBasicHelper (not "
                     * +className+")", this.getName(),null);
                     */
                    final String value = outgoingMessage.getBodyContentAsString();

                    asp.setValue(value);
                    asp.setIsAttribute(false);

                    frame.setTitle(title);
                    try {
                        asp.postInitGUI();
                    } catch (final ConnectionException e1) {
                        ExceptionUtil.throwProcessingException("Cannot start panel", this.getName(), e1);
                    }

                } else {
                    try {
                        ap = (TangoAttribute) outgoingMessage.getBodyContent();
                        AttrDataFormat data_format;

                        data_format = ap.getAttributeProxy().get_info().data_format;

                        if (!data_format.equals(AttrDataFormat.SCALAR)) {
                            asp.setIsAttribute(false);
                            asp.setValue(ap.extractToString(",", ""));

                        } else {
                            asp.setIsAttribute(true);
                            asp.setAttributeName(ap.getAttributeProxy().fullName());
                        }
                    } catch (final DevFailed e) {
                        ExceptionUtil.throwProcessingException(this, e);
                    }
                    frame.setTitle(title + " - " + ap.getAttributeProxy().fullName());

                }

                try {
                    asp.postInitGUI();
                } catch (final ConnectionException e1) {
                    ExceptionUtil.throwProcessingException("Cannot start panel for "
                            + ap.getAttributeProxy().fullName(), this.getName(), e1);
                }
            } catch (final MessageException e) {
                ExceptionUtil.throwProcessingException("Cannot get input message", this.getName(), e);
            }
        }
        frame.getContentPane().add(asp);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
     */
    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == titleParam) {
            title = ((StringToken) titleParam.getToken()).stringValue();
        } else if (attribute == dimWidthParam) {
            dimWidthValue = PasserelleUtil.getParameterIntValue(dimWidthParam);
        } else if (attribute == dimHeightParam) {
            dimHeightValue = PasserelleUtil.getParameterIntValue(dimHeightParam);
        } else {
            super.attributeChanged(attribute);
        }
    }

    public static void main(final String[] args) {
    }

}
