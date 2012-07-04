package com.isencia.passerelle.process.model.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;

@Entity
@Table(name = "PAS_REQUEST")
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("REQUEST")
public class RequestImpl implements Request {

	private static final long serialVersionUID = 1L;

	@Column(name = "ID")
	@Id
	@GeneratedValue(generator = "pas_request")
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;
	
	@OneToMany(targetEntity = RequestAttributeImpl.class, mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@MapKey(name = "name")
	private Map<String, Attribute> requestAttributes = new HashMap<String, Attribute>();
	
	@ManyToOne(targetEntity = CaseImpl.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "CASE_ID")
	private Case requestCase;
	
	@Column(name = "CORRELATION_ID")
	private String correlationId;

	@Column(name = "TYPE")
	private String type;

	@OneToOne(targetEntity = ContextImpl.class, optional = false, mappedBy = "request", cascade = CascadeType.ALL)
	private Context context;

	public RequestImpl() {
	}
	
	public Long getId() {
		return id;
	}

	public Attribute getAttribute(String name) {
		return requestAttributes.get(name);
	}

	public Attribute putAttribute(Attribute attribute) {
		return requestAttributes.put(attribute.getName(), attribute);
	}

	public Iterator<String> getAttributeNames() {
		return requestAttributes.keySet().iterator();
	}

	public Set<Attribute> getAttributes() {
		return new HashSet<Attribute>(requestAttributes.values());
	}

	public Case getCase() {
		return requestCase;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public String getType() {
		return type;
	}

	public Context getProcessingContext() {
		return context;
	}

}
