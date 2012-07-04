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
import com.isencia.passerelle.process.model.Request;

/**
 * @author "puidir"
 *
 */
@Entity
@Table(name = "PAS_REQUESTATTRIBUTE")
public class RequestAttributeImpl implements Attribute, Comparable<RequestAttributeImpl> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(generator = "pas_requestattribute")
	private Long id;
	
	@Version
	private int version;
	
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "VALUE")
	private String value;
	
	@ManyToOne(targetEntity = RequestImpl.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "REQUEST_ID")
	private RequestImpl request;
	
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

	/**
	 * @return the request
	 */
	public Request getRequest() {
		return request;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(RequestAttributeImpl rhs) {
		return new CompareToBuilder()
			.append(id, rhs.id)
			.append(version, rhs.version).toComparison();
	}
	
	
}
