package fr.soleil.passerelle.actor.tango.acquisition.ccd;

import java.net.URL;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class CCDAcquisitionPerformer extends ATangoDeviceActorV5 implements IActorFinalizer {

    private final static Logger LOGGER = LoggerFactory.getLogger(CCDAcquisitionPerformer.class);
    public Parameter acquisitionModeParam;
    public Parameter recordAllSequenceParam;
    protected CCDManager ccd;
    protected String acqMode = "OneShot";

    protected boolean recordAllSequence;
    HashMap<String, Integer> acqModeMap;

    // /** The output ports */
    public Port outputAcqStarted = null;

    public CCDAcquisitionPerformer(final CompositeEntity arg0, final String arg1,
            final HashMap<String, Integer> acqModeMap) throws NameDuplicationException, IllegalActionException {
        super(arg0, arg1);
        initActor(acqModeMap, false);
    }

    public CCDAcquisitionPerformer(final CompositeEntity arg0, final String arg1,
            final HashMap<String, Integer> acqModeMap, final boolean withAcqStartedInfo)
            throws NameDuplicationException, IllegalActionException {
        super(arg0, arg1);
        initActor(acqModeMap, withAcqStartedInfo);

    }

    private void initActor(final HashMap<String, Integer> acqModeMap, final boolean withAcqStartedInfo)
            throws NameDuplicationException, IllegalActionException {
        input.setMode(PortMode.PUSH);

        if (withAcqStartedInfo) {
            outputAcqStarted = PortFactory.getInstance().createOutputPort(this, "AcqStarted");
        }

        this.acqModeMap = acqModeMap;
        // ccd = new CCDManager(this);
        // ccd.setAcqModeMap(acqModeMap);

        acquisitionModeParam = new StringParameter(this, "Acquisition Type");
        boolean firstTime = true;
        for (final String string : acqModeMap.keySet()) {
            final String mode = string;
            if (firstTime) {
                acquisitionModeParam.setExpression(mode);
                firstTime = false;
            }
            acquisitionModeParam.addChoice(mode);
        }

        recordAllSequenceParam = new Parameter(this, "Record all sequence", new BooleanToken(false));
        recordAllSequenceParam.setTypeEquals(BaseType.BOOLEAN);

        final URL url = this.getClass().getResource("/image/camera.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:blue;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-11\" y=\"-11\" width =\"32\" height=\"32\" xlink:href=\"" + url + "\"/>\n" + "</svg>\n");
    }

    @Override
    protected void doInitialize() throws InitializationException {
        final Director dir = getDirector();
        if (dir instanceof BasicDirector) {
            ((BasicDirector) dir).registerFinalizer(this);
        }
        super.doInitialize();
        ccd = CCDManagerFactory.getInstance().createCCDManager(this, getDeviceName());

    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        getLogger().trace("process - entry");
        final CCDConfiguration config = ccd.getConfig();
        config.setAcqModeMap(acqModeMap);
        config.setAcqMode(acqMode);
        config.setRecordAllSequence(recordAllSequence);
        ccd.setActor(this);
        try {
            if (isMockMode()) {
                ExecutionTracerService.trace(this,
                        "MOCK - starting CCD acquisition with parameters: \n" + ccd.getConfig());
                ExecutionTracerService.trace(this, "MOCK - CCD acquisition finished");
            } else {
                ccd.startStandardAcquisition();
                if (outputAcqStarted != null) {
                    getLogger().trace("process - Acquisition is started");
                    sendOutputMsg(outputAcqStarted, PasserelleUtil.createTriggerMessage());
                }
                ccd.updateConfigFromDevice();
                ExecutionTracerService.trace(this, "starting CCD acquisition with parameters: \n" + ccd.getConfig());
                if (isRecordData()) {
                    ccd.waitEndAcquisitionAndStore();
                } else {
                    ccd.waitEndAcquisition();
                }
                ExecutionTracerService.trace(this, "CCD acquisition finished");
            }
            response.addOutputMessage(output, PasserelleUtil.createTriggerMessage());

            getLogger().trace("process - exit");
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        } catch (final IllegalActionException e) {
            ExceptionUtil.throwProcessingExceptionWithLog(this, "Cannot record data", this, e);
        }
    }

    @Override
    protected void doStop() {
        getLogger().debug(" doStop - cancelWaitEndAcquisition");
        ccd.cancelWaitEndAcquisition();
        super.doStop();
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == acquisitionModeParam) {
            acqMode = ((StringToken) acquisitionModeParam.getToken()).stringValue();
        } else if (arg0 == recordAllSequenceParam) {
            recordAllSequence = Boolean.valueOf(recordAllSequenceParam.getExpression());
        } else {
            super.attributeChanged(arg0);
        }
    }

    public void doFinalAction() {
        try {
            if (!isMockMode()) {
                // bug 22954
                if (TangoAccess.isCurrentStateEqualStateRequired(getDeviceName(), DevState.RUNNING)) {
                    ccd.stopAcquisition();
                    ExecutionTracerService.trace(this, "CCD acquisition stopped");
                }
                ccd.cancelWaitEndAcquisition();
            }
        } catch (final DevFailed e) {
            TangoToPasserelleUtil.getDevFailedString(e, this);
        } catch (final Exception e) {
            e.printStackTrace();
            // ignore error since it is impossible to throw it
        }
    }

}
