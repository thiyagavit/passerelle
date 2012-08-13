/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Request;

/**
 * @author "puidir"
 * 
 */
@Entity
@Table(name = "PAS_CASE")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 50)
@DiscriminatorValue("CASE")
public class CaseImpl implements Case {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", nullable = false, unique = true, updatable = false)
	@GeneratedValue(generator = "pas_case")
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_TS", nullable = false, unique = false, updatable = false)
	private Date creationTS;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@Column(name = "EXTERNAL_REF", nullable = true, unique = false, updatable = true, length = 50)
	private String externalReference;

	@OneToMany(targetEntity = RequestImpl.class, mappedBy = "requestCase", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Request> requests = new ArrayList<Request>();

	public static final String _ID = "id";
	public static final String _REFERENCE = "id";
	public static final String _EXTERNAL_REFERENCE = "externalReference";

	public CaseImpl() {
		this.creationTS = new Date();
	}

	public CaseImpl(String externalReference) {
		this.creationTS = new Date();
		this.externalReference = externalReference;
	}

	public Long getId() {
		return id;
	}

	public Date getCreationTS() {
		return creationTS;
	}

	public String getExternalReference() {
		return externalReference;
	}

	public Collection<Request> getRequests() {
		// This avoids concurrent modifications
		List<Request> requestCopies = new ArrayList<Request>();
		requestCopies.addAll(requests);
		return requestCopies;
	}

	public void addRequest(Request request) {
		this.requests.add(request);
	}
}
