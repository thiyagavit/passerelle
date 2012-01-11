package fr.soleil.passerelle.actor.tango.snapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.message.ManagedMessage;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class ExtractValueFromSnapID extends ASnapExtractor {

	private final static Logger logger = LoggerFactory.getLogger(ExtractValueFromSnapID.class);

	public ExtractValueFromSnapID(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		input.setExpectedMessageContentType(Double.class);
		input.setName("SnapID");
	}

	@Override
	protected void doFire(ManagedMessage arg0) throws ProcessingException {
		Double snapID = (Double) PasserelleUtil.getInputValue(arg0);
		Long tmp = snapID.longValue();
		String convertedSnapID = tmp.toString();
		logger.debug(convertedSnapID);
		try {
			super.setSnapID(convertedSnapID);
			executeAndSendValues();
		} catch (DevFailed e) {
			throw new DevFailedProcessingException(e, this);
		}
	}

	@Override
	protected String getExtendedInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
