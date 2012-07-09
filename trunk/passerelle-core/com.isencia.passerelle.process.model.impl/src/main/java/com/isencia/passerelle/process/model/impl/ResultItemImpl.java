/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.io.Serializable;
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
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 50)
public class ResultItemImpl<V extends Serializable> implements ResultItem<V> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", nullable = false, unique = true, updatable = false)
	@GeneratedValue(generator = "pas_resultitem")
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_TS", nullable = true, unique = false, updatable = false)
	private Date creationTS;

	@Column(name = "NAME", nullable = false, unique = false, updatable = false, length = 512)
	private String name;

	@Column(name = "VALUE", nullable = false, unique = false, updatable = false, length = 4000)
	protected String value;
	
	@Column(name = "UNIT", nullable = true, unique = false, updatable = false, length = 512)
	private String unit;
	
	@OneToMany(targetEntity = ResultItemAttributeImpl.class, mappedBy = "resultItem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@MapKey(name = "name")
	private Map<String, AttributeImpl> attributes = new HashMap<String, AttributeImpl>();

	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter methods	
	@ManyToOne(targetEntity = ResultBlockImpl.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "RESULTBLOCK_ID")
	private ResultBlockImpl resultBlock;

	@Column(name = "COLOR", nullable = true, unique = false, updatable = true, length = 40)
	private String colour;

  public static final String _ID = "id";
  public static final String _NAME = "name";
  public static final String _VALUE = "valueAsString";
  public static final String _CREATION_TS = "creationTS";
  public static final String _UNIT = "unit";
  public static final String _DATA_TYPE = "dataType";
  public static final String _RESULT_BLOCK = "resultBlock";
	public static final String _RESULT_BLOCK_TYPE = "resultBlock.type";
  public static final String _COLOUR = "colour";
  
	public ResultItemImpl() {
	}
	
	protected ResultItemImpl(ResultBlock resultBlock, String name, String unit) {
		this.creationTS = new Date();
		
		this.resultBlock = (ResultBlockImpl)resultBlock;
		this.name = name;
		this.unit = unit;
		
		this.resultBlock.putItem(this);
	}
	
	protected ResultItemImpl(ResultBlock resultBlock, String name, String unit, Date creationTS) {
		this.creationTS = creationTS;
		
		this.resultBlock = (ResultBlockImpl)resultBlock;
		this.name = name;
		this.unit = unit;
		
		this.resultBlock.putItem(this);
	}
	
	public Long getId() {
		return id;
	}

	public Date getCreationTS() {
		return creationTS!=null ? creationTS : 
			(resultBlock!=null ? resultBlock.getCreationTS() : null);
	}

	public String getName() {
		return name;
	}

	public String getValueAsString() {
		return value;
	}

	public Attribute getAttribute(String name) {
		return attributes.get(name);
	}

	public Attribute putAttribute(Attribute attribute) {
		return attributes.put(attribute.getName(),(AttributeImpl) attribute);
	}

	public Iterator<String> getAttributeNames() {
		return attributes.keySet().iterator();
	}

	public Set<Attribute> getAttributes() {
		return new HashSet<Attribute>(attributes.values());
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}
	
	public String getUnit() {
		return unit;
	}

	public ResultBlock getResultBlock() {
		return resultBlock;
	}

	public V getValue() {
		// FIXME: this class used to be abstract so we didn't have empty implementations here
		return null;
	}

	public String getDataType() {
    // FIXME: this class used to be abstract so we didn't have empty implementations here
		return null;
	}
}
