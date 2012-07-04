/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Request;

/**
 * @author "puidir"
 *
 */
@Entity
@Table(name = "PAS_CASE")
public class CaseImpl implements Case {

	private static final long serialVersionUID = 1L;

	@Column(name = "ID")
	@Id
	@GeneratedValue(generator = "pas_request")
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@Column(name = "EXTERNAL_REF")
	private String externalReference;
	
	@OneToMany(targetEntity = RequestImpl.class, mappedBy = "requestCase", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Request> requests = new ArrayList<Request>();
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Case#getExternalReference()
	 */
	public String getExternalReference() {
		return externalReference;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Case#getRequests()
	 */
	public Collection<Request> getRequests() {
		return Collections.unmodifiableCollection(requests);
	}

}
