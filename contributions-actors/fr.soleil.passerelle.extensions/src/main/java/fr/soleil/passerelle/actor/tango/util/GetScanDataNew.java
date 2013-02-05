package fr.soleil.passerelle.actor.tango.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeProxy;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.soleil.passerelle.actor.PortUtilities;
import fr.soleil.passerelle.actor.TransformerV5;
import fr.soleil.passerelle.actor.tango.acquisition.scan.ScanUtil;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;
import fr.soleil.salsa.entity.IActuator;
import fr.soleil.salsa.entity.IScanResult;
import fr.soleil.salsa.entity.ISensor;
import fr.soleil.salsa.entity.scan2D.IScanResult2D;
import fr.soleil.salsa.exception.SalsaDeviceException;
import fr.soleil.salsa.exception.SalsaScanConfigurationException;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * Actuators and Sensors extraction thank's to their names
 * 
 * @author GRAMER
 */

@SuppressWarnings("serial")
public class GetScanDataNew extends TransformerV5 {

    private final static Logger logger = LoggerFactory.getLogger(GetScanDataNew.class);

    private static final String SENSOR = "sensor";
    private static final String ACTUATOR = "actuator";
    private static final String TRAJECTORY = "get trajectory ?";

    /**
     * Actuators List fill with a number of actuator or an attribute nominative list Sensors and
     * Actuators must be filled with the same mode
     */
    @ParameterName(name = ACTUATOR)
    public Parameter actuatorsParam;
    private String actuatorsString = "";
    private String[] actuators = {};
    private boolean isAnActuatorsList = false;
 //   private int actuatorsNb = 0;

    /**
     * Sensors List fill with a number of sensor or an attribute nominative list Sensors and
     * Actuators must be filled with the same mode
     */
    @ParameterName(name = SENSOR)
    public Parameter sensorsParam;
    private String[] sensors = {};
    private String sensorsString = "";
    private boolean isASensorsList = false;
 //   private int sensorsNb = 0;

    @ParameterName(name = TRAJECTORY)
    public Parameter trajectoryParam;
    private boolean isTrajectoryValue = false;

    private final List<Port> actuatorsPorts = Collections.synchronizedList(new ArrayList<Port>());
    private final List<Port> sensorsPorts = Collections.synchronizedList(new ArrayList<Port>());
    private final Map<String, String> actuatorsSources = new HashMap<String, String>();
    private final Map<String, String> sensorsSources = new HashMap<String, String>();

