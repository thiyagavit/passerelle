package fr.soleil.passerelle.actor.tango.acquisition.scan;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.actor.tango.acquisition.Scan;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.salsa.entity.IDevice;
import fr.soleil.salsa.entity.impl.scan1d.Range1DImpl;
import fr.soleil.salsa.entity.scan1d.IConfig1D;
import fr.soleil.salsa.entity.scan1d.IRange1D;
import fr.soleil.salsa.entity.scanhcs.IConfigHCS;

@SuppressWarnings("serial")
public class PreConfiguredTimeScan extends Scan {

    public Port xIntegrationTimePort;

    private final static Logger logger = LoggerFactory.getLogger(PreConfiguredTimeScan.class);

    public PreConfiguredTimeScan(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        input.setName("NbSteps");
        input.setExpectedMessageContentType(Double.class);

        xIntegrationTimePort = PortFactory.getInstance().createInputPort(this, "IntegrationTime", Double.class);
    }

    /**
     * Initialize actor
     */
    @Override
    public void validateInitialization() throws ValidationException {
        super.validateInitialization();
        // This actor is not valid for a K configuration because of the non linear trajectory
        // All the other kind of configuration have an X linear dimension
        if (conf == null || (!(conf instanceof IConfig1D) && !(conf instanceof IConfigHCS))) {
            String errorMessage = "Error: " + conf.getFullPath() + " is not a time scan";
            ExecutionTracerService.trace(this, errorMessage);
            throw new ValidationException(ErrorCode.ERROR, errorMessage, this, null);
        }
        List<IDevice> activatedActuatorsList = conf.getActivatedActuatorsList();
        if(activatedActuatorsList != null && !activatedActuatorsList.isEmpty()){
            String errorMessage = "Error: " + conf.getFullPath() + " have some active actuator";
            ExecutionTracerService.trace(this, errorMessage);
            throw new ValidationException(ErrorCode.ERROR, errorMessage, this, null);
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
            final IRange1D range = new Range1DImpl();
            range.setStepsNumber(nbStepsX);
            range.setIntegrationTime(intTime);
            
            try {
                ScanUtil.setTimeScanTrajectory(conf, range);
            } catch (PasserelleException e) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, ErrorCode.FATAL, e.getMessage(), this, e);
            }
            ExecutionTracerService.trace(this, logMessage.toString());
        } else {
            ExecutionTracerService.trace(this, "MOCK - " + logMessage);
        }
        super.process(ctxt, request, response);
    }

}
