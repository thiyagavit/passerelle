package fr.soleil.passerelle.actor.tango.control;

import java.util.Vector;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.control.motor.MotorMover;
import fr.soleil.passerelle.actor.tango.control.motor.actions.IMoveAction;
import fr.soleil.passerelle.actor.tango.control.motor.actions.MoveNumericAttribute;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

@SuppressWarnings("serial")
public class GenericMirror extends MotorMover {

    public Parameter isBenderLessParam;
    private String isBenderLess = "false";
    private TangoAttribute attIsBenderLessHelper;

    static Vector<String> attributeList = new Vector<String>();
    static {
        attributeList.add("theta");
        attributeList.add("pDistance");
        attributeList.add("qDistance");
        attributeList.add("curvature");
    }

    public GenericMirror(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name, attributeList);

        isBenderLessParam = new Parameter(this, "is Bender Less", new BooleanToken(false));
        isBenderLessParam.setTypeEquals(BaseType.BOOLEAN);
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == isBenderLessParam) {
            isBenderLess = isBenderLessParam.getExpression();
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
                attIsBenderLessHelper = new TangoAttribute(getDeviceName() + "/isBenderLess");
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        if (!isMockMode()) {
            try {
                attIsBenderLessHelper.write(isBenderLess);
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
        // sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());

        super.process(ctxt, request, response);

    }

    @Override
    public IMoveAction createMoveAction() {
        return new MoveNumericAttribute();
    }
}
