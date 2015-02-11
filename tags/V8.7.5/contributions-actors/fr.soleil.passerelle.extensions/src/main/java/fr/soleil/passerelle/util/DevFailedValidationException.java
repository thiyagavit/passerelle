package fr.soleil.passerelle.util;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.PasserelleException;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;

@SuppressWarnings("serial")
public class DevFailedValidationException extends ValidationException{
	
	public DevFailedValidationException(DevFailed df, Actor actor) {
		super(PasserelleException.Severity.FATAL, TangoToPasserelleUtil
				.getDevFailedString(df, actor), actor, df);	
	}
}
