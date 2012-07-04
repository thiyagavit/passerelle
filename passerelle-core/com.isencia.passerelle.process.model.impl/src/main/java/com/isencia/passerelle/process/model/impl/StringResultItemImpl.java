/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import javax.persistence.DiscriminatorValue;

import com.isencia.passerelle.process.model.ResultBlock;

/**
 * @author "puidir"
 *
 */
@DiscriminatorValue("STRING_RESULT")
public class StringResultItemImpl extends ResultItemImpl<String> {

	private static final long serialVersionUID = 1L;

	public StringResultItemImpl(ResultBlock resultBlock, String name, String unit, String value) {
		super(resultBlock, name, unit);
		this.value = value;
	}
	
	public StringResultItemImpl(ResultBlock resultBlock, String name, String value) {
		this(resultBlock, name, null, value);
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValue()
	 */
	public String getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultItem#getDataType()
	 */
	public String getDataType() {
		return "String";
	}

}
