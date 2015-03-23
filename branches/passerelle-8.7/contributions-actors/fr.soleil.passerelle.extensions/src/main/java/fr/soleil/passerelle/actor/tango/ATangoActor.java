package fr.soleil.passerelle.actor.tango;

import java.net.URL;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.doc.generator.ParameterName;

import fr.soleil.passerelle.actor.ActorV3;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public abstract class ATangoActor extends ActorV3 {

    private static final String RECORD_DATA = "Record data";

    public Port input;
    public Port output;

    /**
     * Save the device with the DataRecorder
     */
    @ParameterName(name = RECORD_DATA)
    public Parameter recordDataParam;
    private boolean recordData;

    public ATangoActor(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        input = PortFactory.getInstance().createInputPort(this, null);
        output = PortFactory.getInstance().createOutputPort(this);

        recordDataParam = new Parameter(this, RECORD_DATA, new BooleanToken(false));
        recordDataParam.setTypeEquals(BaseType.BOOLEAN);

        final URL url = this.getClass().getResource("/fr/soleil/tango/tango.jpg");
        _attachText("_iconDescription", "<svg>\n"
                + " <image x=\"-20\" y=\"-20\" width =\"40\" height=\"40\" xlink:href=\"" + url + "\"/>\n"
                + "<line x1=\"-20\" y1=\"20\" x2=\"-20\" y2=\"-20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-20\" y1=\"-20\" x2=\"20\" y2=\"-20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"19\" x2=\"-19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"18\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-20\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-19\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "</svg>\n");
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == recordDataParam) {
            recordData = PasserelleUtil.getParameterBooleanValue(recordDataParam);
        } else {
            super.attributeChanged(arg0);
        }
    }

    /**
     * 
     * @return true if device is recorded with DataRecorder
     */
    public boolean isRecordData() {
        return recordData;
    }

}
