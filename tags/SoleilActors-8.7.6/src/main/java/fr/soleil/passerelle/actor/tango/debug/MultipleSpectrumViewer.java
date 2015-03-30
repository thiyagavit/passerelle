/*
 * Synchrotron Soleil
 * 
 * File : MultipleSpectrumViewer.java
 * 
 * Project : passerelle-soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 20 mai 2005
 * 
 * Revision: Author:
 * Date: State:
 * 
 * Log: MultipleSpectrumViewer.java,v
 */
/*
 * Created on 20 mai 2005
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.tango.debug;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageHelper;

import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.soleil.passerelle.actor.PortUtilities;
import fr.soleil.passerelle.util.AttrScalarPanel;
import fr.soleil.passerelle.util.AttributeMultipleSpectrumPanel;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * @author ABEILLE
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@SuppressWarnings("serial")
public class MultipleSpectrumViewer extends Sink {

    private static final String Y = "Y";
    private final static Logger logger = LoggerFactory.getLogger(MultipleSpectrumViewer.class);
    public Parameter yNumberParam;
    private Integer yNumber = new Integer(3);
    private final List<Port> inputsPorts;
    // private List inputsHandlers;
    private JFrame frame;
    private AttributeMultipleSpectrumPanel mainPanel;
    AttrScalarPanel mockPanel;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public MultipleSpectrumViewer(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        input.setName("X");
        // input.setExpectedMessageContentType(AttributeBasicHelper.class);
        yNumberParam = new StringParameter(this, "number of Y plots");
        yNumberParam.setExpression(yNumber.toString());
        registerConfigurableParameter(yNumberParam);

        inputsPorts = new ArrayList<Port>(10);
        // inputsHandlers = new ArrayList(10);
    }

    @Override
    protected void doInitialize() throws InitializationException {
        frame = new JFrame();
        frame.setLocation(new Point(250, 50));
        if (isMockMode()) {
            mockPanel = new AttrScalarPanel();
        } else {
            mainPanel = new AttributeMultipleSpectrumPanel();
        }
        super.doInitialize();
    }

    private double[] getDataFromPort(final ManagedMessage message) throws ProcessingException {
        double[] xValues = null;
        try {
            final Object mydata = message.getBodyContent();

            if (!(mydata instanceof TangoAttribute)) {
                // Can be a String - Constant
                try {
                    String[] input = ((String) mydata).split(",");
                    xValues = new double[input.length];
                    for (int i = 0; i < xValues.length; i++) {
                        xValues[i] = Double.valueOf(input[i]);
                    }
                } catch (final ClassCastException e) {
                    if (!mydata.getClass().isArray()) {
                        ExceptionUtil.throwProcessingExceptionWithLog(this, "Input message has not the good type (not "
                                + mydata.getClass().getName() + ")", this.getName());
                    }
                    // Can be an array of Double - GetScanData
                    // try/except si ce n'est pas du double
                    xValues = Arrays.copyOf((double[]) mydata, ((double[]) mydata).length);
                }
            } else {
                TangoAttribute attribute = (TangoAttribute) mydata;
                if (logger.isTraceEnabled()) {
                    logger.trace(getName() + "" + attribute.getAttributeProxy().fullName());
                }
                xValues = (double[]) attribute.extractArray(double.class);
            }
        } catch (final MessageException e) {
            ExceptionUtil.throwProcessingExceptionWithLog(this, "Cannot get input message", this.getName(), e);

        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        } catch (final Exception e) {
            ExceptionUtil.throwProcessingExceptionWithLog(this, "Cannot get input message", this.getName(), e);
        }

        return xValues;
    }

    @Override
    protected void sendMessage(final ManagedMessage outgoingMessage) throws ProcessingException {

        // TODO: checker que tous les attributs ont le m^ type
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " sendMessage() - entry");
        }

        if (isMockMode()) {
            frame.setTitle("MOCK - Multiple Spectrum Viewer");
            mockPanel.setValue("MOCK");
            mockPanel.setIsAttribute(false);
            frame.getContentPane().add(mockPanel);
            try {
                mockPanel.postInitGUI();
            } catch (final ConnectionException e1) {
                // e1.printStackTrace();
                ExceptionUtil.throwProcessingExceptionWithLog(this, "Cannot start panel", this.getName(), e1);
            }
        } else {
            try {
                // get X input
                double[] xValues = getDataFromPort(outgoingMessage);

                TreeMap<String, Object> chartData = new TreeMap<String, Object>();
                final List<Port> portList = PortUtilities.getOrderedInputPorts(this, Y, 1);
                for (int i = 0; i < portList.size(); i++) {
                    final Port inputPort = portList.get(i);
                    final ManagedMessage message = MessageHelper.getMessage(inputPort);
                    double[] yValues = getDataFromPort(message);

                    double[][] data = new double[2][];
                    data[0] = Arrays.copyOf(xValues, xValues.length);
                    data[1] = Arrays.copyOf(yValues, yValues.length);
                    String trendName;
                    if (message.getBodyContent() instanceof TangoAttribute) {
                        trendName = ((TangoAttribute) message.getBodyContent()).getAttributeProxy().name();
                    } else {
                        trendName = "Trend " + (i + 1);
                    }
                    chartData.put(trendName, data);
                }

                mainPanel.addChartData(chartData);
            } catch (final MessageException e) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, "Cannot get input message", this.getName(), e);
            } catch (final PasserelleException e) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, "Cannot get input message", this.getName(), e);
            }
            frame.setTitle("Multiple Spectrum Viewer");
            frame.getContentPane().add(mainPanel);
        }

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " sendMessage() - exit");
        }
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == yNumberParam) {
            // System.out.println("yNumberParam in");
            final int nrPorts = inputsPorts.size();
            // System.out.println("nrPorts:" + nrPorts);
            yNumber = Integer.valueOf(yNumberParam.getExpression().trim());
            final int newPortCount = yNumber.intValue();
            // System.out.println("nrPorts:" + newPortCount);
            try {
                // remove no more needed ports
                if (newPortCount < nrPorts) {
                    for (int i = nrPorts - 1; i >= newPortCount; i--) {
                        try {
                            /*
                             * System.out.println("remove:" + ((Port)
                             * inputsPorts.get(i)).getName());
                             */
                            inputsPorts.get(i).setContainer(null);
                            inputsPorts.remove(i);
                        } catch (final NameDuplicationException e) {
                            throw new IllegalActionException(this, e, "Error for index " + i);
                        }
                    }

                }// add missing ports
                else if (newPortCount > nrPorts) {
                    for (int i = nrPorts; i < newPortCount; i++) {
                        try {
                            final String intputPortName = Y + (i + 1);
                            // System.out.println("add:" + intputPortName);
                            Port extraInputPort = (Port) getPort(intputPortName);
                            if (extraInputPort == null) {
                                extraInputPort = PortFactory.getInstance().createInputPort(this, intputPortName, null);
                            }
                            // extraInputPort.setTypeEquals(BaseType.OBJECT);
                            inputsPorts.add(extraInputPort);
                        } catch (final NameDuplicationException e) {
                            throw new IllegalActionException(this, e, "Error for index " + i);
                        }
                    }
                }
            } catch (final IllegalActionException e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }
}
