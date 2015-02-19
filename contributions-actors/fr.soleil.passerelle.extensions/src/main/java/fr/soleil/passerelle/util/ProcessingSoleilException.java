package fr.soleil.passerelle.util;

import ptolemy.kernel.util.Nameable;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException.Severity;

public class ProcessingSoleilException extends ProcessingException{

    /**
     * Creates a new ProcessingException with NON_FATAL severity,
     * and the given parameters.
     * 
     * @param message the classical message of pre-JDK1.4 exceptions
     * @param context an object that can give additional info, e.g. input data
     * that caused the problem (may be null)
     * @param rootException an exception that may have caused the processing problem (may be null)
     */
    public ProcessingSoleilException(String message, Object context, Throwable rootException) {
        super(message, context, rootException);
    }
    
    /**
     * @param errorCode
     * @param message
     * @param modelElement
     * @param rootException
     */
    public ProcessingSoleilException(ErrorCode errorCode, String message, Nameable modelElement, Throwable rootException) {
        super(getSeverityFromErrorCode(errorCode), message, modelElement, rootException);
    }
    
    
    public static Severity getSeverityFromErrorCode(ErrorCode errorCode){
        Severity severity = Severity.NON_FATAL;
        if(errorCode != null && errorCode == ErrorCode.FATAL){
            severity = Severity.FATAL;
        }
        return severity;
        
    }
}
