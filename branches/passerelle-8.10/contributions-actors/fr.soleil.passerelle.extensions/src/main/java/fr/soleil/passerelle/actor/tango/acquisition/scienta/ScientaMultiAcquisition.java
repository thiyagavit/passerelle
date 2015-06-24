package fr.soleil.passerelle.actor.tango.acquisition.scienta;

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
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class ScientaMultiAcquisition extends ATangoDeviceActor {

    public Parameter regionNameParam;
    private String[] regionNames;
    public Parameter lensModeParam;
    private String[] lensModes;
    public Parameter passEnergyParam;
    public Parameter acqModeParam;
    private String[] acqModes;
    public Parameter kinBinParam;
    private String[] kinBin;
    public Parameter lowParam;
    private String[] low;
    public Parameter fixParam;
    private String[] fix;
    public Parameter highParam;
    private String[] high;
    public Parameter energyStepParam;
    private String[] energySteps;
    public Parameter stepTimeParam;
    private String[] stepTimes;
    public Parameter sweepNumberParam;
    private String[] sweepNumber;
    public Parameter isActiveParam;
    private String[] isActive;

    private final ScientaRegionsManager manager;
    public Port doNextPort;
    public Port endSingleAcqPort;
    private boolean doNextPortExhausted = false;
    private boolean firstTurn = false;
    private boolean firstExecution = true;

    private final String symbolName = "";

    // /** The input ports */
    // public Port inputExcitationEnergy;
    //
    // /** The output ports */
    // public Port outputEnd;

    public ScientaMultiAcquisition(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        // inputExcitationEnergy =
        // PortFactory.getInstance().createInputPort(this, "ExcitationEnergy",
        // null);
        // outputEnd =
        // PortFactory.getInstance().createOutputPort(this,"Context (End)");

        manager = new ScientaRegionsManager();

        isActiveParam = new StringParameter(this, "is active list");
        regionNameParam = new StringParameter(this, "region names list");
        lensModeParam = new StringParameter(this, "lens modes list");
        passEnergyParam = new StringParameter(this, "pass modes list");
        kinBinParam = new StringParameter(this, "kinetic/binding list");
        lowParam = new StringParameter(this, "low energies list");
        fixParam = new StringParameter(this, "fix energies list");
        highParam = new StringParameter(this, "high energies list");
        energyStepParam = new StringParameter(this, "energy steps list");
        stepTimeParam = new StringParameter(this, "step times list");
        acqModeParam = new StringParameter(this, "acq mode list");
        sweepNumberParam = new StringParameter(this, "sweep nr list");

        // input.setExpectedMessageContentType(Double.class);
        // input.setName("ExcitationEnergy");

        doNextPort = PortFactory.getInstance().createInputPort(this, "doNext", null);
        // output.setName("End");

        endSingleAcqPort = PortFactory.getInstance().createOutputPort(this, "EndSingleAcquisition");
    }

    @Override
    public void attributeChanged(final Attribute param) throws IllegalActionException {
        if (param == isActiveParam) {
            isActive = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == regionNameParam) {
            regionNames = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == lensModeParam) {
            lensModes = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == passEnergyParam) {
            // passEnergies = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == kinBinParam) {
            kinBin = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == lowParam) {
            low = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == fixParam) {
            fix = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == highParam) {
            high = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == energyStepParam) {
            energySteps = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == stepTimeParam) {
            stepTimes = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == acqModeParam) {
            acqModes = PasserelleUtil.getParameterValue(param).split(",");
        } else if (param == sweepNumberParam) {
            sweepNumber = PasserelleUtil.getParameterValue(param).split(",");
        } else {
            super.attributeChanged(param);
        }
    }

    @Override
    protected void doInitialize() throws InitializationException {

        manager.setDeviceName(getDeviceName());

        // TODO: checks sizes
        final int nbRegions = isActive.length;
        for (int i = 0; i < nbRegions; i++) {
            final ScientaRegion region = new ScientaRegion();
            region.setActive(isActive[i]);
            region.setLensMode(lensModes[i]);
            region.setAcqMode(acqModes[i]);
            region.setEnergyStep(energySteps[i]);
            region.setFix(fix[i]);
            region.setHigh(high[i]);
            region.setKinBin(kinBin[i]);
            region.setLensMode(lensModes[i]);
            region.setLow(low[i]);
            region.setNumberSweeps(sweepNumber[i]);
            region.setStepTime(stepTimes[i]);
            region.setRegionName(regionNames[i]);
            manager.addScientaRegion(region);

        }
        firstExecution = true;
        doNextPortExhausted = !(doNextPort.getWidth() > 0);
        manager.intializeMulti();

        if (!isMockMode()) {
            try {
                manager.initialize();
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        final ManagedMessage message = request.getMessage(input);

        boolean continu = true;
        firstTurn = true;
        final double excitationEnergy = (Double) PasserelleUtil.getInputValue(message);
        manager.setExcitationEnergy(excitationEnergy);
        if (firstExecution) {
            manager.setInitialExcitationEnergy(excitationEnergy);
            firstExecution = false;
        }
        if (doNextPort.getWidth() > 0) {
            while (!doNextPortExhausted && continu) {
                try {
                    if (firstTurn) {
                        continu = this.doAcquisition();
                        if (manager.isActive()) {
                            // sendOutputMsg(endSingleAcqPort, PasserelleUtil
                            // .createTriggerMessage());
                            response.addOutputMessage(0, endSingleAcqPort, PasserelleUtil.createTriggerMessage());
                            firstTurn = false;
                        }
                    } else {
                        final ManagedMessage msg = MessageHelper.getMessage(doNextPort);
                        if (msg != null) {
                            continu = this.doAcquisition();
                            if (manager.isActive() && continu) {
                                // sendOutputMsg(endSingleAcqPort,
                                // PasserelleUtil
                                // .createTriggerMessage());
                                response.addOutputMessage(0, endSingleAcqPort, PasserelleUtil.createTriggerMessage());
                            }
                        } else {
                            doNextPortExhausted = true;
                        }
                    }
                } catch (final PasserelleException e) {
                    ExceptionUtil.throwProcessingException("impossible to get doNext input", doNextPort, e);
                }
            }
        } else {
            while (continu) {
                continu = this.doAcquisition();
            }
        }
        // sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
        response.addOutputMessage(1, output, PasserelleUtil.createTriggerMessage());
    }

    private boolean doAcquisition() throws ProcessingException {
        boolean continu = false;
        try {
            if (isMockMode()) {
                continu = manager.configureMulti(true);
                if (manager.isActive() && continu) {
                    ExecutionTracerService.trace(this, "MOCK - Starting Acquisition for config: "
                            + manager.getCurrentScientaRegion().getRegionName());
                    ExecutionTracerService.trace(this, "MOCK - Acquisition Finished");
                }
            } else {
                continu = manager.configureMulti(false);
                if (manager.isActive() && continu) {
                    ExecutionTracerService.trace(this, "Starting Acquisition for config: "
                            + manager.getCurrentScientaRegion().getRegionName());
                    if (manager.getCurrentScientaRegion().getAcqMode().compareToIgnoreCase("Swept") == 0) {
                        ExecutionTracerService.trace(this, "\t\t - low energy "
                                + manager.getCurrentScientaRegion().getLow());
                        ExecutionTracerService.trace(this, "\t\t - high energy "
                                + manager.getCurrentScientaRegion().getHigh());
                    } else {
                        ExecutionTracerService.trace(this, "\t\t - fix energy "
                                + manager.getCurrentScientaRegion().getFix());
                    }
                    manager.doAcquisition(this, symbolName);
                    ExecutionTracerService.trace(this, "Acquisition Finished");
                }
            }
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        } catch (final IllegalActionException e) {
            ExceptionUtil.throwProcessingException("impossible to record data", this, e);
        }
        return continu;
    }

}
