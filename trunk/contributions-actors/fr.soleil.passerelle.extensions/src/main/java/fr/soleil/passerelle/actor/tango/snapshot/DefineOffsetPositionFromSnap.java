package fr.soleil.passerelle.actor.tango.snapshot;

import java.net.URL;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.control.motor.AGalilMotorActor;
import fr.soleil.passerelle.actor.tango.control.motor.actions.IMoveAction;
import fr.soleil.passerelle.actor.tango.control.motor.actions.MoveNumericAttribute;
import fr.soleil.passerelle.actor.tango.control.motor.dataProviders.AttributeDataProvider;
import fr.soleil.passerelle.actor.tango.control.motor.dataProviders.SnapAttributeDataProvider;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.tango.clientapi.TangoCommand;

//TODO check param value
//TODO deal with alias
//TODO add logs

public class DefineOffsetPositionFromSnap extends AGalilMotorActor {

    private WaitStateTask waitTask;

    public DefineOffsetPositionFromSnap(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        final URL url = this.getClass().getResource(
                "/org/tango-project/tango-icon-theme/32x32/devices/camera-photo.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:orange;stroke:black\"/>\n"
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
                + " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\"" + url
                + "\"/>\n" + "</svg>\n");
    }

    @Override
    protected void doStop() {
        if (waitTask != null) {
            try {
                new TangoCommand(getDeviceName(), "Stop").execute();
            }
            catch (DevFailed devFailed) {
                ExecutionTracerService.trace(this, devFailed);
            }
            waitTask.cancel();
        }
        super.doStop();
    }

    @Override
    public AttributeDataProvider createOffsetDataProvider() {
        return new SnapAttributeDataProvider("offset", false);
    }

    @Override
    public IMoveAction createMoveAction() {
        return new MoveNumericAttribute();
    }

    @Override
    public AttributeDataProvider createPositionDataProvider() {
        return new SnapAttributeDataProvider("position", true);
    }
}
