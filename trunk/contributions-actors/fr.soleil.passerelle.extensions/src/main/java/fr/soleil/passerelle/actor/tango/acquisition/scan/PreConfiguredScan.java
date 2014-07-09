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
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.salsa.entity.ITrajectory;
import fr.soleil.salsa.entity.impl.scan1d.Range1DImpl;
import fr.soleil.salsa.entity.impl.scan1d.Trajectory1DImpl;
import fr.soleil.salsa.entity.scan1d.IRange1D;
import fr.soleil.salsa.entity.scank.IConfigK;

/**
 * Do scans using Salsa config, but the From/To NbSteps and InteegrationTime can
 * be configured
 * 
 * @author GRAMER
 */
@SuppressWarnings("serial")
public class PreConfiguredScan extends Scan {

    public List<Port> fromPortList;
    public List<Port> toPortList;
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
    private IntToken nbActuatorDefaultValue = new IntToken(nbActuator);

    public PreConfiguredScan(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        // List of dynamic port for from and to input
        fromPortList = new ArrayList<Port>();
        toPortList = new ArrayList<Port>();

        // Parameter X relative sent in all trajectories
        xRelativeParam = new Parameter(this, "X Relative", new BooleanToken(xRelative));
        xRelativeParam.setTypeEquals(BaseType.BOOLEAN);

        // Parameter number of actuators by default 1
        nbActuatorParam = new Parameter(this, "Nb actuators", nbActuatorDefaultValue);
        nbActuatorParam.setTypeEquals(BaseType.INT);

        // First From input port always existing
        input.setName(FROM);
        input.setExpectedMessageContentType(Double.class);
        fromPortList.add(input);

        // First To input port always existing
        Port toPort = PortFactory.getInstance().createInputPort(this, TO, Double.class);
        toPortList.add(toPort);

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
        // All the other kind of configuration have an X linear dimension
        if (conf == null || conf instanceof IConfigK) {
            String errorMessage = "Error: " + conf.getFullPath() + " is not a linear trajectory";
            ExecutionTracerService.trace(this, errorMessage);
            throw new ValidationException(ErrorCode.ERROR, errorMessage, this, null);
        }
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        StringBuilder logMessage = new StringBuilder();
        String log = xRelative ? "Relative scan" : "Absolute scan";
        logger.debug(log);
        logMessage.append("\n" + log);

        final ManagedMessage nbStepsmessage = request.getMessage(stepsPort);
        final int nbSteps = (int) ((Double) PasserelleUtil.getInputValue(nbStepsmessage)).doubleValue();
        log = STEP + "=" + nbSteps;
        logger.debug(log);
        logMessage.append(log);

        final ManagedMessage intTimemessage = request.getMessage(integrationPort);
        final double intTime = (Double) PasserelleUtil.getInputValue(intTimemessage);
        log = INTEGRATION + "=" + intTime + " s";
        logger.debug(log);
        logMessage.append("\n" + log);

        final IRange1D range = new Range1DImpl();
        range.setStepsNumber(nbSteps);
        range.setIntegrationTime(intTime);

        List<ITrajectory> trajectoryList = new ArrayList<ITrajectory>();
        ITrajectory trajectory = null;

        ManagedMessage fromMessage = null;
        ManagedMessage toMessage = null;
        Port fromPort = null;
        Port toPort = null;
        double fromValue = Double.NaN;
        double toValue = Double.NaN;

        for (int i = 0; i < fromPortList.size(); i++) {
            fromPort = fromPortList.get(i);
            toPort = toPortList.get(i);
            fromMessage = request.getMessage(fromPort);
            toMessage = request.getMessage(toPort);
            fromValue = (Double) PasserelleUtil.getInputValue(fromMessage);
            toValue = (Double) PasserelleUtil.getInputValue(toMessage);
            log = FROM + i + "=" + fromValue;
            logger.debug(log);
            logMessage.append("\n" + log);
            log = TO + i + "=" + toValue;
            logger.debug(log);
            logMessage.append("\n" + log);
            trajectory = new Trajectory1DImpl();
            trajectory.setIRange(range);
            trajectory.setBeginPosition(fromValue);
            trajectory.setEndPosition(toValue);
            trajectoryList.add(trajectory);
        }

        range.setTrajectoriesList(trajectoryList);

        if (!isMockMode()) {
            try {
                ScanUtil.setTrajectory1D(conf, range, xRelative);
            } catch (PasserelleException e) {
                throw new ProcessingException(ErrorCode.ERROR, e.getMessage(), this, e);
            }
            ExecutionTracerService.trace(this, logMessage.toString());
        } else {
            ExecutionTracerService.trace(this, "MOCK - " + logMessage);
        }
        super.process(ctxt, request, response);
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == xRelativeParam) {
            xRelative = PasserelleUtil.getParameterBooleanValue(xRelativeParam);
        } else if (arg0 == nbActuatorParam) {
            // Read the nb actuator parameter
            IntToken token = (IntToken)nbActuatorParam.getToken();
            int newNbActuator =  token.intValue();

            // The minimum value must be one
            if (newNbActuator < 1) {
                newNbActuator = 1;
                nbActuatorParam.setToken(nbActuatorDefaultValue);
            }

            // If the value change
            if (newNbActuator != nbActuator) {
                nbActuator = newNbActuator;
                //Clear dynamic port
                clearDynamicPort();
                
                //Create dynamic port over index 0
                Port inputPort = null;
                if(nbActuator > 1){
                try {
                        for (int i = 1; i < nbActuator; i++) {
                            inputPort = PortFactory.getInstance().createInputPort(this, FROM + i, Double.class);
                            fromPortList.add(inputPort);
    
                            inputPort = PortFactory.getInstance().createInputPort(this, TO + i, Double.class);
                            toPortList.add(inputPort);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        } else {
            super.attributeChanged(arg0);
        }
    }

    private void clearDynamicPort() throws IllegalActionException {
        Port fromPort = null;
        // Do not remove the first fix port index 0
        if (fromPortList.size() > 1) {
            List<Port> fromPortToRemove = new ArrayList<Port>();
            for (int portIndex = 1; portIndex < fromPortList.size(); portIndex++) {
                fromPort = fromPortList.get(portIndex);
                fromPortToRemove.add(fromPort);
                try {
                    fromPort.setContainer(null);
                } catch (NameDuplicationException e) {
                    logger.error(e.getMessage());
                }
            }
            //Remove the dynamic port from the list
            fromPortList.removeAll(fromPortToRemove);
        }

        Port toPort = null;
        // Do not remove the first fix port index 0
        if (toPortList.size() > 1) {
            List<Port> toPortToRemove = new ArrayList<Port>();
            for (int portIndex = 1; portIndex < toPortList.size(); portIndex++) {
                toPort = toPortList.get(portIndex);
                toPortToRemove.add(toPort);
                try {
                    toPort.setContainer(null);
                } catch (NameDuplicationException e) {
                    logger.error(e.getMessage());
                }
            }
            //Remove the dynamic port from the list
            toPortList.removeAll(toPortToRemove);
        }
    }

}
