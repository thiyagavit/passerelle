package fr.soleil.passerelle.actor.tango.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.soleil.passerelle.actor.PortUtilities;
import fr.soleil.passerelle.salsa.SalsaFactory;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.salsa.model.SalsaModel;
import fr.soleil.salsa.model.ScanServer;
import fr.soleil.salsa.model.scanconfig.ScanConfiguration;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.util.display.DataSource;
import fr.soleil.util.display.tango.TangoSource;

@SuppressWarnings("serial")
public class GetScanDataOLD extends Transformer {

    private final static Logger logger = LoggerFactory.getLogger(GetScanDataOLD.class);

    private static final String SENSOR = "sensor";
    private static final String ACTUATOR = "actuator";
    protected ScanConfiguration config = null;
    protected SalsaModel model;
    protected ScanServer scanServer;

    public Parameter actuatorsParam;
    private String[] actuators = {};
    private String actuatorsString = "";

    public Parameter sensorsParam;
    private String[] sensors = {};
    private String sensorsString = "";

    private final List<Port> actuatorsPorts = Collections.synchronizedList(new ArrayList<Port>());
    private final List<Port> sensorsPorts = Collections.synchronizedList(new ArrayList<Port>());
    private final Map<String, String> actuatorsSources = new HashMap<String, String>();
    private final Map<String, String> sensorsSources = new HashMap<String, String>();

    public GetScanDataOLD(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        actuatorsParam = new StringParameter(this, "Actuators");
        actuatorsParam.setExpression(actuatorsString);

        sensorsParam = new StringParameter(this, "Sensors");
        sensorsParam.setExpression(sensorsString);

        output.setName("timestamps");
    }

