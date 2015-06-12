package fr.soleil.passerelle.actor.tango.acquisition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class MCAMultiport extends ATangoDeviceActor {

    private final static Logger logger = LoggerFactory.getLogger(MCAMultiport.class);
    public Parameter nbChannelsParam;
    private int nbChannels = 0;
    public Parameter integrationTimeParam;
    private double integrationTime = 0;
    private TangoAttribute attOutHelper;
    private TangoCommand deviceHelper;
    public Parameter roi1Param;
    public Parameter roi2Param;
    public Parameter roi3Param;
    public Parameter roi4Param;
    public Parameter roi5Param;
    public Parameter roi6Param;
    public Parameter roi7Param;
    public Parameter roi8Param;
    public Parameter roi9Param;
    public Parameter roi10Param;
    public Parameter roi11Param;
    public Parameter roi12Param;
    public Parameter roi13Param;
    public Parameter roi14Param;
    public Parameter roi15Param;
    public Parameter roi16Param;
    public Parameter roi17Param;
    public Parameter roi18Param;
    public Parameter roi19Param;
    public Parameter roi20Param;
    private Object[] roi;
    private TangoAttribute attInChannelsHelper;
    private TangoAttribute attInTimeHelper;
    private TangoCommand commandSetRoisHelper;
    private WaitStateTask waitTask;

    // /** The output ports */
    // public Port output;

    public MCAMultiport(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        // output = PortFactory.getInstance().createOutputPort(this,"output");

        nbChannelsParam = new StringParameter(this, "Nb Channels");
        nbChannelsParam.setExpression("0");

        integrationTimeParam = new StringParameter(this, "Integration Time");
        integrationTimeParam.setExpression("0.0");

        roi = new Object[40];

        roi1Param = new StringParameter(this, "Roi1 start,end (sep by commas)");
        roi1Param.setExpression("0,0");

        roi2Param = new StringParameter(this, "Roi2 start,end (sep by commas)");
        roi2Param.setExpression("0,0");

        roi3Param = new StringParameter(this, "Roi3 start,end (sep by commas)");
        roi3Param.setExpression("0,0");

        roi4Param = new StringParameter(this, "Roi4 start,end (sep by commas)");
        roi4Param.setExpression("0,0");

        roi5Param = new StringParameter(this, "Roi5 start,end (sep by commas)");
        roi5Param.setExpression("0,0");

        roi6Param = new StringParameter(this, "Roi6 start,end (sep by commas)");
        roi6Param.setExpression("0,0");

        roi7Param = new StringParameter(this, "Roi7 start,end (sep by commas)");
        roi7Param.setExpression("0,0");

        roi8Param = new StringParameter(this, "Roi8 start,end (sep by commas)");
        roi8Param.setExpression("0,0");

        roi9Param = new StringParameter(this, "Roi9 start,end (sep by commas)");
        roi9Param.setExpression("0,0");

        roi10Param = new StringParameter(this, "Roi10 start,end (sep by commas)");
        roi10Param.setExpression("0,0");

        roi11Param = new StringParameter(this, "Roi11 start,end (sep by commas)");
        roi11Param.setExpression("0,0");

        roi12Param = new StringParameter(this, "Roi12 start,end (sep by commas)");
        roi12Param.setExpression("0,0");

        roi13Param = new StringParameter(this, "Roi13 start,end (sep by commas)");
        roi13Param.setExpression("0,0");

        roi14Param = new StringParameter(this, "Roi14 start,end (sep by commas)");
        roi14Param.setExpression("0,0");

        roi15Param = new StringParameter(this, "Roi15 start,end (sep by commas)");
        roi15Param.setExpression("0,0");

        roi16Param = new StringParameter(this, "Roi16 start,end (sep by commas)");
        roi16Param.setExpression("0,0");

        roi17Param = new StringParameter(this, "Roi17 start,end (sep by commas)");
        roi17Param.setExpression("0,0");

        roi18Param = new StringParameter(this, "Roi18 start,end (sep by commas)");
        roi18Param.setExpression("0,0");

        roi19Param = new StringParameter(this, "Roi19 start,end (sep by commas)");
        roi19Param.setExpression("0,0");

        roi20Param = new StringParameter(this, "Roi20 start,end (sep by commas)");
        roi20Param.setExpression("0,0");
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " attributeChanged() - entry");
        }

        String roiString;
        int indexComa;

        if (arg0 == nbChannelsParam) {
            nbChannels = Integer.valueOf(nbChannelsParam.getExpression().trim());
        } else if (arg0 == integrationTimeParam) {
            integrationTime = Double.valueOf(integrationTimeParam.getExpression().trim());
        } else if (arg0 == roi1Param) {
            roiString = ((StringToken) roi1Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[0] = roiString.substring(0, indexComa);
            roi[1] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi2Param) {
            roiString = ((StringToken) roi2Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[2] = roiString.substring(0, indexComa);
            roi[3] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi3Param) {
            roiString = ((StringToken) roi3Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[4] = roiString.substring(0, indexComa);
            roi[5] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi4Param) {
            roiString = ((StringToken) roi4Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[6] = roiString.substring(0, indexComa);
            roi[7] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi5Param) {
            roiString = ((StringToken) roi5Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[8] = roiString.substring(0, indexComa);
            roi[9] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi6Param) {
            roiString = ((StringToken) roi6Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[10] = roiString.substring(0, indexComa);
            roi[11] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi7Param) {
            roiString = ((StringToken) roi7Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[12] = roiString.substring(0, indexComa);
            roi[13] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi8Param) {
            roiString = ((StringToken) roi8Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[14] = roiString.substring(0, indexComa);
            roi[15] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi9Param) {
            roiString = ((StringToken) roi9Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[16] = roiString.substring(0, indexComa);
            roi[17] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi10Param) {
            roiString = ((StringToken) roi10Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[18] = roiString.substring(0, indexComa);
            roi[19] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi11Param) {
            roiString = ((StringToken) roi11Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[20] = roiString.substring(0, indexComa);
            roi[21] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi12Param) {
            roiString = ((StringToken) roi12Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[22] = roiString.substring(0, indexComa);
            roi[23] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi13Param) {
            roiString = ((StringToken) roi13Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[24] = roiString.substring(0, indexComa);
            roi[25] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi14Param) {
            roiString = ((StringToken) roi14Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[26] = roiString.substring(0, indexComa);
            roi[27] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi15Param) {
            roiString = ((StringToken) roi15Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[28] = roiString.substring(0, indexComa);
            roi[29] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi16Param) {
            roiString = ((StringToken) roi16Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[30] = roiString.substring(0, indexComa);
            roi[31] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi17Param) {
            roiString = ((StringToken) roi17Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[32] = roiString.substring(0, indexComa);
            roi[33] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi18Param) {
            roiString = ((StringToken) roi18Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[34] = roiString.substring(0, indexComa);
            roi[35] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi19Param) {
            roiString = ((StringToken) roi19Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[36] = roiString.substring(0, indexComa);
            roi[37] = roiString.substring(indexComa + 1);
        } else if (arg0 == roi20Param) {
            roiString = ((StringToken) roi20Param.getToken()).stringValue();
            indexComa = roiString.indexOf(",");
            roi[38] = roiString.substring(0, indexComa);
            roi[39] = roiString.substring(indexComa + 1);
        } else {
            super.attributeChanged(arg0);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " attributeChanged() - exit");
        }
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - entry");
        }

        if (!isMockMode()) {
            try {
                final String deviceName = getDeviceName();
                commandSetRoisHelper = new TangoCommand(deviceName, "SetROIs");
                deviceHelper = new TangoCommand(deviceName, "Start");
                attInChannelsHelper = new TangoAttribute(deviceName + "/NbChannels");
                attInTimeHelper = new TangoAttribute(deviceName + "/integrationTime");
                attOutHelper = new TangoAttribute(deviceName + "/DataSpectrum");
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(ErrorCode.FATAL,
                        TangoToPasserelleUtil.getDevFailedString(e, this), this, e);
            }
        }
        super.doInitialize();

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - exit");
        }
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {
        final String deviceName = getDeviceName();
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - Start acquisition on " + deviceName);
            ExecutionTracerService.trace(this, "MOCK - End acquisition on " + deviceName);
            // sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
            response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());
        } else {
            try {
                attInChannelsHelper.write(nbChannels);
                attInTimeHelper.write(integrationTime);

                commandSetRoisHelper.execute(roi);

                deviceHelper.execute();
                ExecutionTracerService.trace(this, "start acquisition on " + deviceName);
                waitTask = new WaitStateTask(deviceName, DevState.RUNNING, (int) (integrationTime * 100), true);
                waitTask.run();
                if (waitTask.hasFailed()) {
                    throw waitTask.getDevFailed();
                }

                // try {
                // Thread.sleep((long)(integrationTime + 100));
                // } catch(InterruptedException e) { }
                ExecutionTracerService.trace(this, "end acquisition on " + deviceName);

                // save data if necessary
                DataRecorder.getInstance().saveDevice(this, deviceName);
                // sendOutputMsg(output,
                // PasserelleUtil.createContentMessage(this,
                // attOutHelper));
                attOutHelper.update();
                response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, attOutHelper));
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
    }

    @Override
    protected void doStop() {
        if (waitTask != null) {
            waitTask.cancel();
        }
        super.doStop();
    }

    @Override
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
        final MCAMultiport copy = (MCAMultiport) super.clone(workspace);
        copy.roi = new String[40];
        return copy;
    }
}
