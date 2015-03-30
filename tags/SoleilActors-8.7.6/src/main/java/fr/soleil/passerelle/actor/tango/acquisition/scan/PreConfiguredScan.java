package fr.soleil.passerelle.actor.tango.acquisition.scan;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
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
import fr.soleil.salsa.entity.ITrajectory;
import fr.soleil.salsa.entity.impl.scan1d.Range1DImpl;
import fr.soleil.salsa.entity.impl.scan1d.Trajectory1DImpl;
import fr.soleil.salsa.entity.scan1d.IRange1D;
import fr.soleil.salsa.entity.scan2D.IConfig2D;
import fr.soleil.salsa.entity.scank.IConfigK;

/**
 * Do scans using Salsa config, but the From/To NbSteps and InteegrationTime can
 * be configured
 * 
 * @author GRAMER
 */
@SuppressWarnings("serial")
public class PreConfiguredScan extends Scan {

    public Port stepsPort;
    public Port integrationPort;
    private static final String FROM = "from";
    private static final String TO = "to";
    private static final String STEP = "NbSteps";
    private static final String INTEGRATION = "IntegrationTime";

    private final static Logger logger = LoggerFactory.getLogger(PreConfiguredScan.class);

    public Parameter xRelativeParam;
    protected boolean xRelative = false;

    public Parameter nbActuatorParam;
    private int nbActuator = 1;

    public PreConfiguredScan(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        // Parameter X relative sent in all trajectories
        xRelativeParam = new Parameter(this, "X Relative", new BooleanToken(xRelative));
        xRelativeParam.setTypeEquals(BaseType.BOOLEAN);

        // Parameter number of actuators by default 1
        nbActuatorParam = new Parameter(this, "Nb actuators", new IntToken(1));
        nbActuatorParam.setTypeEquals(BaseType.INT);
  
        // First From input port always existing
        input.setName(FROM);
        input.setExpectedMessageContentType(Double.class);

        // First To input port always existing
        PortFactory.getInstance().createInputPort(this, TO, Double.class);

        // Number of step input port always existing
        stepsPort = PortFactory.getInstance().createInputPort(this, STEP, Double.class);

        // integration time input port always existing
        integrationPort = PortFactory.getInstance().createInputPort(this, INTEGRATION, Double.class);

    }

