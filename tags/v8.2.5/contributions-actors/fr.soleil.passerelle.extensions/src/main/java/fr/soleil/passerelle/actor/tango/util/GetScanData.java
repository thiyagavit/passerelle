package fr.soleil.passerelle.actor.tango.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.PortUtilities;
import fr.soleil.passerelle.actor.TransformerV3;
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
public class GetScanData extends TransformerV3 {

	private final static Logger logger = LoggerFactory.getLogger(GetScanData.class);

	private static final String SENSOR = "sensor";
	private static final String ACTUATOR = "actuator";

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

	public GetScanData(final CompositeEntity container, final String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);

		actuatorsParam = new StringParameter(this, "Actuators");
		actuatorsParam.setExpression(actuatorsString);

		sensorsParam = new StringParameter(this, "Sensors");
		sensorsParam.setExpression(sensorsString);

		output.setName("timestamps");
	}

	@Override
	protected void doInitialize() throws InitializationException {
		super.doInitialize();
		actuatorsSources.clear();
		sensorsSources.clear();
	}

	@Override
	protected void process(ActorContext ctxt, ProcessRequest request,
			ProcessResponse response) throws ProcessingException {

		if (isMockMode()) {
			final List<Port> outputPortList = outputPortList();
			for (final Port port : outputPortList) {
				if (port.getName().startsWith(SENSOR) || port.getName().startsWith(ACTUATOR)) {
					ExecutionTracerService.trace(this, "MOCK - reading data for " + port.getName());
					sendOutputMsg(port, PasserelleUtil.createContentMessage(this, "1,2,3"));
				}
			}
		}	else
		{
			try {

				final IScanResult res = ScanUtil.getCurrentSalsaApi().readScanResult();

				final List<IActuator> actuatorList = res.getActuatorsXList();
				final List<ISensor> sensorList = res.getSensorsList();

				// if scan is a 2D scan we add Y actuator at the end of actuator
				// list.
				if (res.getResultType() == IScanResult.ResultType.RESULT_2D) {
					final IScanResult2D res2D = (IScanResult2D) res;
					actuatorList.addAll(res2D.getActuatorsYList());
				}

				// create actuator source map :
				// -keys : actuatorName (ex tmp/passerelle/device.1/position)
				// -value : scanServerDataName (exe test/scan/julien/actuator_1_1)
				final Iterator<IActuator> iteratorAct = actuatorList.iterator();
				String sensorActName;
				while (iteratorAct.hasNext()) {
					final IActuator actuator = iteratorAct.next();
					sensorActName = actuator.getName().toLowerCase();
					actuatorsSources.put(sensorActName, actuator.getScanServerAttributeName().toLowerCase());
					logger.debug("add act name " + sensorActName);
				}

				final Iterator<ISensor> iteratorSen = sensorList.iterator();
				while (iteratorSen.hasNext()) {
					final ISensor sensor = iteratorSen.next();
					sensorActName = sensor.getName().toLowerCase();
					sensorsSources.put(sensorActName, sensor.getScanServerAttributeName().toLowerCase());
					logger.debug("add sensor name " + sensorActName);
				}

				// output timestamps
				final TangoAttribute timestamps = new TangoAttribute(res.getSensorsTimeStampsCompleteName()); //ScanUtil.getCurrentContext().getScanServerName() + "/sensorsTimestamps");
				// read attribute is done by the TangoAttribute constructor
				ExecutionTracerService.trace(this, "reading data for sensorTimestamps ");
				response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, timestamps));

				// output sensor and actuators
				final List<Port> orderedActuatorPorts = PortUtilities.getOrderedOutputPorts(this, ACTUATOR, 0);
				final List<Port> orderedSensorPorts = PortUtilities.getOrderedOutputPorts(this, SENSOR, 0);
				int i;
				String scanDataName;
				for (i = 0; i < actuators.length; i++) {
					scanDataName = actuatorsSources.get(actuators[i]);
					if (scanDataName == null) {
						throw new ProcessingExceptionWithLog(this, "Actuator " + actuators[i] + " does not exists on current scan", actuators[i], null);
					}
					final TangoAttribute act = new TangoAttribute(scanDataName);
					// read attribute is done by the TangoAttribute constructor
					ExecutionTracerService.trace(this, "reading data for actuator " + actuators[i]);
					response.addOutputMessage(i,orderedActuatorPorts.get(i), PasserelleUtil.createContentMessage(this, act));
				}

				for (i = 0; i < sensors.length; i++) {
					scanDataName = sensorsSources.get(sensors[i]);
					if (scanDataName == null) {
						throw new ProcessingExceptionWithLog(this, "Sensor " + sensors[i] + " does not exists on current scan", sensors[i], null);
					}

					final TangoAttribute sensor = new TangoAttribute(scanDataName);
					// read attribute is done by the TangoAttribute constructor
					ExecutionTracerService.trace(this, "reading data for sensor " + sensors[i]);
					response.addOutputMessage(i,orderedSensorPorts.get(i), PasserelleUtil.createContentMessage(this, sensor));
				}
			} catch (final DevFailed e) {
				throw new DevFailedProcessingException(e, this);

			} catch (final SalsaDeviceException e) {
				throw new ProcessingException(e.getMessage(), this, e);

			} catch (final SalsaScanConfigurationException e) {
				throw new ProcessingException(e.getMessage(), this, e);
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

	@Override
	protected String getExtendedInfo() {
		return this.getName();
	}

}
