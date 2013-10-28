package com.isencia.passerelle.process.model.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;

@Entity
@Table(name = "PAS_REQUEST")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 50)
@DiscriminatorValue("REQUEST")
public class RequestImpl implements Request {

  public void setId(Long id) {
    this.id = id;
  }

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "ID", nullable = false, unique = true, updatable = false)
  @GeneratedValue(generator = "pas_request")
  private Long id;

  @SuppressWarnings("unused")
  @Version
  private int version;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATION_TS", nullable = false, unique = false, updatable = false)
  private Date creationTS;

  @OneToMany(targetEntity = RequestAttributeImpl.class, mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @MapKey(name = "name")
  private Map<String, Attribute> requestAttributes = new HashMap<String, Attribute>();

  // Remark: need to use the implementation class instead of the interface
  // here to ensure jpa implementations like EclipseLink will generate setter
  // methods
  // Remark: Cannot use optional = false here since we made TaskImpl extend
  // from RequestImpl
  @ManyToOne(targetEntity = CaseImpl.class, optional = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "CASE_ID")
  private CaseImpl requestCase;

  @Column(name = "CORRELATION_ID", nullable = true, unique = false, updatable = true, length = 250)
  private String correlationId;

  @Column(name = "TYPE", nullable = false, unique = false, updatable = false, length = 250)
  private String type;

  @OneToOne(targetEntity = ContextImpl.class, optional = false, mappedBy = "request", cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
  private Context processingContext;

  @Column(name = "INITIATOR", nullable = false, unique = false, updatable = false, length = 250)
  private String initiator;

  @Column(name = "EXECUTOR", nullable = true, unique = false, updatable = true, length = 250)
  private String executor;

  @Column(name = "CATEGORY", nullable = true, unique = false, updatable = true, length = 250)
  private String category;

  @SuppressWarnings("unused")
  @Column(name = "DTYPE", updatable = false)
  private String discriminator;

  public static final String _DISCRIMINATOR = "discriminator";
  public static final String _INITIATOR = "initiator";
  public static final String _EXECUTOR = "executor";
  public static final String _ID = "id";
  public static final String _ATTRIBUTES = "attributes";
  public static final String _CASE = "case";
  public static final String _CORRELATION_ID = "correlationId";
  public static final String _TYPE = "type";
  public static final String _CATEGORY = "category";
  public static final String _CONTEXT = "processingContext";
  public static final String _REFERENCE = "requestCase.id";
  public static final String _TASKS = "processingContext.tasks";
  public static final String _EVENTS = "processingContext.events";
  public static final String _CREATION_TS = "creationTS";
  public static final String _END_TS = "processingContext.endTS";
  public static final String _STATUS = "processingContext.status";
  public static final String _DURATION = "processingContext.durationInMillis";

  public RequestImpl() {
  }

  public RequestImpl(String initiator, String type) {
    this.creationTS = new Date();
    if (initiator == null) {
      this.initiator = "unknown";
    } else {
      this.initiator = initiator;
    }
    this.processingContext = new ContextImpl(this);
    this.type = type;
  }

  public RequestImpl(String initiator, String type, String correlationId) {
    this(initiator, type);
    this.correlationId = correlationId;
  }

  public RequestImpl(Case requestCase, String initiator, String type) {
    this(initiator, type);
    this.requestCase = (CaseImpl) requestCase;

    this.requestCase.addRequest(this);
  }

  public RequestImpl(Case requestCase, String initiator, String type, String correlationId) {
    this(requestCase, initiator, type);
    this.correlationId = correlationId;
  }

  public RequestImpl(Case requestCase, String initiator, String type, String correlationId, String category) {
    this(requestCase, initiator, type);
    this.correlationId = correlationId;
    this.category = category;
  }

  public RequestImpl(Case requestCase, String initiator, String executor, String type, String correlationId, String category) {
    this(requestCase, initiator, type, correlationId, category);
    this.correlationId = correlationId;
    this.category = category;
    this.executor = executor;
  }

  public Long getId() {
    return id;
  }

  public Date getCreationTS() {
    return creationTS;
  }

  public String getInitiator() {
    return initiator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.isencia.passerelle.process.model.Request#getExecutor()
   */
  public String getExecutor() {
    return executor;
  }

  public void setExecutor(String executor) {
    this.executor = executor;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Attribute getAttribute(String name) {
    // Remark: can't use a CaseInsensitiveMap here because of lazy loading
    for (String attrName : requestAttributes.keySet()) {
      if (attrName.equalsIgnoreCase(name)) {
        return requestAttributes.get(attrName);
      }
    }
    return null;
  }

  public String getAttributeValue(String name) {
    Attribute attribute = getAttribute(name);
    if (attribute == null) {
      return null;
    }
    return attribute.getValueAsString();
  }

  public Attribute putAttribute(Attribute attribute) {
    return requestAttributes.put(attribute.getName(), attribute);
  }

  public Iterator<String> getAttributeNames() {
    return requestAttributes.keySet().iterator();
  }

  @OneToMany(mappedBy = "request", targetEntity = RequestAttributeImpl.class)
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
