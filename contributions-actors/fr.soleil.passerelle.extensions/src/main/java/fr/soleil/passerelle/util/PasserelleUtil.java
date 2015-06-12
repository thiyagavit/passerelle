package fr.soleil.passerelle.util;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;

public final class PasserelleUtil {

    public static Object getInputValue(ManagedMessage message) throws ProcessingException {
        Object value = null;

        if (message == null) {
            ExceptionUtil.throwProcessingException("INPUT PORT ERROR - input message is null", message);
        }
        try {
            value = message.getBodyContent();
        } catch (MessageException e) {
            ExceptionUtil.throwProcessingException("INPUT PORT ERROR - Cannot get input message", message, e);
        }
        return value;
    }

    public static ManagedMessage createTriggerMessage() {
        return MessageFactory.getInstance().createTriggerMessage();
    }

    public static ManagedMessage createContentMessage(Actor act, Object content) throws ProcessingException {
        ManagedMessage resultMsg = act.createMessage();
        try {
            resultMsg.setBodyContent(content, ManagedMessage.objectContentType);
        } catch (MessageException e) {
            ExceptionUtil.throwProcessingException("OUTPUT PORT ERROR - Error creating output message", content, e);
        }
        return resultMsg;

    }

    public static ManagedMessage createCopyMessage(Actor act, ManagedMessage msg) throws ProcessingException {
        ManagedMessage resultMsg = null;
        try {
            resultMsg = MessageFactory.getInstance().copyMessage(msg);
        } catch (MessageException e) {
            ExceptionUtil.throwProcessingException("OUTPUT PORT ERROR - Error creating output message", msg, e);
        }
        return resultMsg;

    }

    public static ManagedMessage createContentMessageInSequence(Actor act, Object content, Long sequenceID,
            long sequencePos, boolean isSequenceEnd) throws ProcessingException {
        ManagedMessage resultMsg = MessageFactory.getInstance().createMessageInSequence(sequenceID, sequencePos,
                isSequenceEnd);
        try {
            resultMsg.setBodyContent(content, ManagedMessage.objectContentType);
        } catch (MessageException e) {
            ExceptionUtil.throwProcessingException("OUTPUT PORT ERROR - Error creating output message", content, e);
        }
        return resultMsg;
    }

    public static String getParameterValue(Parameter param) throws IllegalActionException {
        return ((StringToken) param.getToken()).stringValue().trim();
    }

    public static double getParameterDoubleValue(Parameter param) throws IllegalActionException {
        return Double.valueOf(((StringToken) param.getToken()).stringValue().trim());
    }

    public static int getParameterIntValue(Parameter param) throws IllegalActionException {
        return Integer.valueOf(((StringToken) param.getToken()).stringValue().trim());
    }

    public static boolean getParameterBooleanValue(Parameter param) throws IllegalActionException {
        return new Boolean(param.getExpression().trim()).booleanValue();
    }

    public static String getParameterValue(Attribute attribute, Parameter... params) throws IllegalActionException {
        String value = null;
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            if (param == attribute) {
                value = ((StringToken) param.getToken()).stringValue().trim();
                break;
            }
        }
        return value;
    }

    public static String getFullNameButWithoutModelName(NamedObj actor) {
        // the first string is the name of the model
        String fullName = actor.getFullName();
        int i = fullName.indexOf(".", 1);
        if (i > 0) {
            // there's always an extra '.' in front of the model name...
            // and a trailing '.' just behind it...
            fullName = fullName.substring(i + 1);
        }

        return fullName;
    }
}
