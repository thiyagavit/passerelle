/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.lang.builder.CompareToBuilder;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextEvent;

/**
 * @author "puidir"
 *
 */
@Entity
@Table(name = "PAS_EVENT")
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 50)
@DiscriminatorValue("CONTEXTEVENT")
public class ContextEventImpl implements ContextEvent {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ID", nullable = false, unique = true, updatable = false)
	@GeneratedValue(generator = "pas_contextevent")
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;
	
	@Column(name = "TOPIC", nullable = false, unique = false, updatable = false, length = 255)
	private String topic;
	
	@Column(name = "MESSAGE", nullable = true, unique = false, updatable = false, length = 2000)
	private String message;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_TS", nullable = false, unique = false, updatable = false)
	private Date creationTS;

	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter methods	
	@ManyToOne(targetEntity = ContextImpl.class, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "CONTEXT_ID", nullable = true, updatable = true)
	private ContextImpl context;

  public static final String _ID = "id";
  public static final String _TOPIC = "topic";
  public static final String _CREATION_TS = "creationTS";
  public static final String _DURATION = "duration";
  public static final String _CONTEXT = "context";
  public static final String _MESSAGE = "message";

	public ContextEventImpl() {
	}
	
	public ContextEventImpl(Context context, String topic) {
		this.creationTS = new Date();
		this.context = (ContextImpl)context;
		this.topic = topic;

		this.context.addEvent(this);
	}
	
	public ContextEventImpl(Context context, String topic, String message) {
		this(context, topic);
		this.message = message;
	}
	
	public Long getId() {
		return id;
	}

	public String getTopic() {
		return topic;
	}

	public String getMessage() {
		return message;
	}

	public Date getCreationTS() {
		return creationTS;
	}

	public Long getDuration() {
		// Irrelevant
		return 0L;
	}

	public Context getContext() {
		return context;
	}

	public int compareTo(ContextEvent rhs) {
		ContextEventImpl rhsImpl = (ContextEventImpl)rhs;
		return new CompareToBuilder()
			.append(creationTS, rhsImpl.creationTS)
			.append(topic, rhsImpl.topic).toComparison();
	}

}
