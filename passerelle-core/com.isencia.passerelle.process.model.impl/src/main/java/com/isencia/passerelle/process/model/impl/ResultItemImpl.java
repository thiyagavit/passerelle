/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;

/**
 * @author "puidir"
 * 
 */
@Entity
@Table(name = "PAS_RESULTITEM")
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING)
public abstract class ResultItemImpl<V> implements ResultItem<V> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", nullable = false, unique = true, updatable = false)
	@GeneratedValue(generator = "pas_resultitem")
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@Column(name = "NAME", nullable = false, unique = false, updatable = false)
	private String name;

	@Column(name = "VALUE", nullable = false, unique = false, updatable = false)
	protected String value;

	@Column(name = "UNIT", nullable = true, unique = false, updatable = false)
	private String unit;

	@OneToMany(targetEntity = ResultItemAttributeImpl.class, mappedBy = "resultItem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@MapKey(name = "name")
	private Map<String, Attribute> attributes = new HashMap<String, Attribute>();

	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter
	// methods
	@ManyToOne(targetEntity = ResultBlockImpl.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "RESULTBLOCK_ID")
	private ResultBlockImpl resultBlock;

	@Column(name = "COLOUR", nullable = true, unique = false, updatable = true)
	private String colour;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_TS", nullable = true, updatable = false)
	private Date creationTS;

	public ResultItemImpl() {
	}

	protected ResultItemImpl(ResultBlock resultBlock, String name, String unit, Date creationTS) {
		this(resultBlock, name, unit);
		this.creationTS = creationTS;
	}

	protected ResultItemImpl(ResultBlock resultBlock, String name, String unit) {
		this.resultBlock = (ResultBlockImpl) resultBlock;
		this.name = name;
		this.unit = unit;

		this.resultBlock.putItem(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.NamedValue#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.NamedValue#getValueAsString()
	 */
	public String getValueAsString() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.AttributeHolder#getAttribute(java
	 * .lang.String)
	 */
	public Attribute getAttribute(String name) {
		return attributes.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.AttributeHolder#putAttribute(com
	 * .isencia.passerelle.process.model.Attribute)
	 */
	public Attribute putAttribute(Attribute attribute) {
		return attributes.put(attribute.getName(), attribute);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.AttributeHolder#getAttributeNames()
	 */
	public Iterator<String> getAttributeNames() {
		return attributes.keySet().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.AttributeHolder#getAttributes()
	 */
	public Set<Attribute> getAttributes() {
		return new HashSet<Attribute>(attributes.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.Coloured#getColour()
	 */
	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.ResultItem#getUnit()
	 */
	public String getUnit() {
		return unit;
	}

	public Date getCreationTS() {
		Date resultTS = creationTS;
		if (resultTS == null && resultBlock != null) {
			resultTS = resultBlock.getCreationTS();
		}
		return resultTS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.ResultItem#getResultBlock()
	 */
	public ResultBlock getResultBlock() {
		return resultBlock;
	}
}
