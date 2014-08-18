package fr.soleil.passerelle.actor.flow;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class MessageAssembler extends Transformer {

	private String outputMessage;

	public MessageAssembler(final CompositeEntity container, final String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		input.setExpectedMessageContentType(String.class);
	}

	@Override
	protected void doInitialize() throws InitializationException {
		outputMessage = "";
		super.doInitialize();
	}

	@Override
	protected void doFire(final ManagedMessage message)
			throws ProcessingException {
		// for the moment just get values and put it a string
		final String input = PasserelleUtil.getInputValue(message).toString();
		if (outputMessage.compareTo("") == 0) {
			outputMessage = input;
		} else {
			outputMessage = outputMessage + "," + input;
		}
		ExecutionTracerService.trace(this, "message: " + outputMessage);
		sendOutputMsg(output, PasserelleUtil.createContentMessage(this,
				outputMessage));
	}

	@Override
	protected String getExtendedInfo() {
		return null;
	}

}
