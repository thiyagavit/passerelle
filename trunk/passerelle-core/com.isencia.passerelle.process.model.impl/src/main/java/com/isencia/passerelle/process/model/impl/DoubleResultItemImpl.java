/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import javax.persistence.DiscriminatorValue;

/**
 * @author "puidir"
 *
 */
@DiscriminatorValue("DOUBLE_RESULT")
public class DoubleResultItemImpl extends ResultItemImpl<Double> {

	private static final long serialVersionUID = 1L;

	public DoubleResultItemImpl(Double value) {
		this.value = Double.toString(value);
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultItem#getDataType()
	 */
	public String getDataType() {
		return "Double";
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValue()
	 */
	public Double getValue() {
		return Double.parseDouble(value);
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValueAsString()
	 */
	public String getValueAsString() {
		return value;
	}

}
