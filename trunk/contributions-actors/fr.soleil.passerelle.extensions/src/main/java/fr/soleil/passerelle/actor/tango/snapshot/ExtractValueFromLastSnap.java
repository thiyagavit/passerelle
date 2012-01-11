package fr.soleil.passerelle.actor.tango.snapshot;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.message.ManagedMessage;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.DevFailedInitializationException;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class ExtractValueFromLastSnap extends ASnapExtractor {

	private TangoCommand getSnapID;

	public Parameter contextIDParam;
	private String contextID;

	/**
	 * 
	 * @param container
	 * @param name
	 * @throws NameDuplicationException
	 * @throws IllegalActionException
	 */
	public ExtractValueFromLastSnap(final CompositeEntity container,
			final String name) throws NameDuplicationException,
			IllegalActionException {
		super(container, name);

		contextIDParam = new StringParameter(this, "Context ID");
		contextIDParam.setExpression("1");
	}

	@Override
	protected void doInitialize() throws InitializationException {
		if (!isMockMode()) {
			try {
				getSnapID = new TangoCommand(getSnapExtractorName(),
						"GetSnapID");
			} catch (final DevFailed e) {
				throw new DevFailedInitializationException(e, this);
			}
		}
		super.doInitialize();
	}

	@Override
	protected void doFire(final ManagedMessage arg0) throws ProcessingException {
		try {
			// Syntax for GetSnapID:
			// ctx_id, "id_snap > | < | = | <= | >= nbr",
			// "time < | > | >= | <=  yyyy-mm-dd hh:mm:ss | dd-mm-yyyy hh:mm:ss",
			// "comment starts | ends | contains string",
			// first | last
			if (!isMockMode()) {
				final String[] argin = { contextID, "last" };
				final String snapID = getSnapID.execute(String.class, argin);
				super.setSnapID(snapID);
			}
			executeAndSendValues();
		} catch (final DevFailed e) {
			throw new DevFailedProcessingException(e, this);
		}
	}

	@Override
	/*
	 * @throws IllegalActionException
	 */
	public void attributeChanged(final Attribute attribute)
			throws IllegalActionException {
		if (attribute == contextIDParam) {
			contextID = PasserelleUtil.getParameterValue(contextIDParam);
		} else {
			super.attributeChanged(attribute);
		}
	}

	@Override
	protected String getExtendedInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
