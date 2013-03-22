package fr.soleil.passerelle.actor.tango.control;

import java.util.Vector;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import fr.soleil.passerelle.actor.tango.control.motor.MotorMover;
import fr.soleil.passerelle.actor.tango.control.motor.actions.IMoveAction;
import fr.soleil.passerelle.actor.tango.control.motor.actions.MoveNumericAttribute;

@SuppressWarnings("serial")
public class TraitPointPlan extends MotorMover {

    static Vector<String> attributeList = new Vector<String>();
    static {
        attributeList.add("pitch");
        attributeList.add("roll");
        attributeList.add("zC");
    }

    public TraitPointPlan(CompositeEntity container, String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name, attributeList);
    }

    @Override
    public IMoveAction createMoveAction() {
        return new MoveNumericAttribute();
    }
}
