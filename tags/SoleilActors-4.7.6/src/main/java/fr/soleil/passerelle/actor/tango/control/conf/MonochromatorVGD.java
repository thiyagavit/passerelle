package fr.soleil.passerelle.actor.tango.control.conf;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class MonochromatorVGD extends ConfigureMonochromator {

	public MonochromatorVGD(final CompositeEntity arg0, final String arg1)
			throws NameDuplicationException, IllegalActionException {
		super(arg0, arg1);
		confParam = new StringParameter(this, "VGD");
		confParam.setExpression(confValue);

		super.setEnumDeviceName("-VGD");
	}

}