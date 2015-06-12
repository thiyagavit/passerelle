package fr.soleil.passerelle.actor;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * 
 * @author ADIOUF
 * 
 */
@SuppressWarnings("serial")
public abstract class TransformerV3 extends ActorV3 {

    public Port input;
    public Port output;

    /**
     * Construct an actor with the given container and name.
     * 
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     *                by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *                actor with this name.
     */
    public TransformerV3(CompositeEntity container, String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        input = PortFactory.getInstance().createInputPort(this, null);
        output = PortFactory.getInstance().createOutputPort(this);

        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<circle cx=\"0\" cy=\"0\" r=\"10\"" + "style=\"fill:white;stroke-width:2.0\"/>\n"
                + "<line x1=\"-15\" y1=\"0\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n"
                + "<line x1=\"12\" y1=\"-3\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n"
                + "<line x1=\"12\" y1=\"3\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
    }

    /*
     * (non-Javadoc)
     * @see com.isencia.passerelle.actor.Actor#getAuditTrailMessage(com.isencia.passerelle.message.ManagedMessage, com.isencia.passerelle.core.Port)
     */
    protected String getAuditTrailMessage(ManagedMessage message, Port port) {
        return " sent converted message";
    }

    @Override
    protected void doInitialize() throws InitializationException {
        super.doInitialize();
    }

}
