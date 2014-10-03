package fr.soleil.passerelle.actor.tango.recording;

import java.net.URL;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class SaveDevice extends ATangoDeviceActor {

	public SaveDevice(final CompositeEntity arg0, final String arg1)
			throws NameDuplicationException, IllegalActionException {
		super(arg0, arg1);

		input.setName("trigger");
		recordDataParam.setVisibility(Settable.EXPERT);

		final URL url = this
				.getClass()
				.getResource(
						"/org/tango-project/tango-icon-theme/32x32/categories/applications-multimedia.png");
		_attachText(
				"_iconDescription",
				"<svg>\n"
						+ "<rect x=\"-20\" y=\"-20\" width=\"40\" "
						+ "height=\"40\" style=\"fill:red;stroke:black\"/>\n"
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
	protected void process(final ActorContext ctxt,
			final ProcessRequest request, final ProcessResponse response)
			throws ProcessingException {

		final ManagedMessage message = request.getMessage(input);

		if (isMockMode()) {
			ExecutionTracerService.trace(this, "MOCK - saving "
					+ getDeviceName());
		} else {
			// since some devices are not registered in the datarecorder config
			// either as technical data or Experimental data, it is not an
			// error.
			ExecutionTracerService.trace(this, "saving " + getDeviceName());
			try {
				DataRecorder.getInstance().saveDevice(this, getDeviceName());
			} catch (final DevFailed e) {

				e.printStackTrace();
				TangoToPasserelleUtil.getDevFailedString(e, this);
				// throw new DevFailedProcessingException(e,this);
			}
			try {
				DataRecorder.getInstance().saveExperimentalData(this,
						getDeviceName());
			} catch (final DevFailed e) {
				e.printStackTrace();
				TangoToPasserelleUtil.getDevFailedString(e, this);
				// throw new DevFailedProcessingException(e,this);
			}
		}
		// sendOutputMsg(output, PasserelleUtil.createCopyMessage(this,
		// message));
		response.addOutputMessage(0, output, PasserelleUtil.createCopyMessage(
				this, message));
	}

	@Override
	protected String getExtendedInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