    @Override
    protected void doInitialize() throws InitializationException {
        actuatorsSources.clear();
        sensorsSources.clear();
        if (!isMockMode()) {
            try {
                model = SalsaFactory.getInstance().createSalsaModel();
            } catch (final ConnectionException e) {
                ExceptionUtil.throwInitializationException("Cannot connect to scan server", this, e);
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
            scanServer = model.getScanServer();
            if (scanServer == null) {
                ExceptionUtil.throwInitializationException("Cannot connect to scan server", this);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void doFire(final ManagedMessage message) throws ProcessingException {
        if (isMockMode()) {
            final List<Port> outputPortList = outputPortList();
            for (final Port port : outputPortList) {
                if (port.getName().startsWith(SENSOR) || port.getName().startsWith(ACTUATOR)) {
                    ExecutionTracerService.trace(this, "MOCK - reading data for " + port.getName());
                    sendOutputMsg(port, PasserelleUtil.createContentMessage(this, "1,2,3"));
                }
            }

        } else {
            try {
                final TangoSource source = scanServer.getSource();
                // System.out.println("source " + source);
                for (int i = 0; i < source.getSize(); i++) {
                    final DataSource attr = source.get(i);
                    final String sensorActName = attr.getName().toLowerCase();
                    final String scanDataName = attr.getOrigin().toLowerCase();
                    // System.out.println("source name " + attr.getName());
                    // System.out.println("source orig " + attr.getOrigin());

                    if (attr.getType() == ScanServer.ACTUATOR_TYPE) {
                        actuatorsSources.put(sensorActName, scanDataName);
                        logger.debug("add act name " + sensorActName);
                    } else if (attr.getType() == ScanServer.SENSOR_TYPE) {
                        sensorsSources.put(sensorActName, scanDataName);
                        logger.debug("add sensor name " + sensorActName);
                    }
                }

                // output timestamps
                String scanDataName = source.get(0).getOrigin();
                // System.out.println(scanDataName);
                final TangoAttribute timestamps = new TangoAttribute(scanDataName);
                // read attribute is done by the TangoAttribute constructor
                sendOutputMsg(output, PasserelleUtil.createContentMessage(this, timestamps));

                // output sensor and actuators

                final List<Port> orderedActuatorPorts = PortUtilities.getOrderedOutputPorts(this, ACTUATOR, 0);
                final List<Port> orderedSensorPorts = PortUtilities.getOrderedOutputPorts(this, SENSOR, 0);

                for (int i = 0; i < actuators.length; i++) {
                    scanDataName = actuatorsSources.get(actuators[i]);
                    if (scanDataName == null) {
                        ExceptionUtil.throwProcessingException("Actuator " + actuators[i]
                                + " does not exists on current scan", actuators[i]);
                    }
                    // System.out.println("source act name " + scanDataName);
                    final TangoAttribute act = new TangoAttribute(scanDataName);
                    // read attribute is done by the TangoAttribute constructor
                    ExecutionTracerService.trace(this, "reading data for actuator " + actuators[i]);
                    sendOutputMsg(orderedActuatorPorts.get(i), PasserelleUtil.createContentMessage(this, act));
                }
                for (int i = 0; i < sensors.length; i++) {
                    scanDataName = sensorsSources.get(sensors[i]);
                    if (scanDataName == null) {
                        ExceptionUtil.throwProcessingException("Sensor " + sensors[i]
                                + " does not exists on current scan", sensors[i]);
                    }
                    // System.out.println("source sensor name " + scanDataName);
                    final TangoAttribute sensor = new TangoAttribute(scanDataName);
                    // read attribute is done by the TangoAttribute constructor
                    ExecutionTracerService.trace(this, "reading data for sensor " + sensors[i]);
                    sendOutputMsg(orderedSensorPorts.get(i), PasserelleUtil.createContentMessage(this, sensor));
                }
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == actuatorsParam) {
            actuatorsString = PasserelleUtil.getParameterValue(actuatorsParam);
            actuators = actuatorsString.toLowerCase().split(",");
            final int nrPorts = actuatorsPorts.size();

            // System.out.println(actuatorsString);
            int newPortCount = actuators.length;
            if (actuatorsString.compareTo("") == 0) {
                newPortCount = 0;
            }
            // System.out.println("actuactors port nr futur:"+newPortCount);
            // System.out.println("actuactors port nr actual:"+nrPorts);
            // remove no more needed ports
            if (newPortCount < nrPorts) {
                for (int i = nrPorts - 1; i >= newPortCount; i--) {
                    try {
                        logger.debug("remove port :" + actuatorsPorts.get(i).getName());
                        actuatorsPorts.get(i).setContainer(null);
                        actuatorsPorts.remove(i);
                    } catch (final NameDuplicationException e) {
                        throw new IllegalActionException(this, e, "Error for index " + i);
                    }
                }

            }// add missing ports
            else if (newPortCount > nrPorts) {
                for (int i = nrPorts; i < newPortCount; i++) {
                    try {
                        // System.out.println("checking for port :"+i);
                        final String portName = ACTUATOR + i;
                        Port extraOutputPort = (Port) getPort(portName);
                        if (extraOutputPort == null) {
                            extraOutputPort = PortFactory.getInstance().createOutputPort(this, portName);
                        }
                        logger.debug("adding port :" + extraOutputPort.getName());
                        actuatorsPorts.add(extraOutputPort);

                    } catch (final NameDuplicationException e) {
                        throw new IllegalActionException(this, e, "Error for index " + i);
                    }
                }
            }
        } else if (attribute == sensorsParam) {
            sensorsString = PasserelleUtil.getParameterValue(sensorsParam);
            sensors = sensorsString.toLowerCase().split(",");
            final int nrPorts = sensorsPorts.size();
            int newPortCount = sensors.length;
            if (sensorsString.compareTo("") == 0) {
                newPortCount = 0;
            }
            // remove no more needed ports
            if (newPortCount < nrPorts) {
                for (int i = nrPorts - 1; i >= newPortCount; i--) {
                    try {
                        sensorsPorts.get(i).setContainer(null);
                        sensorsPorts.remove(i);
                    } catch (final NameDuplicationException e) {
                        throw new IllegalActionException(this, e, "Error for index " + i);
                    }
                }

            }// add missing ports
            else if (newPortCount > nrPorts) {
                for (int i = nrPorts; i < newPortCount; i++) {
                    try {
                        final String intputName = SENSOR + i;// sensors[i];
                        Port extraOutputPort = (Port) getPort(intputName);
                        if (extraOutputPort == null) {
                            extraOutputPort = PortFactory.getInstance().createOutputPort(this, intputName);
                        }
                        System.out.println("adding port :" + extraOutputPort.getName());
                        sensorsPorts.add(extraOutputPort);
                    } catch (final NameDuplicationException e) {
                        throw new IllegalActionException(this, e, "Error for index " + i);
                    }
                }
            }
        }

        super.attributeChanged(attribute);
    }

}
