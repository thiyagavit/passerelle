package fr.soleil.passerelle.actor.tango.control.motor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.actor.tango.control.motor.actions.IMoveAction;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * An base class actor that is able to move all equipments to a wanted position
 * 
 * @author ABEILLE
 * 
 */
@SuppressWarnings("serial")
public abstract class MotorMover extends ATangoDeviceActor {

    private final static Logger logger = LoggerFactory.getLogger(MotorMover.class);
    private static final String MOUVEMENT_TYPE = "Mouvement type";
    private final List<String> outputList;
    private List<TangoAttribute> attrList;

    /**
     * The wanted mouvement
     */
    @ParameterName(name = MOUVEMENT_TYPE)
    public Parameter mouvementTypeParam;
    protected String mouvementType;

    private IMoveAction action;

    public MotorMover(final CompositeEntity container, final String name, final List<String> outputList)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setExpectedMessageContentType(String.class);
        attrList = new ArrayList<TangoAttribute>();
        this.outputList = outputList;

        input.setName("wanted position");
        output.setName(outputList.get(0));
        for (int i = 1; i < outputList.size(); i++) {
            PortFactory.getInstance().createOutputPort(this, outputList.get(i));
        }

        mouvementTypeParam = new StringParameter(this, MOUVEMENT_TYPE);
        mouvementTypeParam.setExpression(mouvementType);

        action = createMoveAction();
    }

    public abstract IMoveAction createMoveAction();

    @Override
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
                action.setDeviceName(getDeviceName());
                action.setActionName(mouvementType);
                action.init();
                attrList.clear();
                for (int i = 0; i < outputList.size(); i++) {
                    final TangoAttribute attHelper = new TangoAttribute(getDeviceName() + "/" + outputList.get(i));
                    attrList.add(attHelper);
                }
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

        final String desiredPosition = (String) PasserelleUtil.getInputValue(message);
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - Moving " + mouvementType + " to " + desiredPosition);
            ExecutionTracerService.trace(this, "MOCK - " + mouvementType + " has been to " + desiredPosition);
            final List<Port> outputPortList = outputPortList();
            int i = 0;
            for (final Port port : outputPortList) {
                // FIXME: find another way to retrieve default ports
                if (!port.equals(errorPort) && !port.getName().equals("hasFired")
                        && !port.getName().equals("hasFinished")) {
                    logger.debug("MOCK output " + " on " + port.getFullName());
                    response.addOutputMessage(0, port, PasserelleUtil.createContentMessage(this, desiredPosition));
                }
                i++;
            }
        } else {
            try {
                ExecutionTracerService.trace(this, "Moving " + getDeviceName() + " to " + desiredPosition);

                action.setDesiredPosition(desiredPosition);
                action.move();
                action.waitEndMouvement();
                ExecutionTracerService.trace(this, getDeviceName() + " " + action.getStatus());
                if (isRecordData()) {
                    DataRecorder.getInstance().saveDevice(this, getDeviceName());
                }

                final List<Port> outputPortList = outputPortList();
                int i = 0;
                for (final Port port : outputPortList) {
                    // FIXME: find another way to retrieve default ports
                    if (!port.getName().equals(errorPort.getName()) && !port.getName().equals("hasFired")
                            && !port.getName().equals("hasFinished")) {
                        final TangoAttribute attHelper = attrList.get(i);
                        attHelper.update();
                        logger.debug("output " + attHelper.getAttributeProxy().fullName() + " on " + port.getFullName());
                        response.addOutputMessage(0, port, PasserelleUtil.createContentMessage(this, attHelper));
                        i++;
                    }
                }
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
    }

    @Override
    protected void doStop() {
        action.cancelWaitEnd();
        super.doStop();
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == mouvementTypeParam) {
            mouvementType = ((StringToken) mouvementTypeParam.getToken()).stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMouvementType() {
        return mouvementType;
    }

    @Override
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
        final MotorMover copy = (MotorMover) super.clone(workspace);
        copy.attrList = new ArrayList<TangoAttribute>();
        copy.action = createMoveAction();
        return copy;
    }

}
