package fr.soleil.passerelle.message.type;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleToken;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import fr.soleil.passerelle.ptolemy.PtolemyType;

/**
 * Convert between Passerelle Messages
 * 
 * @author erwin.de.ley@isencia.be
 */
public class ArrayConverter extends com.isencia.passerelle.message.type.ArrayConverter {

    static Logger logger = LoggerFactory.getLogger(ArrayConverter.class);

    @Override
    public PasserelleToken convertPtolemyTokenToPasserelleToken(final Token origToken,
	    final Class targetContentType) throws UnsupportedOperationException,
	    PasserelleException {
	logger.debug("convertPtolemyTokenToPasserelleToken - in ");
	if (origToken == null) {
	    logger.debug("convertPtolemyTokenToPasserelleToken - null ");
	    return null;
	} else if (PasserelleToken.class.isInstance(origToken)) {
	    logger.debug("convertPtolemyTokenToPasserelleToken - PasserelleToken ");
	    return convertPasserelleMessageContent((PasserelleToken) origToken, targetContentType);
	} else {
	    // TODO: remove that patch for Array converter
	    // this patch is able to convert an ArrayToken to a String with
	    // values separated by commas
	    // but it should normally returns an array
	    System.out.println("Array converter " + targetContentType);
	    // if (String.class.isAssignableFrom(targetContentType)) {

	    if (origToken.getType().getTokenClass() == ArrayToken.class) {
		logger.debug("###ArrayConverter arrayconverter patch : "
			+ origToken.getType().getTokenClass());
		final ManagedMessage passerelleMsg = MessageFactory.getInstance().createMessage();
		passerelleMsg.setBodyContent(PtolemyType.getStringForToken(origToken),
			ManagedMessage.objectContentType);
		final PasserelleToken result = new PasserelleToken(passerelleMsg);
		logger.debug(" convertPtolemyTokenToPasserelleToken() - out");

		return result;
	    }
	    // } else {
	    // logger.debug("not converting " + origToken);
	    // throw new UnsupportedOperationException();
	    // }
	}
	return null;
    }

    @Override
    public PasserelleToken convertPasserelleMessageContent(final PasserelleToken origToken,
	    final Class targetContentType) throws UnsupportedOperationException,
	    PasserelleException {
	logger.debug("convertPasserelleMessageContent - in ");
	if (origToken == null || origToken.getMessage() == null
		|| origToken.getMessage().getBodyContent() == null) {
	    return origToken;
	} else if (areTypesCompatible(origToken.getMessageContentType(), targetContentType)) {
	    logger.debug("convertPasserelleMessageContent - areTypesCompatible ");
	    PasserelleToken result = null;
	    try {
		final Object content = convertTokenToContent(origToken, targetContentType);
		final ManagedMessage msg = MessageFactory.getInstance().createMessage();
		if (String.class.isInstance(content)) {
		    logger.debug("convertPasserelleMessageContent - String.class ");
		    // TODO need to think about this a bit.
		    // Is this acceptable if we would want to use specific
		    // String subclasses?
		    // Or should we then also put the mime type to
		    // ManagedMessage.objectContentType?
		    msg.setBodyContentPlainText((String) content);
		} else {
		    logger.debug("convertPasserelleMessageContent - else ");
		    msg.setBodyContent(content, ManagedMessage.objectContentType);
		}
		result = new PasserelleToken(msg);
	    } catch (final MessageException e) {
		logger.error("", e);
	    }
	    return result;
	} else if (String.class.isAssignableFrom(targetContentType)) {
	    // try to convert the input array to a string
	    PasserelleToken result = origToken;
	    final Object content = origToken.getMessage().getBodyContent();
	    logger.debug("convertPasserelleMessageContent - target is a string " + content);
	    logger.debug("convertPasserelleMessageContent - content " + content.getClass());
	    if (content != null) {
		final ManagedMessage msg = MessageFactory.getInstance().copyMessage(
			origToken.getMessage());
		/*
		 * if (String.class.isAssignableFrom(content.getClass())) { //
		 * input is a string msg.setBodyContent(content,
		 * ManagedMessage.objectContentType); } else
		 */if (content.getClass().isArray()) {
		    // input is an array
		    logger.debug("convertPasserelleMessageContent - input array "
			    + content.getClass().getComponentType());
		    final String[] values = new String[Array.getLength(content)];
		    for (int i = 0; i < values.length; i++) {
			values[i] = Array.get(content, i).toString();
		    }
		    logger.debug("convertPasserelleMessageContent -  conversion done "
			    + Arrays.toString(values));
		    msg.setBodyContent(values, ManagedMessage.objectContentType);
		} else if (content.getClass() == ArrayToken.class) {
		    logger.debug("###ArrayConverter arrayconverter patch ");
		    msg.setBodyContent(PtolemyType.getStringForToken((ArrayToken) content),
			    ManagedMessage.objectContentType);

		    logger.debug(" convertPasserelleMessageContent - conversion done "
			    + PtolemyType.getStringForToken((ArrayToken) content));
		} else {
		    logger.debug("not converting " + origToken);
		    throw new UnsupportedOperationException();
		}
		result = new PasserelleToken(msg);
	    }
	    return result;
	} else {
	    logger.debug("convertPasserelleMessageContent - UnsupportedOperationException ");
	    throw new UnsupportedOperationException();
	}
    }
}
