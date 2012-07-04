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

import com.isencia.passerelle.process.model.Request;

/**
 * @author "puidir"
 *
 */
@Entity
@Table(name = "PAS_REQUESTATTRIBUTE")
public class RequestAttributeImpl extends AttributeImpl implements Comparable<RequestAttributeImpl> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(generator = "pas_requestattribute")
	private Long id;
	
	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter methods	
	@ManyToOne(targetEntity = RequestImpl.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "REQUEST_ID")
	private RequestImpl request;
	
	public RequestAttributeImpl() {
	}
	
	public RequestAttributeImpl(Request request, String name, String value) {
		super(name, value);
		this.request = (RequestImpl)request;
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
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
