/*
 * Created on 27 avr. 2005
 */
package fr.soleil.passerelle.actor.tango.acquisition;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.data.StringToken;
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
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.ptolemy.ParameterGroup;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * @author ABEILLE
 * 
 *         Actor to perform acquisition on the device ScientaAcquisition. Input:
 *         A trigger. This actor will wait for the trigger before saving object
 *         to file. Ouput 1: an Attribute Proxy to the Attribute sumData
 *         (spectrum). Ouput 2: an Attribute Proxy to the Attribute data
 *         (image). Ouput 3: The context of the acquisition, a String (values of
 *         written Attributes). Other Outputs: the standard ones.
 */
@SuppressWarnings("serial")
public class ScientaAcquisition extends ATangoDeviceActor implements IActorFinalizer {

    private final static Logger logger = LoggerFactory.getLogger(ScientaAcquisition.class);

    public Port sumDataPort;
    public Port dataPort;
    // public Port contextPort;
    // private double excitationEnergy = new Double(0);
    public Parameter modeParam;
    private String mode = "Fixed";
    public Parameter lowEnergyParam;
    private double lowEnergy = 0;
    public Parameter fixEnergyParam;
    private double fixEnergy = 1;
    public Parameter highEnergyParam;
    private double highEnergy = 1;
    public Parameter energyStepParam;
    private double energyStep = 0.1;
    public Parameter stepTimeParam;
    private double stepTime = 1;
    public Parameter lensModeParam;
    private String lensMode = "Transmission";
    public Parameter passEnergyParam;
    private double passEnergy = 2;

    public ParameterGroup detectorParam;
    public ParameterGroup energyParam;
    public ParameterGroup stepParam;

    private WaitStateTask waitTask;

