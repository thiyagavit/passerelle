package fr.soleil.passerelle.actor.tango.acquisition.ccd;

import java.util.LinkedHashMap;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class AcquisitionPrincetonOde extends CCDAcquisitionPerformer {
    static LinkedHashMap<String, Integer> acqModeMap = new LinkedHashMap<String, Integer>();
    static {
        acqModeMap.put("OneShot", 0);
        acqModeMap.put("MultiFrame", 2);
        acqModeMap.put("Intensity0", 3);
        acqModeMap.put("IntensityBlack", 4);
        acqModeMap.put("AverageIntensity0", 5);
    }

    public AcquisitionPrincetonOde(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1, acqModeMap);
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        if (isMockMode()) {
            // super.doFire(arg0);
            super.process(ctxt, request, response);
        } else {
            final CCDConfiguration config = ccd.getConfig();
            config.setAcqModeMap(acqModeMap);
            config.setAcqMode(acqMode);
            config.setRecordAllSequence(recordAllSequence);

            try {
                switch (AcquisitionPrincetonOde.acqModeMap.get(ccd.getConfig().getAcqMode())) {
                    case 0:
                    case 2:

                        // super.doFire(arg0);
                        super.process(ctxt, request, response);
                        break;
                    case 3:
                        this.doIntensity0Acquisition();
                        response.addOutputMessage(output, PasserelleUtil.createTriggerMessage());
                        break;
                    case 4:
                        this.doIntensityBlackAcquisition();
                        response.addOutputMessage(output, PasserelleUtil.createTriggerMessage());
                        break;
                    case 5:
                        ccd.getConfig().setAcqMode("MultiFrame");
                        this.doAverageIntensity0Acquisition();
                        response.addOutputMessage(output, PasserelleUtil.createTriggerMessage());
                        break;
                }
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            } catch (final IllegalActionException e) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, "error saving data", this, e);
            }

        }
    }

    private void doIntensity0Acquisition() throws DevFailed {
        ExecutionTracerService.trace(this, "Acquire I0");
        final TangoCommand cmd = new TangoCommand(ccd.getDeviceName(), "AcquireIntensity0");
        cmd.execute();
    }

    private void doAverageIntensity0Acquisition() throws DevFailed, IllegalActionException {
        ExecutionTracerService.trace(this, "Acquire average I0");
        ccd.doStandardAcquisitionWithoutStore();
        final TangoCommand cmd = new TangoCommand(ccd.getDeviceName(), "SetAverageIntensity0");
        cmd.execute();
    }

    private void doIntensityBlackAcquisition() throws DevFailed {
        ExecutionTracerService.trace(this, "Acquire IBlack");
        final TangoCommand cmd = new TangoCommand(ccd.getDeviceName(), "AcquireIntensityBlack");
        cmd.execute();

    }
}
