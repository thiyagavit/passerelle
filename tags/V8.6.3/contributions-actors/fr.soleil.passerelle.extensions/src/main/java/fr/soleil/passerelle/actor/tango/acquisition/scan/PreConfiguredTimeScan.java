package fr.soleil.passerelle.actor.tango.acquisition.scan;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.TransformerV5;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.scan.SimpleScan;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class PreConfiguredTimeScan extends TransformerV5 {

    private static final String SCAN_CONFIG = "Scan Config";

    public Port xIntegrationTimePort;
    /**
     * The name of the Salsa config V3. must be the entire path to the config
     * exemple : root/config1 or root/folder1/config2
     */
    @ParameterName(name = SCAN_CONFIG)
    public Parameter scanConfigParam;
    private String confName = "root/";

    public StringParameter simpleScanNameParam;
    public final static String DEFAULT_SIMPLE_SCAN_NAME = "tango/CA/SimpleScan.1";
    private String simpleScanName = DEFAULT_SIMPLE_SCAN_NAME;

    private SimpleScan simpleScanObj = null;
    private final static Logger logger = LoggerFactory.getLogger(PreConfiguredTimeScan.class);

    public PreConfiguredTimeScan(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        scanConfigParam = new StringParameter(this, SCAN_CONFIG);
        scanConfigParam.setExpression(confName);

        simpleScanNameParam = new StringParameter(this, "SimpleScan Device Name");
        simpleScanNameParam.setExpression(simpleScanName);
        registerExpertParameter(simpleScanNameParam);

        input.setName("NbSteps");
        input.setExpectedMessageContentType(Double.class);

        xIntegrationTimePort = PortFactory.getInstance().createInputPort(this, "IntegrationTime", Double.class);
        output.setName("TriggerOut");

        final URL url = this.getClass().getResource("/fr/soleil/salsa/salsa.png");

        _attachText("_iconDescription", "<svg>\n" + " <image x=\"0\" y=\"0\" width =\"75\" height=\"51\" xlink:href=\""
                + url + "\"/>\n" + "</svg>\n");

    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == scanConfigParam) {
            confName = PasserelleUtil.getParameterValue(scanConfigParam);
        } else if (arg0 == simpleScanNameParam) {
            simpleScanName = PasserelleUtil.getParameterValue(simpleScanNameParam);
        } else {
            super.attributeChanged(arg0);
        }
    }

    /**
     * Initialize actor
     */
    @Override
    public void validateInitialization() throws ValidationException {
        if (!isMockMode()) {
            final BasicDirector dir = (BasicDirector) getDirector();
            try {
                simpleScanObj = new SimpleScan(simpleScanName, confName);
            } catch (DevFailed e) {
                String errorMessage = "Error: " + confName + " cannot be loadded on SimpleScan " + simpleScanName;
                ExecutionTracerService.trace(this, errorMessage);
                throw new ValidationException(errorMessage, this, null);
            }

            try {
                configureRecordingSession();
            } catch (DevFailed e) {
                ExecutionTracerService.trace(this, "Error: Recording session configuration error");
                throw new ValidationException("Error: Recording session configuration error", confName, e);
            }
        }
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        final ManagedMessage nbStepsmessage = request.getMessage(input);
        final int nbStepsX = (int) ((Double) PasserelleUtil.getInputValue(nbStepsmessage)).doubleValue();
        logger.debug("nbStepsX:" + nbStepsX);

        final ManagedMessage intTimemessage = request.getMessage(xIntegrationTimePort);
        final double intTime = (Double) PasserelleUtil.getInputValue(intTimemessage);
        logger.debug("intTime:" + intTime);

        String logMessage = "Time scan in " + nbStepsX + " steps with an integration time equals to " + intTime + " s";
        if (!isMockMode()) {
            try {
                ExecutionTracerService.trace(this, logMessage);
                simpleScanObj.startTimeScan(nbStepsX, intTime);

                response.addOutputMessage(output, PasserelleUtil.createTriggerMessage());
                ExecutionTracerService.trace(this, "Scan is finished");

            } catch (DevFailed e) {
                throw new DevFailedProcessingException(e, this);
            }

        } else {
            ExecutionTracerService.trace(this, "MOCK - " + logMessage);
        }
    }

    private void configureRecordingSession() throws DevFailed {
        if (DataRecorder.getInstance().isSaveActive(this)) {
            simpleScanObj.setDataRecorderPartialMode(true);
            // System.out.println("setDataRecorderPartialMode(true)");
        } else {

            simpleScanObj.setDataRecorderPartialMode(false);
            // System.out.println("setDataRecorderPartialMode(false)");
        }
    }

    @Override
    protected void doStop() {
        super.doStop();
        if (!isMockMode()) {
            try {
                simpleScanObj.stopScan();
                ExecutionTracerService.trace(this, "Scan aborted");
            } catch (DevFailed e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public void doFinalAction() {
//        // System.out.println("SCAN : dofinal action is requiered");
//        // stopScan();
//    }

    @Override
    protected void doPauseFire() {
        super.doPauseFire();
        if (!isMockMode()) {
            try {
                simpleScanObj.pauseScan();
                ExecutionTracerService.trace(this, "Scan paused");
            } catch (DevFailed e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doResumeFire() {
        super.doResumeFire();
        if (!isMockMode()) {
            try {
                simpleScanObj.resumeScan();
                ExecutionTracerService.trace(this, "Scan resumed");
            } catch (DevFailed e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
