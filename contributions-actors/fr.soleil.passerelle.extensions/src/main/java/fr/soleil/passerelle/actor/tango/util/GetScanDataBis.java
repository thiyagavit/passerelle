package fr.soleil.passerelle.actor.tango.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Actuators and Sensors extraction thank's to their number
 * 
 * @author GRAMER
 */
@SuppressWarnings("serial")
public class GetScanDataBis extends AbstractGetScanData {

    private final static Logger logger = LoggerFactory.getLogger(GetScanDataBis.class);

    private int actuatorsNb = 0;

    private int sensorsNb = 0;

    private final List<String> actuatorsSources = new ArrayList<String>();
    private final List<String> sensorsSources = new ArrayList<String>();

    public GetScanDataBis(CompositeEntity container, String name) throws NameDuplicationException,
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
            final List<ISensor> sensorList) throws ProcessingException, DevFailed {

        if (actuatorList.size() != actuatorsNb) {
            ExceptionUtil.throwProcessingExceptionWithLog(this, "Actuator number port is incorect. Correct number is "
                    + actuatorList.size(), this);

        }
        if (sensorList.size() != sensorsNb) {
            ExceptionUtil.throwProcessingExceptionWithLog(this, "Sensor number port is incorect. Correct number is "
                    + sensorList.size(), this);
        }

        // output sensor and actuators
        final List<Port> orderedActuatorPorts = PortUtilities.getOrderedOutputPorts(this, ACTUATOR, 0);
        final Iterator<IActuator> iteratorAct = actuatorList.iterator();
        String realScanDataName;
        String scanDataName;
        int i = 0;
        IActuator iact;
        if (isTrajectoryValue) {

            // Map<String, double[]> realTrajectoryValues = super.getRealTrajectory(res);
            Map<IActuator, double[]> realTrajectoryValues = res.getTrajectoryMap();

            while (iteratorAct.hasNext()) {
                iact = iteratorAct.next();
                realScanDataName = iact.getScanServerAttributeName();
                scanDataName = iact.getName();
                // double[] trajectory = realTrajectoryValues.get(scanDataName);
                double[] trajectory = realTrajectoryValues.get(iact);

                if (trajectory != null) {
                    System.out.println("trajectory " + Arrays.toString(trajectory));

                    logger.debug("source act name " + realScanDataName + " for attribute " + i + " : " + scanDataName);

                    // read attribute is done by the TangoAttribute constructor
                    ExecutionTracerService.trace(this, "reading trajectory for actuator " + i + " : " + scanDataName);
                    sendOutputMsg(orderedActuatorPorts.get(i), PasserelleUtil.createContentMessage(this, trajectory));
                } else {
                    sendOutputMsg(orderedActuatorPorts.get(i), PasserelleUtil.createContentMessage(this, new double[0]));
                    ExecutionTracerService.trace(this, "No reading trajectory for actuator " + scanDataName);
                }
                i++;

            }
        } else {
            while (iteratorAct.hasNext()) {
                iact = iteratorAct.next();
                realScanDataName = iact.getScanServerAttributeName();
                scanDataName = iact.getName();
                logger.debug("source act name " + realScanDataName);
                final TangoAttribute attr = new TangoAttribute(realScanDataName);
                // read attribute is done by the TangoAttribute constructor
                ExecutionTracerService.trace(this, "reading data for actuator " + i + " : " + scanDataName);
                sendOutputMsg(orderedActuatorPorts.get(i), PasserelleUtil.createContentMessage(this, attr));
                i++;

            }
        }

        final List<Port> orderedSensorPorts = PortUtilities.getOrderedOutputPorts(this, SENSOR, 0);
        final Iterator<ISensor> iteratorSen = sensorList.iterator();
        i = 0;
        ISensor sens;
        while (iteratorSen.hasNext()) {
            sens = iteratorSen.next();
            realScanDataName = sens.getScanServerAttributeName();
            scanDataName = sens.getName();
            logger.debug("source sensor name " + realScanDataName);

            final TangoAttribute sensor = new TangoAttribute(realScanDataName);
            // read attribute is done by the TangoAttribute
            // constructor
            // System.err.println(sensor.getDataType() == AttrDataFormat._IMAGE);
            ExecutionTracerService.trace(this, "reading data for sensor " + i + " : " + scanDataName);
            sendOutputMsg(orderedSensorPorts.get(i), PasserelleUtil.createContentMessage(this, sensor));
            i++;
        }

        super.sendTimestampsOnOutputPort(res.getActuatorsTimeStampsCompleteName());

    }

    @Override
    protected String getActuatorParamName() {
        return "Actuators Nr";
    }

    @Override
    protected String getSensorParamName() {
        return "Sensors Nr";
    }

    @Override
    protected String getInitActuatorParamValue() {
        return "0";
    }

    @Override
    protected String getInitSensorParamValue() {
        return "0";
    }

    @Override
    protected int getNbRequieredActuator() {
        return actuatorsNb;
    }

    @Override
    protected int getNbRequieredSensor() {
        return sensorsNb;
    }

    @Override
    protected void readActuatorValue() throws IllegalActionException {
        actuatorsNb = PasserelleUtil.getParameterIntValue(actuatorsParam);
    }

    @Override
    protected void readSensorValue() throws IllegalActionException {
        sensorsNb = PasserelleUtil.getParameterIntValue(sensorsParam);
    }
}
