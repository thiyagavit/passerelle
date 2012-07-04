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
import javax.persistence.Version;

import org.apache.commons.lang.builder.CompareToBuilder;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.ResultBlock;

/**
 * @author "puidir"
 *
 */
@Entity
@Table(name = "PAS_RESULTBLOCKATTRIBUTE")
public class ResultBlockAttributeImpl implements Attribute, Comparable<ResultBlockAttributeImpl> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(generator = "pas_resultblockattribute")
	private Long id;
	
	@Version
	private int version;
	
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "VALUE")
	private String value;
	
	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter methods	
	@ManyToOne(targetEntity = ResultBlockImpl.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "RESULTBLOCK_ID")
	private ResultBlockImpl resultBlock;
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
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
		return value;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValueAsString()
	 */
	public String getValueAsString() {
		return value;
	}
	
	public ResultBlock getResultBlock() {
		return resultBlock;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ResultBlockAttributeImpl rhs) {
		return new CompareToBuilder()
			.append(id, rhs.id)
			.append(version, rhs.version).toComparison();
	}
	
	
}