    /**
     * @param arg0
     * @param arg1
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public ScientaAcquisition(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {

        super(arg0, arg1);

        lensModeParam = new StringParameter(this, "Lens Mode");
        lensModeParam.setExpression(lensMode);

        passEnergyParam = new StringParameter(this, "Pass Energy");
        passEnergyParam.setExpression(Double.toString(passEnergy));

        lowEnergyParam = new StringParameter(this, "Low Energy");
        lowEnergyParam.setExpression(Double.toString(lowEnergy));

        fixEnergyParam = new StringParameter(this, "Center Energy");
        fixEnergyParam.setExpression(Double.toString(fixEnergy));

        highEnergyParam = new StringParameter(this, "High Energy");
        highEnergyParam.setExpression(Double.toString(highEnergy));

        energyStepParam = new StringParameter(this, "Energy Step");
        energyStepParam.setExpression(Double.toString(energyStep));

        stepTimeParam = new StringParameter(this, "Step Time");
        stepTimeParam.setExpression(Double.toString(stepTime));

        modeParam = new StringParameter(this, "Mode");
        modeParam.setExpression(mode);
        modeParam.addChoice("Fixed");
        modeParam.addChoice("Swept");

        sumDataPort = PortFactory.getInstance().createOutputPort(this, "sumData (AttributeProxy)");
        sumDataPort.setMultiport(true);
        dataPort = PortFactory.getInstance().createOutputPort(this, "data (AttributeProxy)");
        dataPort.setMultiport(true);

        input.setName("excitationEnergy");
        input.setExpectedMessageContentType(Double.class);
        output.setName("Context (String)");

        final URL url = this.getClass().getResource("/image/BL_SCIENTA.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:blue;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-20\" y=\"-20\" width =\"40\" height=\"40\" xlink:href=\"" + url + "\"/>\n" + "</svg>\n");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute
     * )
     */
    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == modeParam) {
            mode = modeParam.getExpression().trim();
        } else if (attribute == highEnergyParam) {
            highEnergy = Double.valueOf(((StringToken) highEnergyParam.getToken()).stringValue());
        } else if (attribute == fixEnergyParam) {
            fixEnergy = Double.valueOf(((StringToken) fixEnergyParam.getToken()).stringValue());
        } else if (attribute == lowEnergyParam) {
            lowEnergy = Double.valueOf(((StringToken) lowEnergyParam.getToken()).stringValue());
        } else if (attribute == energyStepParam) {
            energyStep = Double.valueOf(((StringToken) energyStepParam.getToken()).stringValue());
        } else if (attribute == stepTimeParam) {
            stepTime = Double.valueOf(((StringToken) stepTimeParam.getToken()).stringValue());
        } else if (attribute == lensModeParam) {
            lensMode = ((StringToken) lensModeParam.getToken()).stringValue();
        } else if (attribute == passEnergyParam) {
            passEnergy = Double.valueOf(((StringToken) passEnergyParam.getToken()).stringValue());
        } else {
            super.attributeChanged(attribute);
        }

    }

    @Override
    protected void doInitialize() throws InitializationException {
        final Director dir = getDirector();
        if (dir instanceof BasicDirector) {
            ((BasicDirector) dir).registerFinalizer(this);
        }
        super.doInitialize();
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        // final ManagedMessage message = request.getMessage(input);

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - entry");
        }
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - Starting acquisition");
            ExecutionTracerService.trace(this, "MOCK - lowEnergy " + lowEnergy);
            ExecutionTracerService.trace(this, "MOCK - fixEnergy " + fixEnergy);
            ExecutionTracerService.trace(this, "MOCK - highEnergy " + highEnergy);
            ExecutionTracerService.trace(this, "MOCK - Acquisition finished");
            response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, "context"));

            final Double[] value = { 1.0, 2.0 };

            response.addOutputMessage(1, sumDataPort, PasserelleUtil.createContentMessage(this, value));

            final Double[] value2 = { 4.0, 5.0, 6.0, 7.0 };

            response.addOutputMessage(2, dataPort, PasserelleUtil.createContentMessage(this, value2));

        } else {
            try {
                // ==========get excitation energy====================
                // excitationEnergy = (Double)
                // PasserelleUtil.getInputValue(message);

                // ================write all attributes=======================
                final DeviceProxy dev = getDeviceProxy();
                // System.out.println("scienta mode: "+ mode);
                DeviceAttribute da = new DeviceAttribute("mode");
                da.insert(mode);
                dev.write_attribute(da);

                // not functional for the last scienta device
                // da = new DeviceAttribute("excitationEnergy");
                // da.insert(excitationEnergy.doubleValue());
                // dev.write_attribute(da);

                double lowEn;

                DeviceAttribute high = new DeviceAttribute("highEnergy");
                high = dev.read_attribute("highEnergy");
                high.extractDouble();
                DeviceAttribute low = new DeviceAttribute("lowEnergy");
                low = dev.read_attribute("highEnergy");
                lowEn = low.extractDouble();

                if (lowEn > highEnergy) {
                    // write low before high
                    da = new DeviceAttribute("lowEnergy");
                    da.insert(lowEnergy);
                    dev.write_attribute(da);

                    da = new DeviceAttribute("highEnergy");
                    da.insert(highEnergy);
                    dev.write_attribute(da);
                } else {
                    // write high energy before low
                    da = new DeviceAttribute("highEnergy");
                    da.insert(highEnergy);
                    dev.write_attribute(da);

                    da = new DeviceAttribute("lowEnergy");
                    da.insert(lowEnergy);
                    dev.write_attribute(da);
                }

                da = new DeviceAttribute("fixEnergy");
                da.insert(fixEnergy);
                dev.write_attribute(da);

                da = new DeviceAttribute("energyStep");
                da.insert(energyStep);
                dev.write_attribute(da);

                da = new DeviceAttribute("stepTime");
                da.insert(stepTime);
                dev.write_attribute(da);

                da = new DeviceAttribute("lensMode");
                da.insert(lensMode);
                dev.write_attribute(da);

                da = new DeviceAttribute("passEnergy");
                da.insert(passEnergy);
                dev.write_attribute(da);

                // ==================start acquisition========================
                ExecutionTracerService.trace(this, "Starting acquisition");

                final String deviceName = getDeviceName();
                dev.command_inout("Start");
                waitTask = new WaitStateTask(deviceName, DevState.STANDBY, 1000, true);
                waitTask.run();
                if (waitTask.hasFailed()) {
                    throw waitTask.getDevFailed();
                }

                ExecutionTracerService.trace(this, "Acquisition finished");
                // ============read result of acquisition====================
                if (isRecordData()) {
                    DataRecorder.getInstance().saveDevice(this, deviceName);
                    DataRecorder.getInstance().saveExperimentalData(this, deviceName);
                }

                // TODO: this output shall be removed
                response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());

                final TangoAttribute sumDataProxy = new TangoAttribute(deviceName + "/sumData");

                // read attribute is done by the TangoAttribute constructor
                response.addOutputMessage(1, sumDataPort, PasserelleUtil.createContentMessage(this, sumDataProxy));

                final TangoAttribute dataProxy = new TangoAttribute(deviceName + "/data");

                // read attribute is done by the TangoAttribute constructor
                response.addOutputMessage(2, dataPort, PasserelleUtil.createContentMessage(this, dataProxy));

            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            } catch (final PasserelleException e) {
                ExceptionUtil.throwProcessingException("Passerelle Exception", this, e);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - exit");
        }
    }

    @Override
    protected void doStop() {
        if (waitTask != null) {
            waitTask.cancel();
        }
        super.doStop();
    }

    public void doFinalAction() {
        if (!isMockMode()) {
            try {
                // bug 22954
                /*
                 * final String deviceName = getDeviceName(); final TangoCommand
                 * cmd = new TangoCommand(deviceName, "State");
                 * 
                 * final DevState currentState = (DevState)
                 * cmd.executeExtract(null); if
                 * (currentState.equals(DevState.RUNNING)) { new
                 * TangoCommand(deviceName, "Stop").execute();
                 * ExecutionTracerService.trace(this,
                 * "scienta acquisition has been stop"); }
                 */
                if (TangoAccess.executeCmdAccordingState(getDeviceName(), DevState.RUNNING, "Stop")) {
                    ExecutionTracerService.trace(this, "scienta acquisition has been stop");
                }

            } catch (final DevFailed e) {
                TangoToPasserelleUtil.getDevFailedString(e, this);
            } catch (final Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
     */
    @Override
    protected String getExtendedInfo() {
        return this.getName() + " for making acquisition on " + getDeviceName();
    }
}
