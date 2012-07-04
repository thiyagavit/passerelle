/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.CompareToBuilder;

import com.isencia.passerelle.process.model.ResultItem;

/**
 * @author "puidir"
 *
 */
@Entity
@Table(name = "PAS_RESULTITEMATTRIBUTE")
public class ResultItemAttributeImpl extends AttributeImpl implements Comparable<ResultItemAttributeImpl> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(generator = "pas_resultitemattribute")
	private Long id;
	
	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter methods	
	@ManyToOne(targetEntity = ResultItemImpl.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "RESULTITEM_ID")
	private ResultItemImpl<?> resultItem;
	
	public ResultItemAttributeImpl() {
	}
	
	public ResultItemAttributeImpl(ResultItem<?> resultItem, String name, String value) {
		super(name, value);
		this.resultItem = (ResultItemImpl<?>)resultItem;
		
		this.resultItem.putAttribute(this);
	}
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
	}

	public ResultItem<?> getResultItem() {
		return resultItem;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ResultItemAttributeImpl rhs) {
		return new CompareToBuilder()
			.append(id, rhs.id)
			.append(version, rhs.version).toComparison();
	}
	
	
}