    /**
     * Initialize actor
     */
    @Override
    public void validateInitialization() throws ValidationException {
        super.validateInitialization();
        // This actor is not valid for a K configuration because of the non linear trajectory
        if (conf == null || conf instanceof IConfigK) {
            String errorMessage = "Error: " + conf.getFullPath() + " is not a linear trajectory";
            ExecutionTracerService.trace(this, errorMessage);
            throw new ValidationException(ErrorCode.ERROR, errorMessage, this, null);
        }
        //This actor is only used for 1D configuration 
        if(conf instanceof IConfig2D){
            String errorMessage = "Error: " + conf.getFullPath() + " is a 2D configuration";
            ExecutionTracerService.trace(this, errorMessage);
            throw new ValidationException(ErrorCode.ERROR, errorMessage, this, null);
        }
        
        //Test the validity of the configuration with all the parameter
        String errorMessage = ScanUtil.isValidConfiguration(conf, 0,nbActuator, false);
        if(errorMessage != null){
            throw new ValidationException(ErrorCode.ERROR, errorMessage, this, null);
        }
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        StringBuilder logMessage = new StringBuilder();
        String log = xRelative ? "Relative scan " : "Absolute scan ";
        logger.debug(log);
        logMessage.append("\n" + log);

        final ManagedMessage nbStepsmessage = request.getMessage(stepsPort);
        final int nbSteps = (int) ((Double) PasserelleUtil.getInputValue(nbStepsmessage)).doubleValue();
        log = STEP + "=" + nbSteps + " ";
        logger.debug(log);
        logMessage.append(log);

        final ManagedMessage intTimemessage = request.getMessage(integrationPort);
        final double intTime = (Double) PasserelleUtil.getInputValue(intTimemessage);
        log = INTEGRATION + "=" + intTime + " s ";
        logger.debug(log);
        logMessage.append("\n" + log);

        final IRange1D range = new Range1DImpl();
        range.setStepsNumber(nbSteps);
        range.setIntegrationTime(intTime);

        List<ITrajectory> trajectoryList = new ArrayList<ITrajectory>();
        ITrajectory trajectory = null;

        double fromValue = Double.NaN;
        double toValue = Double.NaN;

        String fromPortName = null;
        String toPortName = null;

        for (int actuatorIndex = 0; actuatorIndex < nbActuator; actuatorIndex++) {
            trajectory = new Trajectory1DImpl();
            trajectory.setIRange(range);
            trajectoryList.add(trajectory);

            fromPortName = FROM;
            if (actuatorIndex > 0) {
                fromPortName = fromPortName + actuatorIndex;
            }

            fromValue = getValueFromPort(fromPortName, request, logMessage);
            if (!Double.isNaN(fromValue)) {
                trajectory.setBeginPosition(fromValue);
            }

            toPortName = TO;
            if (actuatorIndex > 0) {
                toPortName = toPortName + actuatorIndex;
            }

            toValue = getValueFromPort(toPortName, request, logMessage);
            if (!Double.isNaN(toValue)) {
                trajectory.setEndPosition(toValue);
            }
        }

        range.setTrajectoriesList(trajectoryList);

        if (!isMockMode()) {
            try {
                ScanUtil.setTrajectory1D(conf, range, xRelative);
            } catch (PasserelleException e) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, ErrorCode.FATAL, e.getMessage(), this, e);
            }
            ExecutionTracerService.trace(this, logMessage.toString());
        } else {
            ExecutionTracerService.trace(this, "MOCK - " + logMessage);
        }
        super.process(ctxt, request, response);
    }

    private double getValueFromPort(String portName, final ProcessRequest request, final StringBuilder logMessage)
            throws ProcessingException {
        double value = Double.NaN;
        if (portName != null) {
            Object portObject = getPort(portName);
            if (portObject instanceof Port) {
                Port port = (Port) portObject;
                ManagedMessage message = request.getMessage(port);
                value = (Double) PasserelleUtil.getInputValue(message);
                String log = portName + "=" + value + " ";
                logger.trace(log);
                logMessage.append("\n" + log);
            }
        }
        return value;
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == xRelativeParam) {
            xRelative = PasserelleUtil.getParameterBooleanValue(xRelativeParam);
        } else if (arg0 == nbActuatorParam) {
            try {
                // Read the nb actuator parameter
                IntToken token = (IntToken) nbActuatorParam.getToken();
                int newNbActuator = token.intValue();

                // The minimum value must be one
                if (newNbActuator < 1) {
                    nbActuator = newNbActuator;
                    throw new IllegalActionException("nb actuators must be > 0");
                }

                // If the value change
                if (newNbActuator != nbActuator) {
                    // System.out.println("newNbActuator=" + newNbActuator);
                    nbActuator = newNbActuator;
                    // Clear dynamic port
                    clearDynamicPort();

                    // Create dynamic port over index 0
                    if (nbActuator > 1) {
                        try {
                            for (int i = 1; i < nbActuator; i++) {
                                PortFactory.getInstance().createInputPort(this, FROM + i, Double.class);
                                PortFactory.getInstance().createInputPort(this, TO + i, Double.class);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                            logger.debug("Stack trace ", e);
                        }
                    }
                }
            } catch (IllegalActionException e) {
                throw e;
            } catch (Exception e) {
                logger.error(e.getMessage());
                logger.debug("Stack trace ", e);
            }
        } else {
            super.attributeChanged(arg0);
        }
    }

    private void clearDynamicPort() throws IllegalActionException {
        List<?> portList = portList();
        if (portList != null) {
            List<Object> portListCopy = new ArrayList<Object>();
            portListCopy.addAll(portList);
            Port port = null;
            String portName = null;
            for (Object objectPort : portListCopy) {
                if (objectPort instanceof Port) {
                    port = (Port) objectPort;
                    portName = port.getName();
                    if (isDynamicPort(portName)) {
                        try {
                            port.setContainer(null);
                        } catch (NameDuplicationException e) {
                            logger.error(e.getMessage());
                            logger.debug("Stack trace ", e);
                        }
                    }
                }
            }
        }
    }

    private boolean isDynamicPort(String portName) {
        boolean dynamic = false;
        if (portName != null) {
            dynamic = portName.startsWith(FROM) && portName.length() > FROM.length();
            if (!dynamic) {
                dynamic = portName.startsWith(TO) && portName.length() > TO.length();
            }
        }
        return dynamic;
    }

}
