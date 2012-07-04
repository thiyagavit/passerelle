/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import javax.persistence.DiscriminatorValue;

/**
 * @author "puidir"
 *
 */
@DiscriminatorValue("STRING_RESULT")
public class StringResultItemImpl extends ResultItemImpl<String> {

	private static final long serialVersionUID = 1L;

	public StringResultItemImpl(String value) {
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValue()
	 */
	public String getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValueAsString()
	 */
	public String getValueAsString() {
		return value;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultItem#getDataType()
	 */
	public String getDataType() {
		return "String";
	}

}
