package fr.soleil.passerelle.actor.flow;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class Diamond extends Transformer{

	public Diamond(CompositeEntity arg0, String arg1) throws NameDuplicationException, IllegalActionException {
		super(arg0, arg1);
		_attachText("_iconDescription", "<svg>\n"
				+ "<polygon points=\"-13,0, 0,13, 13,0, 0,-13\" "
                + "style=\"fill:black\"/>\n"
                + "</svg>\n");
	}

	@Override
	protected void doFire(ManagedMessage arg0) throws ProcessingException {
		sendOutputMsg(output, PasserelleUtil.createCopyMessage(this,arg0));
	}

}
