/*
 * Created on 9 juin 2005
 * 
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package fr.soleil.passerelle.message.type;

/**
 * @author root
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Director;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleToken;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.ptolemy.PtolemyType;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * A converter specific for Tango
 * 
 * @author erwin.de.ley@isencia.be
 */
public class AttributeBaseConverter implements com.isencia.passerelle.message.type.TypeConverter {
    static Logger logger = LoggerFactory.getLogger(AttributeBaseConverter.class);

    public Token convertPasserelleTokenToPtolemyToken(final PasserelleToken passerelleMsgToken, final Type targetType)
            throws UnsupportedOperationException, PasserelleException {

        logger.debug("convertPasserelleTokenToPtolemyToken() - in");
        if (passerelleMsgToken != null) {
            if (passerelleMsgToken instanceof PasserelleToken && isTargetTypeCompatible(targetType)) {
                Token result = null;
                final ManagedMessage passerelleMsg = MessageHelper.getMessageFromToken(passerelleMsgToken);
                try {
//          if (passerelleMsg.getBodyContent() instanceof Event) {
//            final Event event = (Event) passerelleMsg.getBodyContent();
//            result = convertEventToSimpleTypedToken(event, targetType);
//          } else 
                    {
                        final TangoAttribute attribute = (TangoAttribute) passerelleMsg.getBodyContent();
                        result = convertAttributeToSimpleTypedToken(attribute, targetType);
                    }
                } catch (final PasserelleException e) {
                    throw e;
                } catch (final ClassCastException e) {
                    // indicates that the msg does not contain the right stuff
                    // for this converter
                    throw new UnsupportedOperationException();
                } catch (final Exception e) {
                    ExceptionUtil.throwPasserelleException("Unexpected error during conversion attempt",
                            passerelleMsgToken, e);
                }

                logger.debug(" convertPasserelleTokenToPtolemyToken() - out");

                return result;
            } else {
                throw new UnsupportedOperationException();
            }
        } else {

            logger.debug(" convertPasserelleTokenToPtolemyToken() - out");

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public PasserelleToken convertPtolemyTokenToPasserelleToken(final Token origToken, final Class targetType)
            throws UnsupportedOperationException, PasserelleException {

        logger.debug(" convertPtolemyTokenToPasserelleToken() - in");

        ManagedMessage passerelleMsg = null;
        if (origToken == null) {
            return null;
        } else if (PasserelleToken.class.isInstance(origToken)) {

            final PasserelleToken passerelleMsgToken = (PasserelleToken) origToken;
            try {
                passerelleMsg = MessageHelper.getMessageFromToken(passerelleMsgToken);
//        if (passerelleMsg.getBodyContent() instanceof Event) {
//          final Event event = (Event) passerelleMsg.getBodyContent();
//          final Object resultContent = convertEventToSimpleTypedObject(event, targetType);
//          passerelleMsg.setBodyContent(resultContent, ManagedMessage.objectContentType);
//          logger.debug(" convertPtolemyTokenToPasserelleToken() - conversion done - PasserelleToken");
//        } else 
                {
                    final TangoAttribute attribute = (TangoAttribute) passerelleMsg.getBodyContent();
                    final Object resultContent = convertAttributeToSimpleTypedObject(attribute, targetType);
                    passerelleMsg.setBodyContent(resultContent, ManagedMessage.objectContentType);
                    logger.debug(" convertPtolemyTokenToPasserelleToken() - conversion done - PasserelleToken");
                }
            } catch (final ClassCastException e) {
                // indicates that the msg does not contain the right stuff for
                // this converter
                logger.debug(" convertPtolemyTokenToPasserelleToken() - UnsupportedOperationException");
                throw new UnsupportedOperationException();
            } catch (final PasserelleException e) {
                logger.debug(" convertPtolemyTokenToPasserelleToken() - PasserelleException");
                throw e;
            } catch (final Exception e) {
                logger.debug(" convertPtolemyTokenToPasserelleToken() - Exception");
                ExceptionUtil.throwPasserelleException("Unexpected error during conversion attempt",
                        passerelleMsgToken, e);
            }
        } else {
            logger.debug("UnsupportedOperationException ");
            throw new UnsupportedOperationException();
        }
        PasserelleToken result = null;
        if (passerelleMsg != null) {
            result = new PasserelleToken(passerelleMsg);
        }

        logger.debug(" convertPtolemyTokenToPasserelleToken() - out");

        return result;
    }

    /**
     * Converts a Tango Attribute to a std Ptolemy token with the desired content type.
     * 
     * @param content
     * @param targetType
     * @return
     */
    public Token convertAttributeToSimpleTypedToken(final TangoAttribute content, final Type targetType)
            throws UnsupportedOperationException, PasserelleException {
        logger.debug("convertAttributeToSimpleTypedToken");
        Token result = null;
        try {
            final PtolemyType type = PtolemyType.getPtolemyType(targetType);
          
            if (content.isScalar()) {
                try {
                    result = type.getTokenForString(content.extract(String.class));
                    if (result == null) { // means that this attribute is WRITE
                        // only
                        result = type.getTokenForString(content.extractWritten(String.class));
                    }
                } catch (final IllegalActionException e) {
                    ExceptionUtil.throwPasserelleException("Error during construction of Token", content, e);
                }
            } else {
                String[] array = content.extractSpecOrImage(String.class);
                if (array.length == 0) {
                    array = content.extractWrittenSpecOrImage(String.class);
                }
                final Token[] tokensArray = new Token[array.length];
                try {
                    for (int i = 0; i < tokensArray.length; i++) {
                        tokensArray[i] = type.getTokenForString(array[i]);
                    }
                    result = new ArrayToken(tokensArray);
                } catch (final IllegalActionException e) {
                    ExceptionUtil.throwPasserelleException("Error during construction of ArrayToken", content, e);
                }
            }
        } catch (final DevFailed e) {
            ExceptionUtil.throwPasserelleException(TangoToPasserelleUtil.getDevFailedString(e, (Director) null), content, e);
        }
        
        return result;
    }

    /**
     * Converts a Event to a std Ptolemy token with the desired content type.
     * 
     * @param content
     * @param targetType
     * @return
     */
//  public Token convertEventToSimpleTypedToken(final Event event, final Type targetType) throws UnsupportedOperationException, PasserelleException {
//    logger.debug("convertAttributeToSimpleTypedToken");
//    final PtolemyType type = PtolemyType.getPtolemyType(targetType);
//    Token result;
//    try {
//      if (event instanceof ResultItem) {
//        result = type.getTokenForString(((ResultItem) event).getValue());
//      } else {
//        result = type.getTokenForString(event.toString());
//      }
//
//    } catch (final IllegalActionException e) {
//      throw new PasserelleException("Error during construction of Token", event, e);
//    }
//    return result;
//
//  }

    /**
     * Converts a Event to an object with the desired type (Double, String etc).
     * 
     * @param content
     * @param targetType
     * @return
     */
//  @SuppressWarnings("unchecked")
//  public Object convertEventToSimpleTypedObject(final Event event, final Class targetType) throws UnsupportedOperationException, PasserelleException {
//    Object result;
//    logger.debug("convertEventToSimpleTypedObject");
//    if (Event.class.isAssignableFrom(targetType)) {
//      result = event;
//    } else {
//      if (event instanceof ResultItem) {
//        try {
//          final ResultItem ri = (ResultItem) event;
//          if (targetType.isAssignableFrom(Double.class)) {
//            result = ri.getDoubleValue();
//          } else if (targetType.isAssignableFrom(Float.class)) {
//            result = Float.parseFloat(ri.getValue());
//          } else if (targetType.isAssignableFrom(Integer.class)) {
//            result = (int) ri.getDoubleValue().doubleValue();
//          } else if (targetType.isAssignableFrom(Short.class)) {
//            result = (short) ri.getDoubleValue().doubleValue();
//          } else if (targetType.isAssignableFrom(Byte.class)) {
//            result = (byte) ri.getDoubleValue().doubleValue();
//          } else if (targetType.isAssignableFrom(String.class)) {
//            result = ri.getValue();
//          } else if (targetType.isAssignableFrom(Date.class)) {
//            result = ri.getCreationTS();
//          } else {
//            throw new UnsupportedOperationException();
//          }
//        } catch (final NumberFormatException e) {
//          throw new UnsupportedOperationException();
//        }
//      } else {
//        if (targetType.isAssignableFrom(String.class)) {
//          result = event.toString();
//        } else if (targetType.isAssignableFrom(Date.class)) {
//          result = event.getCreationTS();
//        } else {
//          throw new UnsupportedOperationException();
//        }
//
//      }
//    }
//
//    return result;
//
//  }

    /**
     * Converts a Tango Attribute to an object with the desired type (Double, String etc).
     * 
     * @param content
     * @param targetType
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object convertAttributeToSimpleTypedObject(final TangoAttribute content, final Class targetType)
            throws UnsupportedOperationException, PasserelleException {
        Object result = null;
        logger.debug("convertAttributeToSimpleTypedObject");
        try {
//      if (Event.class.isAssignableFrom(targetType)) {
//        result = new ResultItemImpl(content.getName(), content.extract(String.class), null, new Date(content.getTimestamp()));
//      } else 
            {
                if (content.isSpectrum() || content.isImage()) {
                    result = content.extractSpecOrImage(targetType);
                    if (content.extractSpecOrImage(targetType).length == 0) {
                        // means that this attribute is WRITE only
                        result = content.extractWrittenSpecOrImage(targetType);
                    }
                } else {
                    result = content.extract(targetType);
                    if (result == null) { // means that this attribute is WRITE
                        // only
                        result = content.extractWritten(targetType);
                    }
                }
            }
        } catch (final DevFailed e) {
            //e.printStackTrace();
            ExceptionUtil.throwPasserelleException(TangoToPasserelleUtil.getDevFailedString(e, (Director) null), content, e);
        }
        return result;
    }

    /**
     * @param targetType
     * @return
     */
    public boolean isTargetTypeCompatible(final Type targetType) {
        return true;
    }

    /**
     * From a Passerelle msg with an Attribute in there to one with some other content type
     */
    @SuppressWarnings("unchecked")
    public PasserelleToken convertPasserelleMessageContent(final PasserelleToken origToken,
            final Class targetContentType) throws UnsupportedOperationException, PasserelleException {
        logger.debug("convertPasserelleMessageContent() - in");
        if (origToken == null || origToken.getMessage() == null || origToken.getMessage().getBodyContent() == null) {
            return origToken;
        } else if (TangoAttribute.class.isAssignableFrom(origToken.getMessageContentType())) {
            PasserelleToken result = origToken;
            try {
                final TangoAttribute content = (TangoAttribute) origToken.getMessage().getBodyContent();
                if (content != null && !content.getClass().equals(targetContentType)) {
                    final Object resultContent = convertAttributeToSimpleTypedObject(content, targetContentType);
                    final ManagedMessage msg = MessageFactory.getInstance().copyMessage(origToken.getMessage());
                    if (String.class.isInstance(resultContent)) {
                        msg.setBodyContentPlainText((String) resultContent);
                    } else {
                        msg.setBodyContent(resultContent, ManagedMessage.objectContentType);
                    }
                    result = new PasserelleToken(msg);
                }
            } catch (final MessageException e) {
                logger.error("", e);
            }
            return result;
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