    public GetScanDataNew(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        actuatorsParam = new StringParameter(this, "Actuators (name or number)");
        actuatorsParam.setExpression(actuatorsString);

        sensorsParam = new StringParameter(this, "Sensors (name or number)");
        sensorsParam.setExpression(sensorsString);

        output.setName("timestamps");

        trajectoryParam = new Parameter(this, TRAJECTORY, new BooleanToken(isTrajectoryValue));
        trajectoryParam.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    protected void doInitialize() throws InitializationException {
        super.doInitialize();
        actuatorsSources.clear();
        sensorsSources.clear();

        // TODO : vérifier qu'il n'est pas nécessaire de faire un clear de la liste des ports
    }

    @Override
    protected void validateInitialization() throws ValidationException {
        // TODO Auto-generated method stub
        super.validateInitialization();
        // IL doit y avoir au minimum 1 ou plusieurs sensors/actuators
        if (actuators.length == 0 || sensors.length == 0) {
            throw new ValidationException(Severity.FATAL,
                    "Actuators and Sensors list must not be empties !", this, null);
        }
    }

    @Override
    protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
            throws ProcessingException {

        if (isMockMode()) {
            final List<Port> outputPortList = outputPortList();
            for (final Port port : outputPortList) {
                if (port.getName().startsWith(SENSOR) || port.getName().startsWith(ACTUATOR)) {
                    ExecutionTracerService.trace(this, "MOCK - reading data for " + port.getName());
                    sendOutputMsg(port, PasserelleUtil.createContentMessage(this, "1,2,3"));
                }
            }
        }
        else {
            try {

                final IScanResult res = ScanUtil.getCurrentSalsaApi().readScanResult();
                final List<IActuator> actuatorList = res.getActuatorsXList();
                final List<ISensor> sensorList = res.getSensorsList();

                // if(!isAnActuatorsList){
                // if (actuatorList.size() != actuatorsNb) {
                // throw new ProcessingExceptionWithLog(this,
                // "Actuator number port is incorect. Correct number is "
                // + actuatorList.size(), this, null);
                // }
                // }
                // if scan is a 2D scan we add Y actuator at the end of actuator
                // list.
                if (res.getResultType() == IScanResult.ResultType.RESULT_2D) {
                    final IScanResult2D res2D = (IScanResult2D) res;
                    actuatorList.addAll(res2D.getActuatorsYList());
                }

                /**
                 * Récupération de la liste des actuators et des sensors existants dans le dernier
                 * scan
                 **/
                // create actuator source map with all the existing actuators in the Scan:
                // -keys : actuatorName (ex tmp/passerelle/device.1/position)
                // -value : scanServerDataName (exe test/scan/julien/actuator_1_1)
                final Iterator<IActuator> iteratorAct = actuatorList.iterator();
                String name;
                while (iteratorAct.hasNext()) {
                    final IActuator actuator = iteratorAct.next();
                    name = actuator.getName().toLowerCase();
                    actuatorsSources.put(name, actuator.getScanServerAttributeName().toLowerCase());
                    logger.debug("add act name " + name);
                }

                // create sensor source map with all the existing sensors in the Scan:
                final Iterator<ISensor> iteratorSen = sensorList.iterator();
                while (iteratorSen.hasNext()) {
                    final ISensor sensor = iteratorSen.next();
                    name = sensor.getName().toLowerCase();
                    sensorsSources.put(name, sensor.getScanServerAttributeName().toLowerCase());
                    logger.debug("add sensor name " + name);
                }

                /** Verification de l'existance des sensors/actutors demandés **/
                int i;
                String scanDataName;
                for (i = 0; i < actuators.length; i++) {
                    scanDataName = actuatorsSources.get(actuators[i]);
                    if (scanDataName == null) {
                        throw new ProcessingExceptionWithLog(this, "Actuator " + actuators[i]
                                + " does not exists on current scan", actuators[i], null);
                    }
                }

                for (i = 0; i < sensors.length; i++) {
                    scanDataName = sensorsSources.get(sensors[i]);
                    if (scanDataName == null) {
                        throw new ProcessingExceptionWithLog(this, "Sensor " + sensors[i]
                                + " does not exists on current scan", sensors[i], null);
                    }
                }

                /** Envoie des informations sur les ports de sorties **/
                // output timestamps
                final TangoAttribute timestamps = new TangoAttribute(
                        res.getSensorsTimeStampsCompleteName()); // ScanUtil.getCurrentContext().getScanServerName()
                                                                 // + "/sensorsTimestamps");
                // read attribute is done by the TangoAttribute constructor
                ExecutionTracerService.trace(this, "reading data for sensorTimestamps ");
                sendOutputMsg(output, PasserelleUtil.createContentMessage(this, timestamps));

                // output sensor and actuators
                final List<Port> orderedActuatorPorts = PortUtilities.getOrderedOutputPorts(this,
                        ACTUATOR, 0);
                final List<Port> orderedSensorPorts = PortUtilities.getOrderedOutputPorts(this,
                        SENSOR, 0);

                if (isTrajectoryValue) {
                    Map<String, double[]> realTrajectoryValues = new HashMap<String, double[]>();

                    // Récupération de l'ensemble des trajectoires
                    AttributeProxy att = new AttributeProxy(res.getScanServer() + "/trajectories");
                    AttributeProxy nbActuators = new AttributeProxy(res.getScanServer()
                            + "/actuators");
                    if (att != null && nbActuators != null) {
                        DeviceAttribute attribute = att.read();
                        double[] allValues = att.read().extractDoubleArray();
                        System.out.println("allValues " + Arrays.toString(allValues));
                        DeviceAttribute actuators = nbActuators.read();
                        String[] actuatorsList = actuators.extractStringArray();
                        int nbAct = actuatorsList.length;

                        if (allValues != null && allValues.length > 0) {
                            double[] readValues = Arrays.copyOf(allValues, attribute.getNbRead());
                            System.out.println("flatValues " + Arrays.toString(readValues));

                            int nbLine = attribute.getNbRead() / nbAct;
                            double[] line = null;
                            // Trajectory trajectory = null;
                            for (i = 0; i < nbAct; i++) {
                                line = new double[nbLine];
                                System.arraycopy(readValues, i * nbLine, line, 0, nbLine);
                                System.out.println("Line " + i + " " + Arrays.toString(line));
                                realTrajectoryValues.put(actuatorsList[i], line);
                            }
                        }

                    }

                    for (i = 0; i < actuators.length; i++) {
                        double[] trajectory = realTrajectoryValues.get(actuators[i]);
                        if (trajectory != null) {
                            System.out.println("flatValues " + Arrays.toString(trajectory));
                            sendOutputMsg(orderedActuatorPorts.get(i),
                                    PasserelleUtil.createContentMessage(this, trajectory));
                            ExecutionTracerService.trace(this, "reading data for actuator "
                                    + actuators[i]);
                        }
                        else {
                            sendOutputMsg(orderedActuatorPorts.get(i),
                                    PasserelleUtil.createContentMessage(this, new double[0]));
                            ExecutionTracerService.trace(this, "No reading data for actuator "
                                    + actuators[i]);
                        }
                    }
                }
                else {
                    System.out.println("===============> actuators " + Arrays.toString(actuators));

                    for (i = 0; i < actuators.length; i++) {
                        scanDataName = actuatorsSources.get(actuators[i]);

                        final TangoAttribute act = new TangoAttribute(scanDataName);
                        // read attribute is done by the TangoAttribute constructor
                        ExecutionTracerService.trace(this, "reading data for actuator "
                                + actuators[i]);
                        sendOutputMsg(orderedActuatorPorts.get(i),
                                PasserelleUtil.createContentMessage(this, act));
                    }
                }

                for (i = 0; i < sensors.length; i++) {
                    scanDataName = sensorsSources.get(sensors[i]);

                    final TangoAttribute sensor = new TangoAttribute(scanDataName);
                    // read attribute is done by the TangoAttribute constructor
                    ExecutionTracerService.trace(this, "reading data for sensor " + sensors[i]);
                    sendOutputMsg(orderedSensorPorts.get(i),
                            PasserelleUtil.createContentMessage(this, sensor));
                }
            }
            catch (final DevFailed e) {
                throw new DevFailedProcessingException(e, this);

            }
            catch (final SalsaDeviceException e) {
                throw new ProcessingException(e.getMessage(), this, e);

            }
            catch (final SalsaScanConfigurationException e) {
                throw new ProcessingException(e.getMessage(), this, e);
            }
        }
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        // Output dynamique ports creation
        if (attribute == actuatorsParam) {
            // System.out.println("===============> actuators Avant " + Arrays.toString(actuators));
            actuators = readScanParameter(actuatorsParam, actuatorsString);
            constructOutputPorts(ACTUATOR, actuators.length, actuatorsPorts, actuatorsString,
                    isAnActuatorsList);

        }
        else if (attribute == sensorsParam) {
            sensors = readScanParameter(sensorsParam, sensorsString);
            constructOutputPorts(SENSOR, sensors.length, sensorsPorts, sensorsString,
                    isASensorsList);
        }
        else if (attribute == trajectoryParam) {
            isTrajectoryValue = ((BooleanToken) trajectoryParam.getToken()).booleanValue();
        }
        else {
            super.attributeChanged(attribute);
        }

    }

    private String[] readScanParameter(final Parameter paramObj, String paramValue)
            throws IllegalActionException {

        String[] elementList = {};
        paramValue = PasserelleUtil.getParameterValue(paramObj);
        if (!paramValue.isEmpty()) {
            elementList = paramValue.toLowerCase().split(",");
        }
        return elementList;

    }

    private void constructOutputPorts(final String constantPortName,
    final int newPortCount, List<Port> portList, String paramValue,
            boolean isManagedByList) throws IllegalActionException {
  
        int nrPorts = portList.size();

        // if (paramValue.isEmpty()) {
        // newPortCount = 0;
        // }
        // else {
        /*    try {
                // Remplissage avec un nombre
                newPortCount = Integer.parseInt(paramValue);
                isManagedByList = false;

            }
            catch (NumberFormatException e) {
                // Remplissage avec une liste exhaustive
                params = paramValue.toLowerCase().split(",");
                // System.out.println(actuatorsString);
                newPortCount = params.length;
                isManagedByList = true;
            }*/

        // System.out.println("actuactors port nr futur:"+newPortCount);
        // System.out.println("actuactors port nr actual:"+nrPorts);
        // remove no more needed ports
        if (newPortCount < nrPorts) {
            for (int i = nrPorts - 1; i >= newPortCount; i--) {
                try {
                    logger.debug("remove port :" + portList.get(i).getName());
                    portList.get(i).setContainer(null);
                    portList.remove(i);
                }
                catch (final NameDuplicationException e) {
                    throw new IllegalActionException(this, e, "Error for index " + i);
                }
            }

        }// add missing ports
        else if (newPortCount > nrPorts) {
            for (int i = nrPorts; i < newPortCount; i++) {
                try {
                    // System.out.println("checking for port :"+i);
                    final String portName = constantPortName + i;
                    Port extraOutputPort = (Port) getPort(portName);
                    if (extraOutputPort == null) {
                        extraOutputPort = PortFactory.getInstance()
                                .createOutputPort(this, portName);
                    }
                    logger.debug("adding port :" + extraOutputPort.getName());
                    portList.add(extraOutputPort);

                }
                catch (final NameDuplicationException e) {
                    throw new IllegalActionException(this, e, "Error for index " + i);
                }
            }
        }

    }

    // @Override
    // protected String getExtendedInfo() {
    // return this.getName();
    // }

}
