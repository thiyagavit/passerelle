package fr.soleil.passerelle.actor.tango.acquisition.ccd;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
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
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

@SuppressWarnings("serial")
public class RecordAviex extends ATangoDeviceActor {

    private final static Logger logger = LoggerFactory.getLogger(RecordAviex.class);

    public Parameter flagNameParam;
    String flagName = "";
    public Parameter flagValueParam;
    String flagValue = "";
    TangoAttribute attrFlag;
    TangoAttribute framesAttr;
    TangoAttribute selectFrameAttr;
    double flag = 0;
    double expectedFlag = 0;

    // /** The output ports */
    // public Port output;

    public RecordAviex(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);

        // output = PortFactory.getInstance().createOutputPort(this,"output");

        flagNameParam = new StringParameter(this, "Flag Attribute Name");
        flagNameParam.setExpression(flagName);

        flagValueParam = new StringParameter(this, "Flag valid value");
        flagValueParam.setExpression(flagValue);

        recordDataParam.setVisibility(Settable.EXPERT);

        final URL url = this.getClass().getResource(
                "/org/tango-project/tango-icon-theme/32x32/categories/applications-multimedia.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:gray;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\"" + url + "\"/>\n" + "</svg>\n");
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
                attrFlag = new TangoAttribute(flagName);
                framesAttr = new TangoAttribute(getDeviceName() + "/frames");
                selectFrameAttr = new TangoAttribute(getDeviceName() + "/selectFrame");
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
            if (flagValue.compareToIgnoreCase("false") == 0) {
                expectedFlag = 0;
            } else if (flagValue.compareToIgnoreCase("true") == 0) {
                expectedFlag = 1;
            } else {
                try {
                    expectedFlag = Double.valueOf(flagValue);
                } catch (final NumberFormatException e) {
                    ExceptionUtil.throwInitializationException("Flag must be numerical", flagValue, e);
                }
            }
        }
        super.doInitialize();
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - Starting recording Aviex data");
            ExecutionTracerService.trace(this, "MOCK - Aviex data has been saved");
        } else {

            try {
                ExecutionTracerService.trace(this, "Waiting flag for recording");
                logger.debug("expected value " + expectedFlag);
                do {
                    flag = attrFlag.read(Double.class);
                    logger.debug("current value " + flag);
                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        // ignore
                    }
                } while (expectedFlag != flag && !isFinishRequested());
                ExecutionTracerService.trace(this, "Starting recording Aviex data");
                // save context data
                DataRecorder.getInstance().saveDevice(this, getDeviceName());
                final int nbFrames = framesAttr.read(Integer.class);
                logger.debug("nbFrames " + nbFrames);
                for (int i = 0; i < nbFrames; i++) {
                    selectFrameAttr.write(i);
                    logger.debug("saving " + i);
                    DataRecorder.getInstance().saveExperimentalData(this, getDeviceName());
                    if (isFinishRequested()) {
                        break;
                    }
                }
                ExecutionTracerService.trace(this, "Aviex data has been saved");
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        // sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == flagNameParam) {
            flagName = ((StringToken) flagNameParam.getToken()).stringValue();
        } else if (attribute == flagValueParam) {
            flagValue = ((StringToken) flagValueParam.getToken()).stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }
}
