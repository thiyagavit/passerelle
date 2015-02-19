package fr.soleil.passerelle.util;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;

@SuppressWarnings("serial")
public class DevFailedProcessingException extends ProcessingException {

    public DevFailedProcessingException(DevFailed df, Actor actor) {
        super(Severity.NON_FATAL, TangoToPasserelleUtil.getDevFailedString(df, actor), actor, df);
        // PasserelleUtil.sendException(actor, errorPort, this);
    }

    public DevFailedProcessingException(DevFailed df, ErrorCode errorCode, Actor actor) {
        this(df, ProcessingSoleilException.getSeverityFromErrorCode(errorCode), actor);
    }

    public DevFailedProcessingException(DevFailed df, Severity severity, Actor actor) {
        super(severity, TangoToPasserelleUtil.getDevFailedString(df, actor), actor, df);
    }

}
