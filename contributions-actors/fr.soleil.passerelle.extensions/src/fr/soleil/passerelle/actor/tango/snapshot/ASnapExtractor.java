package fr.soleil.passerelle.actor.tango.snapshot;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.DevFailedInitializationException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.util.SoleilUtilities;

@SuppressWarnings("serial")
public abstract class ASnapExtractor extends Transformer {

	private final static Logger logger = LoggerFactory.getLogger(ASnapExtractor.class);
	protected TangoCommand getSnapValue;

	public Parameter attributeNameParam;
	protected String attributeName;

	public Port writePort;

	private String snapID;
	private String snapExtractorName;

	public ASnapExtractor(final CompositeEntity container, final String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		attributeNameParam = new StringParameter(this, "Attribute to extract");
		attributeNameParam.setExpression("name");

		output.setName("read value");

		writePort = PortFactory.getInstance().createOutputPort(this,
				"write value");

		final URL url = this
				.getClass()
				.getResource(
						"/org/tango-project/tango-icon-theme/32x32/devices/camera-photo.png");
		_attachText(
				"_iconDescription",
				"<svg>\n"
						+ "<rect x=\"-20\" y=\"-20\" width=\"40\" "
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
						+ " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\""
						+ url + "\"/>\n" + "</svg>\n");
	}

	@Override
	protected void doInitialize() throws InitializationException {
		if (!isMockMode()) {
			try {
				getSnapValue = new TangoCommand(getSnapExtractorName(),
						"GetSnapValue");
			} catch (final DevFailed e) {
				throw new DevFailedInitializationException(e, this);
			}
		}
		super.doInitialize();
	}

	protected void executeAndSendValues() throws DevFailed, ProcessingException {
		if (isMockMode()) {
			ExecutionTracerService.trace(this,
					"MOCK - snap values for snap ID " + snapID + " : [read = "
							+ 1 + "] [write = " + 2 + "]");
			sendOutputMsg(output, PasserelleUtil.createContentMessage(this, 1));
			sendOutputMsg(writePort, PasserelleUtil.createContentMessage(this,
					2));
		} else {
			final List<String> snapValues = getSnapValue.executeExtractList(
					String.class, snapID, attributeName);
			logger.debug("att " + attributeName);
			ExecutionTracerService.trace(this, "snap values for snap ID "
					+ snapID + " : [read = " + snapValues.get(0)
					+ "] [write = " + snapValues.get(1) + "]");
			sendOutputMsg(output, PasserelleUtil.createContentMessage(this,
					snapValues.get(0)));
			sendOutputMsg(writePort, PasserelleUtil.createContentMessage(this,
					snapValues.get(1)));
		}
	}

	@Override
	/*
	 * @throws IllegalActionException
	 */
	public void attributeChanged(final Attribute attribute)
			throws IllegalActionException {
		if (attribute == attributeNameParam) {
			attributeName = PasserelleUtil
					.getParameterValue(attributeNameParam);
		} else {
			super.attributeChanged(attribute);
		}
	}

	public String getSnapID() {
		return snapID;
	}

	public void setSnapID(final String snapID) {
		this.snapID = snapID;
	}

	public String getSnapExtractorName() throws DevFailed {
		if (snapExtractorName == null) {
			snapExtractorName = SoleilUtilities
					.getDevicesFromClass("SnapExtractor")[0];
		}
		return snapExtractorName;
	}

}
