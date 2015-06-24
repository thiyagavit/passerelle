package fr.soleil.passerelle.actor.tango.control.conf;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class MonochromatorFunctionningMode extends ConfigureMonochromator {

	public MonochromatorFunctionningMode(final CompositeEntity arg0,
			final String arg1) throws NameDuplicationException,
			IllegalActionException {
		super(arg0, arg1);
		confParam = new StringParameter(this, "Behavior");
		confParam.setExpression(confValue);

		super.setEnumDeviceName("-MODE");
	}

}