/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.isencia.passerelle.process.model.ResultBlock;

/**
 * @author "puidir"
 *
 */
@DiscriminatorValue("STRING_RESULT")
public class StringResultItemImpl extends ResultItemImpl<String> {

	private static final long serialVersionUID = 1L;
	private static final int MAX_CHAR_SIZE = 4000;

	@OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "LOB_ID", unique = true, nullable = true, updatable = false)
	private ClobItem clobItem;
	
	public StringResultItemImpl(ResultBlock resultBlock, String name, String unit, String value) {
		super(resultBlock, name, unit);
		if (value != null && value.length() > MAX_CHAR_SIZE) {
			this.clobItem = new ClobItem(value);
		} else {
			this.value = value;
		}
	}
	
	public StringResultItemImpl(ResultBlock resultBlock, String name, String value) {
		this(resultBlock, name, null, value);
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValue()
	 */
	public String getValue() {
		if (clobItem != null) {
			return clobItem.getValue();
		}
		
		return value;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultItem#getDataType()
	 */
	public String getDataType() {
		return "String";
	}

}
