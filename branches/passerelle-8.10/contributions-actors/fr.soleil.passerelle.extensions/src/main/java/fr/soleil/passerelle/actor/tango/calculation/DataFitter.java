/*
 * Synchrotron Soleil
 * 
 * File : DataFitter.java
 * 
 * Project : passerelle-soleil-2.3.0
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 15 f�vr. 2006
 * 
 * Revision: Author:
 * Date: State:
 * 
 * Log: DataFitter.java,v
 */
/*
 * Created on 15 f�vr. 2006
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.tango.calculation;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.MessageOutputContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.ActorV3;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

@SuppressWarnings("serial")
public class DataFitter extends ActorV3 {

    public Parameter dataFitterNameParam;
    private String dataFitterName = "test/1/toto/datafitter";

    public Parameter functionParam;
    private String function = "gaussian";

    public Parameter epsilonParam;
    private Double epsilon = new Double(1);

    public Port xDataPort;
    public Port yDataPort;
    public Port positionPort;
    public Port heigthPort;
    public Port widthPort;
    public Port backgroundPort;

    public DataFitter(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);

        dataFitterNameParam = new StringParameter(this, "DataFitter device name");
        dataFitterNameParam.setExpression(dataFitterName);
        registerConfigurableParameter(dataFitterNameParam);

        functionParam = new StringParameter(this, "Fitting function");
        functionParam.setExpression(function);
        registerConfigurableParameter(functionParam);

        epsilonParam = new StringParameter(this, "epsilon");
        epsilonParam.setExpression(epsilon.toString());
        registerConfigurableParameter(epsilonParam);

        xDataPort = PortFactory.getInstance().createInputPort(this, "X Data", null);
        xDataPort.setMultiport(false);
        yDataPort = PortFactory.getInstance().createInputPort(this, "Y Data", null);
        yDataPort.setMultiport(false);
        positionPort = PortFactory.getInstance().createOutputPort(this, "position");
        heigthPort = PortFactory.getInstance().createOutputPort(this, "height");
        widthPort = PortFactory.getInstance().createOutputPort(this, "width");
        backgroundPort = PortFactory.getInstance().createOutputPort(this, "background");

    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {
        if (isMockMode()) {
            if (function.endsWith("b")) {// using fit with background
                ExecutionTracerService.trace(this, "MOCK - position:" + 1 + ", width:" + 2 + ", height: " + 3
                        + ", background: " + 4);
            } else {
                ExecutionTracerService.trace(this, "MOCK - position:" + 1 + ", width:" + 2 + ", height: " + 3);
            }
            response.addOutputContextInSequence(new MessageOutputContext(0, positionPort, PasserelleUtil
                    .createContentMessage(this, 1.0)));
            response.addOutputContextInSequence(new MessageOutputContext(0, widthPort, PasserelleUtil
                    .createContentMessage(this, 2.0)));
            response.addOutputContextInSequence(new MessageOutputContext(0, heigthPort, PasserelleUtil
                    .createContentMessage(this, 3.0)));
            if (function.endsWith("b")) {
                response.addOutputContextInSequence(new MessageOutputContext(0, backgroundPort, PasserelleUtil
                        .createContentMessage(this, 4.0)));

            } else {
                response.addOutputContextInSequence(new MessageOutputContext(0, backgroundPort, PasserelleUtil
                        .createContentMessage(this, Double.NaN)));
            }
        } else {
            TangoAttribute xAttr = null, yAttr = null;
            final ManagedMessage msgX = request.getMessage(xDataPort);
            final ManagedMessage msgY = request.getMessage(yDataPort);
            if (PasserelleUtil.getInputValue(msgX) instanceof TangoAttribute
                    && PasserelleUtil.getInputValue(msgY) instanceof TangoAttribute) {
                xAttr = (TangoAttribute) PasserelleUtil.getInputValue(msgX);
                yAttr = (TangoAttribute) PasserelleUtil.getInputValue(msgY);
            } else {
                ExceptionUtil.throwProcessingException("Inputs must be Tango Attributes", this);
            }
            try {
                final String xDataToFitName = xAttr.getAttributeProxy().fullName();
                final String yDataToFitName = yAttr.getAttributeProxy().fullName();
                final TangoAttribute[] results = doFit(xDataToFitName, yDataToFitName, dataFitterName, function,
                        epsilon);
                final double pos = results[0].read(Double.class);
                final double width = results[1].read(Double.class);
                final double height = results[2].read(Double.class);
                double background = 0;
                if (function.endsWith("b")) {// using fit with background
                    background = results[3].read(Double.class);
                    ExecutionTracerService.trace(this, "position:" + pos + ", width:" + width + ", height: " + height
                            + ", background: " + background);
                } else {
                    ExecutionTracerService.trace(this, "position:" + pos + ", width:" + width + ", height: " + height);
                }
                // output results
                response.addOutputContextInSequence(new MessageOutputContext(0, positionPort, PasserelleUtil
                        .createContentMessage(this, results[0])));
                response.addOutputContextInSequence(new MessageOutputContext(0, widthPort, PasserelleUtil
                        .createContentMessage(this, results[1])));
                response.addOutputContextInSequence(new MessageOutputContext(0, heigthPort, PasserelleUtil
                        .createContentMessage(this, results[2])));
                if (function.endsWith("b")) {
                    response.addOutputContextInSequence(new MessageOutputContext(0, backgroundPort, PasserelleUtil
                            .createContentMessage(this, results[3])));

                } else {
                    response.addOutputContextInSequence(new MessageOutputContext(0, backgroundPort, PasserelleUtil
                            .createContentMessage(this, Double.NaN)));
                }
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
    }

    /**
     * Fit data and get results. Done in a static method to be sure that one
     * calculation at a time is done on a single instance of the device. Because
     * several fits can be ask in parallel on Passerelle model.
     * 
     * @param dataToFitName
     * @param dataFitterName
     * @param function
     * @param epsilon
     * @return
     * @throws DevFailed
     *             17 f�vr. 2006
     */
    private static synchronized TangoAttribute[] doFit(final String xdataToFitName, final String ydataToFitName,
            final String dataFitterName, final String function, final Double epsilon) throws DevFailed {
        final TangoAttribute[] attrList = new TangoAttribute[4];
        final DeviceProxy devProxy = ProxyFactory.getInstance().createDeviceProxy(dataFitterName);

        DeviceAttribute da = new DeviceAttribute("deviceAttributeNameX");
        da.insert(xdataToFitName);
        devProxy.write_attribute(da);

        da = new DeviceAttribute("deviceAttributeNameY");
        da.insert(ydataToFitName);
        devProxy.write_attribute(da);

        da = new DeviceAttribute("fittingFunctionType");
        da.insert(function);
        devProxy.write_attribute(da);

        da = new DeviceAttribute("epsilon");
        da.insert(epsilon.doubleValue());
        devProxy.write_attribute(da);

        devProxy.command_inout("StartFit");
        // TODO: gerer les erreurs (qui apparaissent dans le state et
        // status)

        attrList[0] = new TangoAttribute(dataFitterName + "/position");
        attrList[1] = new TangoAttribute(dataFitterName + "/width");
        attrList[2] = new TangoAttribute(dataFitterName + "/height");
        attrList[3] = new TangoAttribute(dataFitterName + "/background");

        return attrList;

    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == dataFitterNameParam) {
            dataFitterName = ((StringToken) dataFitterNameParam.getToken()).stringValue();
        } else if (arg0 == functionParam) {
            function = ((StringToken) functionParam.getToken()).stringValue();
        } else if (arg0 == epsilonParam) {
            epsilon = Double.valueOf(((StringToken) epsilonParam.getToken()).stringValue());
        } else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }

}
