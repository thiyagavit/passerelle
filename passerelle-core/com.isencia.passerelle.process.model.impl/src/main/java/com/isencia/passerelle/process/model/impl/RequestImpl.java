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

	@Id
	@Column(name = "ID", nullable = false, unique = true, updatable = false)
	@GeneratedValue(generator = "pas_request")
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;
	
	@OneToMany(targetEntity = RequestAttributeImpl.class, mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@MapKey(name = "name")
	private Map<String, Attribute> requestAttributes = new HashMap<String, Attribute>();
	
	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter methods
	// Remark: Cannot use optional = false here since we made TaskImpl extend from RequestImpl
	@ManyToOne(targetEntity = CaseImpl.class, optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "CASE_ID")
	private CaseImpl requestCase;
	
	@Column(name = "CORRELATION_ID", nullable = true, unique = false, updatable = true)
	private String correlationId;

	@Column(name = "TYPE", nullable = false, unique = false, updatable = false)
	private String type;

	@OneToOne(targetEntity = ContextImpl.class, optional = false, mappedBy = "request", cascade = {
		CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
	private Context processingContext;

	@Column(name = "OWNER", nullable = false, unique = false, updatable = false)
	private String owner;

  public static final String _OWNER = "owner";
  public static final String _ID = "id";
  public static final String _ATTRIBUTES = "attributes";
  public static final String _CASE = "case";
  public static final String _CORRELATION_ID = "correlationId";
  public static final String _TYPE = "type";
  public static final String _CONTEXT = "processingContext";
  public static final String _REFERENCE = "case.id";
  public static final String _TASKS = "processingContext.tasks";
  public static final String _EVENTS = "processingContext.events";
  public static final String _CREATION_TS = "processingContext.creationTS";
  public static final String _END_TS = "processingContext.endTS";
  public static final String _STATUS = "processingContext.status";
  public static final String _DURATION = "processingContext.durationInMillis";

	public RequestImpl() {
	}

	public RequestImpl(String type, String owner) {
		this.owner = owner;
		this.processingContext = new ContextImpl(this);
		this.type = type;
	}
	
	public RequestImpl(String type, String owner, String correlationId) {
		this(type, owner);
		this.correlationId = correlationId;
	}
	
	public RequestImpl(Case requestCase, String type, String owner) {
		this(type, owner);
		this.requestCase = (CaseImpl)requestCase;
		
		this.requestCase.addRequest(this);
	}
	
	public RequestImpl(Case requestCase, String type, String owner, String correlationId) {
		this(requestCase, type, owner);
		this.correlationId = correlationId;
	}
	
	public Long getId() {
		return id;
	}

	public String getOwner() {
		return owner;
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

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
	
	public String getType() {
		return type;
	}

	public Context getProcessingContext() {
		return processingContext;
	}

}
