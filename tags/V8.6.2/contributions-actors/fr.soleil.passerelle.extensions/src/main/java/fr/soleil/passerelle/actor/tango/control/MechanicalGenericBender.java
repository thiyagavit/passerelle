package fr.soleil.passerelle.actor.tango.control;

import java.util.Vector;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import fr.soleil.passerelle.actor.tango.control.motor.MotorMover;
import fr.soleil.passerelle.actor.tango.control.motor.actions.IMoveAction;
import fr.soleil.passerelle.actor.tango.control.motor.actions.MoveNumericAttribute;

@SuppressWarnings("serial")
public class MechanicalGenericBender extends MotorMover {

    static Vector<String> attributeList = new Vector<String>();
    static {
        attributeList.add("bender");
        attributeList.add("asymmetry");
        attributeList.add("curvature");
        attributeList.add("meanCurvature");
    }

    public MechanicalGenericBender(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name, attributeList);
        // TODO attribut autoSendValues a true en mode non simul�
        // a false en mode simul�

    }

    @Override
    public IMoveAction createMoveAction() {
        return new MoveNumericAttribute();
    }

}