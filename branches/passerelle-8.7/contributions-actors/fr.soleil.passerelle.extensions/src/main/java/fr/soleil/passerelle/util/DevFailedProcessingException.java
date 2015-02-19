package fr.soleil.passerelle.util;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;

public class DevFailedProcessingException extends ProcessingException{

     private static final long serialVersionUID = 6853820731030206848L;

        public DevFailedProcessingException(DevFailed df,Actor actor) {
                super(Severity.NON_FATAL, TangoToPasserelleUtil
                                .getDevFailedString(df, actor), actor, df);
        //      PasserelleUtil.sendException(actor, errorPort, this);
        }

        public DevFailedProcessingException(DevFailed df,Severity severity,Actor actor) {
                super(severity, TangoToPasserelleUtil.getDevFailedString(df, actor), actor, df);
        }
}
