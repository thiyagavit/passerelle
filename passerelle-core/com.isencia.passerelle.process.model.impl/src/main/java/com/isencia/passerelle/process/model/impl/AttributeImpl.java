/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import com.isencia.passerelle.process.model.Attribute;

/**
 * @author "puidir"
 *
 */
public abstract class AttributeImpl implements Attribute {

	private static final long serialVersionUID = 1L;
	private static final int MAX_CHAR_SIZE = 4000;

	@Version
	protected int version;
	
	@Column(name = "NAME", nullable = false, unique = false, updatable = false)
	private String name;
	
	@Column(name = "VALUE", nullable = false, unique = false, updatable = false)
	private String value;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "LOB_ID", unique = true, nullable = true, updatable = false)
	private ClobItem clobItem;

	protected AttributeImpl() {
	}
	
	protected AttributeImpl(String name, String value) {
		this.name = name;
		if (value != null && value.length() > MAX_CHAR_SIZE) {
			this.clobItem = new ClobItem(value);
		} else {
			this.value = value;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValue()
	 */
	public String getValue() {
		return getValueAsString();
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValueAsString()
	 */
	public String getValueAsString() {
		if (clobItem != null) {
			return clobItem.getValue();
		}

		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
