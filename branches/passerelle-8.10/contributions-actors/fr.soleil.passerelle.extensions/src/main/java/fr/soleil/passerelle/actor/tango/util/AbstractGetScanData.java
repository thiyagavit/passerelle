package fr.soleil.passerelle.actor.tango.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
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
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeProxy;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.soleil.passerelle.actor.TransformerV5;
import fr.soleil.passerelle.actor.tango.acquisition.scan.ScanUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.salsa.entity.IActuator;
import fr.soleil.salsa.entity.IScanResult;
import fr.soleil.salsa.entity.ISensor;
import fr.soleil.salsa.entity.scan2D.IScanResult2D;
import fr.soleil.salsa.exception.SalsaDeviceException;
import fr.soleil.salsa.exception.SalsaScanConfigurationException;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * Actuators and Sensors extraction
 * 
 * @author PierreJosephZephir
 */

@SuppressWarnings("serial")
public abstract class AbstractGetScanData extends TransformerV5 {

    private final static Logger logger = LoggerFactory.getLogger(AbstractGetScanData.class);

    protected static final String SENSOR = "sensor";
    protected static final String ACTUATOR = "actuator";
    private static final String TRAJECTORY = "Trajectory ?";

    /**
     * Actuators List fill with a number of actuator or an attribute nominative list Sensors and
     * Actuators must be filled with the same mode
     */
    @ParameterName(name = ACTUATOR)
    public Parameter actuatorsParam;

    /**
     * Sensors List fill with a number of sensor or an attribute nominative list Sensors and
     * Actuators must be filled with the same mode
     */
    @ParameterName(name = SENSOR)
    public Parameter sensorsParam;

    @ParameterName(name = TRAJECTORY)
    public Parameter trajectoryParam;
    protected boolean isTrajectoryValue = false;

    protected final List<Port> actuatorsPorts = Collections.synchronizedList(new ArrayList<Port>());
    protected final List<Port> sensorsPorts = Collections.synchronizedList(new ArrayList<Port>());

    public AbstractGetScanData(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        actuatorsParam = new StringParameter(this, getActuatorParamName());
        actuatorsParam.setExpression(getInitActuatorParamValue());

        sensorsParam = new StringParameter(this, getSensorParamName());
        sensorsParam.setExpression(getInitSensorParamValue());

        output.setName("timestamps");

        trajectoryParam = new Parameter(this, TRAJECTORY, new BooleanToken(isTrajectoryValue));
        trajectoryParam.setTypeEquals(BaseType.BOOLEAN);
    }

    protected abstract String getActuatorParamName();

    protected abstract String getSensorParamName();

    protected abstract String getInitActuatorParamValue();

    protected abstract String getInitSensorParamValue();

    protected abstract int getNbRequieredActuator();

    protected abstract int getNbRequieredSensor();

    protected abstract void readActuatorValue() throws IllegalActionException;

    protected abstract void readSensorValue() throws IllegalActionException;

    @Override
    protected void validateInitialization() throws ValidationException {
        super.validateInitialization();
        // IL doit y avoir au minimum 1 ou plusieurs sensors/actuators
        if (getNbRequieredActuator() == 0 || getNbRequieredSensor() == 0) {
            ExceptionUtil.throwValidationException(ErrorCode.FATAL, "Actuators and Sensors list must not be empties !",
                    this);
        }
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        // Output dynamique ports creation
        if (attribute == actuatorsParam) {
            readActuatorValue();
            constructOutputPorts(ACTUATOR, getNbRequieredActuator(), actuatorsPorts);
        } else if (attribute == sensorsParam) {
            readSensorValue();
            constructOutputPorts(SENSOR, getNbRequieredSensor(), sensorsPorts);
        } else if (attribute == trajectoryParam) {
            isTrajectoryValue = ((BooleanToken) trajectoryParam.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }

    }

    private void constructOutputPorts(final String constantPortName, final int newPortCount, List<Port> portList)
            throws IllegalActionException {

        int currentNbPorts = portList.size();

        // System.out.println("actuactors port nr futur:"+newPortCount);
        // System.out.println("actuactors port nr actual:"+nrPorts);
        // remove no more needed ports
        if (newPortCount < currentNbPorts) {
            for (int i = currentNbPorts - 1; i >= newPortCount; i--) {
                try {
                    logger.debug("remove port :" + portList.get(i).getName());
                    portList.get(i).setContainer(null);
                    portList.remove(i);
                } catch (final NameDuplicationException e) {
                    throw new IllegalActionException(this, e, "Error for index " + i);
                }
            }

        }// add missing ports
        else if (newPortCount > currentNbPorts) {
            for (int i = currentNbPorts; i < newPortCount; i++) {
                try {
                    // System.out.println("checking for port :"+i);
                    final String portName = constantPortName + i;
                    Port extraOutputPort = (Port) getPort(portName);
                    if (extraOutputPort == null) {
                        extraOutputPort = PortFactory.getInstance().createOutputPort(this, portName);
                    }
                    logger.debug("adding port :" + extraOutputPort.getName());
                    portList.add(extraOutputPort);

                } catch (final NameDuplicationException e) {
                    throw new IllegalActionException(this, e, "Error for index " + i);
                }
            }
        }

    }

    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }

