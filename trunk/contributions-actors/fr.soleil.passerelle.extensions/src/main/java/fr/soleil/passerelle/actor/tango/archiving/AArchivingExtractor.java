package fr.soleil.passerelle.actor.tango.archiving;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.InitializationException;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoCommandActor;
import fr.soleil.passerelle.util.DevFailedInitializationException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.util.TangoUtil;

@SuppressWarnings("serial")
public abstract class AArchivingExtractor extends ATangoCommandActor {

    public Parameter attributeNameParam;
    private String attributeName;
    private String[] attributeNames;

    public AArchivingExtractor(final CompositeEntity container, final String name) throws NameDuplicationException,
	    IllegalActionException {
	super(container, name);
	input.setName("Trigger");
	output.setName("archived value");
	attributeNameParam = new StringParameter(this, "Attribute to extract");
	attributeNameParam.setExpression("name");
	new TextStyle(attributeNameParam, "paramsTextArea");
	recordDataParam.setVisibility(Settable.EXPERT);
	commandNameParam.setVisibility(Settable.EXPERT);
	final URL url = this.getClass().getResource("/fr/soleil/datalogging.png");
	_attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
		+ "height=\"40\" style=\"fill:orange;stroke:black\"/>\n"
		+ "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
		+ "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
		+ "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
		+ "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
		+ "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
		+ "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
		+ " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\"" + url + "\"/>\n" + "</svg>\n");
    }

    @Override
    public void attributeChanged(final Attribute attr) throws IllegalActionException {
	if (attr == attributeNameParam) {
	    attributeName = PasserelleUtil.getParameterValue(attributeNameParam);

	} else {
	    super.attributeChanged(attr);
	}
    }

    // public String getAttributeName() {
    // return attributeName;
    // }

    public String[] getAttributeNames() {
	return attributeNames;
    }

    @Override
    protected void doInitialize() throws InitializationException {
	if (isMockMode()) {
	    attributeNames = new String[] { "mock1", "mock2" };
	} else {
	    try {
		final List<String> list = new ArrayList<String>();
		final String[] tempAttr = attributeName.split("\n");
		for (final String attr : tempAttr) {
		    if (!attr.startsWith("#") && !attr.isEmpty()) {
			final String attrOnly = TangoUtil.getAttributeName(attr);
			final String devicePattern = TangoUtil.getfullDeviceNameForAttribute(attr);
			final String[] deviceNames = TangoUtil.getDevicesForPattern(devicePattern);
			for (final String string : deviceNames) {
			    list.add(string + "/" + attrOnly);
			}
		    }
		}
		attributeNames = list.toArray(new String[list.size()]);
	    } catch (final DevFailed e) {
		throw new DevFailedInitializationException(e, this);
	    }
	}
	super.doInitialize();
    }
}
