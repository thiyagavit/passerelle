package fr.soleil.passerelle.actor.tango.acquisition;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

/**
 * Set a single ROI on Rontec detector
 */
@SuppressWarnings("serial")
public class SetSingleROI extends ATangoDeviceActor {

    /**
     * The ROI index
     */
    public Parameter roiIndexParam;
    private double roiIndex;

    /**
     * The start ROI value
     */
    public Parameter startROIParam;
    private double startROI;

    /**
     * The end ROI value
     */
    public Parameter endROIParam;
    private double endROI;

    private TangoCommand setSingleROI;

    // /** The output ports */
    // public Port output;

    public SetSingleROI(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        // output = PortFactory.getInstance().createOutputPort(this,"end");

        roiIndexParam = new StringParameter(this, "ROI index");
        roiIndexParam.setExpression("1");

        startROIParam = new StringParameter(this, "ROI start");
        startROIParam.setExpression("0");

        endROIParam = new StringParameter(this, "ROI end");
        endROIParam.setExpression("10");

        recordDataParam.setVisibility(Settable.EXPERT);

    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
                setSingleROI = new TangoCommand(getDeviceName(), "SetSingleROI");
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        if (isMockMode()) {
            ExecutionTracerService
                    .trace(this, "MOCK - Setting ROI " + roiIndex + " from " + startROI + " to " + endROI);
        } else {
            try {
                ExecutionTracerService.trace(this, "Setting ROI " + roiIndex + " from " + startROI + " to " + endROI);
                setSingleROI.execute(Double.toString(roiIndex), Double.toString(startROI), Double.toString(endROI));
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        // sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());

    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == roiIndexParam) {
            roiIndex = PasserelleUtil.getParameterDoubleValue(roiIndexParam);
        } else if (arg0 == startROIParam) {
            startROI = PasserelleUtil.getParameterDoubleValue(startROIParam);
        } else if (arg0 == endROIParam) {
            endROI = PasserelleUtil.getParameterDoubleValue(endROIParam);
        } else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
