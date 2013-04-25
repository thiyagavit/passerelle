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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Matcher;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;

/**
 * @author "puidir"
 * 
 */
public class ResultBlockImpl implements ResultBlock {

	private static final long serialVersionUID = 1L;

	private Long id;
	private TaskImpl task;
	private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
	private String colour;
	private Date creationTS;
	private String type;
	private Map<String, ResultItem<?>> resultItems = new HashMap<String, ResultItem<?>>();

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

	public Set<ResultItem> getResultItems() {
		return new HashSet<ResultItem>(resultItems.values());
	}

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

	private String discriminator;

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

  public Collection<ResultItem<?>> getMatchingItems(Matcher<ResultItem<?>> matcher) {
    // TODO Auto-generated method stub
    return null;
  }
  
  
}