package fr.soleil.passerelle.actor.tango.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.PortUtilities;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.salsa.entity.IActuator;
import fr.soleil.salsa.entity.IScanResult;
import fr.soleil.salsa.entity.ISensor;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * Actuators and Sensors extraction thank's to their names
 * 
 * @author GRAMER
 */

@SuppressWarnings("serial")
public class GetScanData extends AbstractGetScanData {

    private final static Logger logger = LoggerFactory.getLogger(GetScanData.class);

    /**
     * Actuators List fill with a number of actuator or an attribute nominative list Sensors and
     * Actuators must be filled with the same mode
     */
    private String actuatorsString = "";
    private String[] actuators = {};

    /**
     * Sensors List fill with a number of sensor or an attribute nominative list Sensors and
     * Actuators must be filled with the same mode
     */
    private String[] sensors = {};
    private String sensorsString = "";

    private final Map<String, String> actuatorsSources = new HashMap<String, String>();
    private final Map<String, String> sensorsSources = new HashMap<String, String>();

    public GetScanData(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
    }

    @Override
    protected void doInitialize() throws InitializationException {
        super.doInitialize();
        actuatorsSources.clear();
        sensorsSources.clear();
    }

    @Override
    protected void realProcessImpl(final IScanResult res, final List<IActuator> actuatorList,
            final List<ISensor> sensorList) throws ProcessingException, DevFailed

    {
        /**
         * Récupération de la liste des actuators et des sensors existants dans le dernier scan
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

        /** Verification de l'existance des sensors/actuators demandés **/
        int i;
        String scanDataName;
        for (i = 0; i < actuators.length; i++) {
            scanDataName = actuatorsSources.get(actuators[i]);
            if (scanDataName == null) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, "Actuator " + actuators[i]
                        + " does not exists on current scan", actuators[i]);
            }
        }

        for (i = 0; i < sensors.length; i++) {
            scanDataName = sensorsSources.get(sensors[i]);
            if (scanDataName == null) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, "Sensor " + sensors[i]
                        + " does not exists on current scan", sensors[i]);
            }
        }

        /** Envoie des informations sur les ports de sorties **/
        // output sensor and actuators
        final List<Port> orderedActuatorPorts = PortUtilities.getOrderedOutputPorts(this, ACTUATOR, 0);
        if (isTrajectoryValue) {
            Map<String, double[]> realTrajectoryValues = super.getRealTrajectory(res);
            // Map<IActuator, double[]> realTrajectoryValues = res.getTrajectoryMap();

            for (i = 0; i < actuators.length; i++) {
                double[] trajectory = realTrajectoryValues.get(actuators[i]);
                if (trajectory != null) {
                    // System.out.println("trajectory " + Arrays.toString(trajectory));
                    sendOutputMsg(orderedActuatorPorts.get(i), PasserelleUtil.createContentMessage(this, trajectory));
                    ExecutionTracerService.trace(this, "reading trajectory for actuator " + actuators[i]);
                } else {
                    sendOutputMsg(orderedActuatorPorts.get(i), PasserelleUtil.createContentMessage(this, new double[0]));
                    ExecutionTracerService.trace(this, "No reading trajectory for actuator " + actuators[i]);
                }
            }
        } else {
            // System.out.println("===============> actuators " + Arrays.toString(actuators));

            for (i = 0; i < actuators.length; i++) {
                scanDataName = actuatorsSources.get(actuators[i]);

                final TangoAttribute act = new TangoAttribute(scanDataName);
                // read attribute is done by the TangoAttribute constructor
                ExecutionTracerService.trace(this, "reading data for actuator " + actuators[i]);
                sendOutputMsg(orderedActuatorPorts.get(i), PasserelleUtil.createContentMessage(this, act));
            }
        }

        final List<Port> orderedSensorPorts = PortUtilities.getOrderedOutputPorts(this, SENSOR, 0);
        for (i = 0; i < sensors.length; i++) {
            scanDataName = sensorsSources.get(sensors[i]);

            final TangoAttribute sensor = new TangoAttribute(scanDataName);
            // read attribute is done by the TangoAttribute constructor
            ExecutionTracerService.trace(this, "reading data for sensor " + sensors[i]);
            sendOutputMsg(orderedSensorPorts.get(i), PasserelleUtil.createContentMessage(this, sensor));
        }

        super.sendTimestampsOnOutputPort(res.getActuatorsTimeStampsCompleteName());
    }

    private String[] readScanParameter(final Parameter paramObj, String paramValue) throws IllegalActionException {

        String[] elementList = {};
        paramValue = PasserelleUtil.getParameterValue(paramObj);
        if (!paramValue.isEmpty()) {
            elementList = paramValue.toLowerCase().split(",");
        }
        return elementList;

    }

    @Override
    protected String getActuatorParamName() {
        return "Actuators";
    }

    @Override
    protected String getSensorParamName() {
        return "Sensors";
    }

    @Override
    protected String getInitActuatorParamValue() {
        return actuatorsString;
    }

    @Override
    protected String getInitSensorParamValue() {
        return sensorsString;
    }

    @Override
    protected int getNbRequieredActuator() {
        return actuators.length;
    }

    @Override
    protected int getNbRequieredSensor() {
        return sensors.length;
    }

    @Override
    protected void readActuatorValue() throws IllegalActionException {
        actuators = readScanParameter(actuatorsParam, actuatorsString);
    }

    @Override
    protected void readSensorValue() throws IllegalActionException {
        sensors = readScanParameter(sensorsParam, sensorsString);
    }
}