    protected abstract void realProcessImpl(final IScanResult res, final List<IActuator> actuatorList,
            final List<ISensor> sensorList) throws ProcessingException, DevFailed;

    @Override
    protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        if (isMockMode()) {
            final List<Port> outputPortList = outputPortList();
            for (final Port port : outputPortList) {
                if (port.getName().startsWith(SENSOR) || port.getName().startsWith(ACTUATOR)) {
                    ExecutionTracerService.trace(this, "MOCK - reading data for " + port.getName());
                }
                sendOutputMsg(port, PasserelleUtil.createContentMessage(this, "1,2,3"));
            }
        } else {
            try {
                final IScanResult res;
                if (isTrajectoryValue) {
                    res = ScanUtil.getCurrentSalsaApi().readScanResultWithTrajectories();
                } else {
                    res = ScanUtil.getCurrentSalsaApi().readScanResult();
                }
                final List<IActuator> actuatorList = res.getActuatorsXList();
                final List<ISensor> sensorList = res.getSensorsList();

                // if scan is a 2D scan we add Y actuator at the end of actuator
                // list.
                if (res.getResultType() == IScanResult.ResultType.RESULT_2D) {
                    final IScanResult2D res2D = (IScanResult2D) res;
                    actuatorList.addAll(res2D.getActuatorsYList());
                }

                realProcessImpl(res, actuatorList, sensorList);

            } catch (final SalsaDeviceException e) {
                ExceptionUtil.throwProcessingException(e.getMessage(), this, e);
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            } catch (final SalsaScanConfigurationException e) {
                ExceptionUtil.throwProcessingException(e.getMessage(), this, e);
            }
        }
    }

    protected void sendTimestampsOnOutputPort(final String TimeStampsCompleteName) throws ProcessingException,
            DevFailed {
        // output timestamps
        final TangoAttribute timestamps = new TangoAttribute(TimeStampsCompleteName);
        // read attribute is done by the TangoAttribute constructor
        ExecutionTracerService.trace(this, "reading data for sensorTimestamps ");
        sendOutputMsg(output, PasserelleUtil.createContentMessage(this, timestamps));

    }

    protected Map<String, double[]> getRealTrajectory(final IScanResult res) throws DevFailed {
        Map<String, double[]> realTrajectoryValues = new HashMap<String, double[]>();

        // Récupération de l'ensemble des trajectoires
        AttributeProxy att = new AttributeProxy(res.getScanServer() + "/trajectories");
        AttributeProxy nbActuators = new AttributeProxy(res.getScanServer() + "/actuators");
        if (att != null && nbActuators != null) {
            DeviceAttribute attribute = att.read();
            double[] allValues = att.read().extractDoubleArray();
            System.out.println("allValues " + Arrays.toString(allValues));
            DeviceAttribute actuators = nbActuators.read();
            String[] actuatorsList = actuators.extractStringArray();
            int nbAct = actuatorsList.length / 2; // Attention au Read/write

            if (allValues != null && allValues.length > 0) {
                double[] readValues = Arrays.copyOf(allValues, attribute.getNbRead());
                System.out.println("flatValues " + Arrays.toString(readValues));

                int nbLine = attribute.getNbRead() / nbAct;
                double[] line = null;
                // Trajectory trajectory = null;
                for (int i = 0; i < nbAct; i++) {
                    line = new double[nbLine];
                    System.arraycopy(readValues, i * nbLine, line, 0, nbLine);
                    System.out.println("Line " + i + " " + Arrays.toString(line));
                    realTrajectoryValues.put(actuatorsList[i], line);
                }
            }

        }
        return realTrajectoryValues;
    }

}
