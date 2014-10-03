/*
 * (c) Copyright 2001-2005, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package fr.soleil.passerelle.message.type;

import java.lang.reflect.Constructor;

import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.type.ConverterBase;

/**
 * Convert between IntToken and Passerelle messages
 * 
 * @author erwin.de.ley@isencia.be
 */
public class AttributeEventConverter extends ConverterBase {
	protected boolean areTypesCompatible(Class fromType, Type toType) {
		boolean res = BaseType.INT.equals(toType);
		if(fromType!=null) {
			// check some cases where we can easily convert to a Integer
			res = res && 
				(String.class.equals(fromType)
			  || Boolean.class.equals(fromType)
			  || Number.class.isAssignableFrom(fromType));
		}
		return res;
	}

	protected boolean areTypesCompatible(Type fromType, Class toType) {
		return BaseType.INT.equals(fromType);
	}

	protected Token convertContentToToken(Object content, Type targetType) throws UnsupportedOperationException, MessageException {
		IntToken result;
		if(content==null) {
			result = new IntToken();
		} else if(Integer.class.isInstance(content)) {
			result = new IntToken(((Integer)content).intValue());
		} else {
			try {
				// do a conversion via a String representation
				result = new IntToken(content.toString());
			} catch (IllegalActionException e) {
				throw new UnsupportedOperationException();
			}
		}
		return result;
	}

	protected Object convertTokenToContent(Token typedToken, Class targetType) throws MessageException {
		if(targetType!=null && !Integer.class.equals(targetType)) {
			// do a conversion via a String representation
			try {
				Constructor c = targetType.getConstructor(new Class[] {String.class});
				return c.newInstance(new Object[] {Integer.toString(((IntToken)typedToken).intValue())});
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		} else {
			try {
				return new Integer(((IntToken)typedToken).intValue());
			} catch (ClassCastException e) {
				throw new UnsupportedOperationException();
			}
		}
	}
	
	protected boolean areTypesCompatible(Class fromType, Class toType) {
		return toType==null || Integer.class.isAssignableFrom(toType);
	}

	protected Object convertContentToType(Object content, Class targetType) throws UnsupportedOperationException, MessageException {
		if(targetType!=null && !Integer.class.equals(targetType)) {
			// do a conversion via a String representation
			try {
				Constructor c = targetType.getConstructor(new Class[] {String.class});
				return c.newInstance(new Object[] {content.toString()});
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		} else {
			try {
				return new Integer(Integer.parseInt(content.toString()));
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		}
	}

}
