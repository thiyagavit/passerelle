package fr.soleil.passerelle.actor.tango.control;

import java.util.Vector;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import fr.soleil.passerelle.actor.tango.control.motor.MotorMover;
import fr.soleil.passerelle.actor.tango.control.motor.actions.IMoveAction;
import fr.soleil.passerelle.actor.tango.control.motor.actions.MoveNumericAttribute;

@SuppressWarnings("serial")
public class DiffractometerKappa6C extends MotorMover {

    static Vector<String> attributeList = new Vector<String>();
    static {
        attributeList.add("mu");
        attributeList.add("komega");
        attributeList.add("kappa");
        attributeList.add("kphi");
        attributeList.add("gamma");
        attributeList.add("delta");
    }

    public DiffractometerKappa6C(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name, attributeList);
    }

    @Override
    public IMoveAction createMoveAction() {
        return new MoveNumericAttribute();
    }
}
