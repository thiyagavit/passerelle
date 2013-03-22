package fr.soleil.passerelle.actor.tango.control;

import java.net.URL;
import java.util.Vector;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.tango.control.motor.MotorMover;
import fr.soleil.passerelle.actor.tango.control.motor.actions.IMoveAction;
import fr.soleil.passerelle.actor.tango.control.motor.actions.MoveNumericAttribute;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

@SuppressWarnings("serial")
public class SampleMover extends MotorMover implements IActorFinalizer {

    static Vector<String> attributeList = new Vector<String>();
    static {
        attributeList.add("position");
    }

    public SampleMover(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name, attributeList);
        mouvementTypeParam.setVisibility(Settable.EXPERT);
        mouvementTypeParam.addChoice("position");
        mouvementTypeParam.setExpression("position");
        final URL url = this.getClass().getResource("/image/Sample.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:cyan;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-20\" y=\"-20\" width =\"40\" height=\"40\" xlink:href=\"" + url
                + "\"/>\n" + "</svg>\n");
    }

    @Override
    public IMoveAction createMoveAction() {
        final IMoveAction action = new MoveNumericAttribute() {
            @Override
            public String getStatus() throws DevFailed {

                final TangoAttribute x = new TangoAttribute(deviceName + "/position");
                final String status = "Sample has been to " + x.readAsString("", "");
                return status;
            }
        };

        return action;
    }

    @Override
    public void doFinalAction() {
        if (!isMockMode()) {
            try {
                // bug 22954
                if (TangoAccess.executeCmdAccordingState(getDeviceName(), DevState.MOVING, "Stop")) {
                    ExecutionTracerService.trace(this, "motor has been stop");
                }

            }
            catch (final DevFailed e) {
                TangoToPasserelleUtil.getDevFailedString(e, this);
            }
            catch (final Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
