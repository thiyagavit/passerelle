/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@Table(name = "PAS_RESULTBLOCK")
public class ResultBlockImpl implements ResultBlock {

	private static final long serialVersionUID = 1L;

	@Column(name = "ID")
	@Id
	@GeneratedValue(generator = "pas_resultblock")
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@OneToMany(targetEntity = ResultBlockAttributeImpl.class, mappedBy = "resultBlock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@MapKey(name = "name")
	private Map<String, Attribute> attributes = new HashMap<String, Attribute>();

	@Column(name = "COLOUR", nullable = true)
	private String colour;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_TS", updatable = false)
	private Date creationTS;

	@Column(name = "TYPE", updatable = false)
	private String type;

	@OneToMany(targetEntity = ResultItemImpl.class, mappedBy = "resultBlock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "RESULTBLOCK_ID")
	@MapKey(name = "name")
	private Map<String, ResultItem<?>> resultItems = new HashMap<String, ResultItem<?>>();

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.AttributeHolder#getAttribute(java.lang.String)
	 */
	public Attribute getAttribute(String name) {
		return attributes.get(name);
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.AttributeHolder#putAttribute(com.isencia.passerelle.process.model.Attribute)
	 */
	public Attribute putAttribute(Attribute attribute) {
		return attributes.put(attribute.getName(), attribute);
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.AttributeHolder#getAttributeNames()
	 */
	public Iterator<String> getAttributeNames() {
		return attributes.keySet().iterator();
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.AttributeHolder#getAttributes()
	 */
	public Set<Attribute> getAttributes() {
		return new HashSet<Attribute>(attributes.values());
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Coloured#getColour()
	 */
	public String getColour() {
		return colour;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultBlock#getCreationTS()
	 */
	public Date getCreationTS() {
		return creationTS;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultBlock#getType()
	 */
	public String getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultBlock#addItem(com.isencia.passerelle.process.model.ResultItem)
	 */
	public ResultItem<?> putItem(ResultItem<?> item) {
		return resultItems.put(item.getName(), item);
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultBlock#getAllItems()
	 */
	public Collection<ResultItem<?>> getAllItems() {
		return Collections.unmodifiableCollection(resultItems.values());
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultBlock#getAllItems(java.util.Comparator)
	 */
	public SortedSet<ResultItem<?>> getAllItems(Comparator<ResultItem<?>> comparator) {
		// TODO Auto-generated method stub
		// TODO: ask erwin for what this will be used ... returns null in edm now
		return null;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultBlock#getItemForName(java.lang.String)
	 */
	public ResultItem<?> getItemForName(String name) {
		return resultItems.get(name);
	}

}
