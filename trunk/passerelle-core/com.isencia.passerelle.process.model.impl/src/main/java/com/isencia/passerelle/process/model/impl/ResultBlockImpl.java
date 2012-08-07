/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.Collection;
import java.util.Collections;
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
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;

/**
 * @author "puidir"
 * 
 */
@Entity
@Table(name = "PAS_RESULTBLOCK")
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 50)
@DiscriminatorValue("RESULTBLOCK")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ResultBlockImpl implements ResultBlock {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", nullable = false, unique = true, updatable = false)
	@GeneratedValue(generator = "pas_resultblock")
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter
	// methods
	@ManyToOne(targetEntity = TaskImpl.class, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "TASK_ID")
	private TaskImpl task;

	@OneToMany(targetEntity = ResultBlockAttributeImpl.class, mappedBy = "resultBlock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@MapKey(name = "name")
	private Map<String, Attribute> attributes = new HashMap<String, Attribute>();

	@Column(name = "COLOR", nullable = true, unique = false, updatable = true, length = 40)
	private String colour;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_TS", nullable = false, unique = false, updatable = false)
	private Date creationTS;

	@Column(name = "TYPE", nullable = false, unique = false, updatable = false, length = 512)
	private String type;

	@OneToMany(targetEntity = ResultItemImpl.class, mappedBy = "resultBlock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "RESULTBLOCK_ID")
	@MapKey(name = "name")
	private Map<String, ResultItem<?>> resultItems = new HashMap<String, ResultItem<?>>();

	public static final String _ID = "id";
	public static final String _CREATION_TS = "creationTS";
	public static final String _TYPE = "type";
	public static final String _RESULT_ITEMS = "allItems";
	public static final String _COLOUR = "colour";
	public static final String _ATTRIBUTES = "attributes";
	public static final String _DISCRIMINATOR = "discriminator";

	public ResultBlockImpl() {
	}

	public ResultBlockImpl(Task task, String type) {
		this.creationTS = new Date();
		this.task = (TaskImpl) task;
		this.type = type;

		this.task.addResultBlock(this);
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
	@OneToMany(mappedBy = "resultBlock", targetEntity = ResultBlockAttributeImpl.class)
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
	 * @see com.isencia.passerelle.process.model.ResultBlock#getCreationTS()
	 */
	public Date getCreationTS() {
		return creationTS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.ResultBlock#getType()
	 */
	public String getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.ResultBlock#addItem(com.isencia.
	 * passerelle.process.model.ResultItem)
	 */
	public ResultItem<?> putItem(ResultItem<?> item) {
		return resultItems.put(item.getName(), item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.ResultBlock#getAllItems()
	 */
	public Collection<ResultItem<?>> getAllItems() {
		return Collections.unmodifiableCollection(resultItems.values());
	}

	@OneToMany(mappedBy = "resultBlock", targetEntity = ResultItemImpl.class)
	public Set<ResultItem> getResultItems() {
		return new HashSet<ResultItem>(resultItems.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.ResultBlock#getItemForName(java.
	 * lang.String)
	 */
	public ResultItem<?> getItemForName(String name) {
		return resultItems.get(name);
	}

	public Task getTask() {
		return task;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Resultblock [id=");
		builder.append(id);
		if (type != null) {
			builder.append(", type=");
			builder.append(type);
		}
		if (colour != null) {
			builder.append(", colour=");
			builder.append(getColour());
		}
		builder.append("]");
		return builder.toString();
	}

	@SuppressWarnings("unused")
	@Column(name = "DTYPE", updatable = false)
	private String discriminator;

	@SuppressWarnings("all")
	public int hashCode() {
		return new HashCodeBuilder(31, 71).append(id).append(type).toHashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof ResultBlockImpl)) {
			return false;
		}
		ResultBlockImpl rhs = (ResultBlockImpl) arg0;
		return new EqualsBuilder().append(this.id, rhs.id).append(this.type, rhs.type).isEquals();
	}
}
