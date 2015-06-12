package fr.soleil.passerelle.util;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;

public class ExceptionUtil {
    public static final String FATAL_ERROR = Severity.FATAL.toString();
    public static final String NON_FATAL_ERROR = Severity.NON_FATAL.toString();

    public static void throwPasserelleException(String errorMessage, Object context) throws PasserelleException {
        throwPasserelleException(errorMessage, context, null);
    }

    public static void throwPasserelleException(String errorMessage, Object context, Throwable rootException)
            throws PasserelleException {
        throw new PasserelleException(errorMessage, context, rootException);
    }

    public static void throwPasserelleException(ErrorCode errorCode, String errorMessage, Object context)
            throws PasserelleException {
        throw new PasserelleException(getSeverityFromErrorCode(errorCode), errorMessage, context, null);
    }

    public static void throwValidationException(String errorMessage, Actor actor) throws ValidationException {
        throwValidationException(errorMessage, actor, null);
    }

    public static void throwValidationException(String errorMessage, Actor actor, Throwable rootException)
            throws ValidationException {
        throw new ValidationException(errorMessage, actor, rootException);
    }

    public static void throwValidationException(Actor actor, DevFailed df) throws ValidationException {
        throw new ValidationException(getSeverityFromErrorCode(ErrorCode.FATAL),
                TangoToPasserelleUtil.getDevFailedString(df, actor), actor, df);
    }

    public static void throwValidationException(ErrorCode errorCode, String errorMessage, Object context)
            throws ValidationException {
        throw new ValidationException(getSeverityFromErrorCode(errorCode), errorMessage, context, null);
    }

    public static void throwProcessingExceptionWithLog(Actor actor, String errorMessage, Object context)
            throws ProcessingException {
        throwProcessingExceptionWithLog(actor, errorMessage, context, null);
    }

    public static void throwProcessingExceptionWithLog(Actor actor, String errorMessage, Object context,
            Throwable rootException) throws ProcessingException {
        ProcessingException ex = new ProcessingException(errorMessage, context, rootException);
        ExecutionTracerService.trace(actor, "Error: " + errorMessage);
        throw ex;
    }

    public static void throwProcessingExceptionWithLog(Actor actor, ErrorCode errorCode, String errorMessage,
            Object context, Throwable rootException) throws ProcessingException {
        ProcessingException ex = new ProcessingException(getSeverityFromErrorCode(errorCode), errorMessage, context,
                rootException);
        ExecutionTracerService.trace(actor, "Error: " + errorMessage);
        throw ex;
    }

    public static void throwProcessingExceptionWithLog(Actor actor, ErrorCode errorCode, String errorMessage,
            Object context) throws ProcessingException {
        throwProcessingExceptionWithLog(actor, errorCode, errorMessage, context, null);
    }

    public static void throwProcessingException(String errorMessage, Object context, Throwable rootException)
            throws ProcessingException {
        throw new ProcessingException(errorMessage, context, rootException);
    }

    public static void throwProcessingException(Actor actor, DevFailed df) throws ProcessingException {
        throwProcessingException(TangoToPasserelleUtil.getDevFailedString(df, actor), actor, df);
    }

    public static void throwProcessingException(ErrorCode errorCode, Actor actor, DevFailed df)
            throws ProcessingException {
        throwProcessingException(errorCode, TangoToPasserelleUtil.getDevFailedString(df, actor), actor, df);
    }

    public static void throwProcessingException(ErrorCode errorCode, String errorMessage, Object context)
            throws ProcessingException {
        throwProcessingException(errorCode, errorMessage, context, null);
    }

    public static void throwProcessingException(ErrorCode errorCode, String errorMessage, Object context,
            Throwable rootException) throws ProcessingException {
        throw new ProcessingException(getSeverityFromErrorCode(errorCode), errorMessage, context, rootException);
    }

    public static void throwProcessingException(String errorMessage, Object context) throws ProcessingException {
        throwProcessingException(errorMessage, context, null);
    }

    public static void throwProcessingException(String errorMessage) throws ProcessingException {
        throwProcessingException(errorMessage, null);
    }

    public static void throwInitializationException(ErrorCode errorCode, String errorMessage, Object context)
            throws InitializationException {
        throwInitializationException(errorCode, errorMessage, context, null);
    }

    public static void throwInitializationException(ErrorCode errorCode, String errorMessage, Object context,
            Throwable rootException) throws InitializationException {
        throw new InitializationException(getSeverityFromErrorCode(errorCode), errorMessage, context, rootException);
    }

    public static void throwInitializationException(Actor actor, DevFailed df) throws InitializationException {
        throwInitializationException(ErrorCode.FATAL, TangoToPasserelleUtil.getDevFailedString(df, actor), actor, df);
    }

    public static void throwInitializationException(String errorMessage, Object context) throws InitializationException {
        throwInitializationException(errorMessage, context, null);
    }

    public static void throwInitializationException(String errorMessage, Object context, Throwable rootException)
            throws InitializationException {
        throw new InitializationException(errorMessage, context, rootException);
    }

    public static void throwTerminationException(String errorMessage, Object context, Throwable rootException)
            throws TerminationException {
        throw new TerminationException(errorMessage, context, rootException);
    }

    public static Severity getSeverityFromErrorCode(ErrorCode errorCode) {
        Severity severity = Severity.NON_FATAL;
        if (errorCode != null && errorCode == ErrorCode.FATAL) {
            severity = Severity.FATAL;
        }
        return severity;
    }

    public static boolean isSeverityFatal(Severity severity) {
        return severity == Severity.NON_FATAL;
    }
}
