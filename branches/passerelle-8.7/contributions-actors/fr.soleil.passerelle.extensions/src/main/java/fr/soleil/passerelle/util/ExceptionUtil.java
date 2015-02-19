package fr.soleil.passerelle.util;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException.Severity;

import fr.esrf.Tango.DevFailed;

public class ExceptionUtil {
  
    public static void throwValidationException(String errorMessage, Actor actor) throws ValidationException{
        throwValidationException( errorMessage, actor,null);
    }
    
    public static void throwValidationException(String errorMessage, Actor actor,Throwable rootException) throws ValidationException{
        throw new ValidationException(errorMessage, actor, rootException);
    }
    
    public static void throwProcessingExceptionWithLog(Actor actor,ErrorCode errorCode,String errorMessage,Object context,Throwable rootException) throws ProcessingException {
        throw new ProcessingExceptionWithLog(actor, getSeverityFromErrorCode( errorCode), errorMessage, context, rootException);
    }
    
    public static void throwProcessingExceptionWithLog(Actor actor,ErrorCode errorCode,String errorMessage,Object context) throws ProcessingException {
        throwProcessingExceptionWithLog(actor, errorCode, errorMessage, context, null);
    }
    
    public static void throwProcessingException(String errorMessage,Object context,Throwable rootException) throws ProcessingException {
        throw new ProcessingException(errorMessage, context, rootException);
    }
    
    public static void throwProcessingException(String errorMessage,Object context) throws ProcessingException {
        throwProcessingException(errorMessage, context, null);
    }
    
    public static void throwProcessingException(String errorMessage) throws ProcessingException {
        throwProcessingException(errorMessage, null);
    }
    
    public static void throwDevFailedProcessingException(Actor actor,ErrorCode errorCode,DevFailed rootException) throws ProcessingException {
        throw new DevFailedProcessingException(rootException, getSeverityFromErrorCode( errorCode), actor);
    }
    
   
    
    public static Severity getSeverityFromErrorCode(ErrorCode errorCode){
        Severity severity = Severity.NON_FATAL;
        if(errorCode != null && errorCode == ErrorCode.FATAL){
            severity = Severity.FATAL;
        }
        return severity;
        
    }
}
