/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import com.isencia.passerelle.process.model.Identifiable;

/**
 * @author "puidir"
 *
 */
@Entity
@Table(name = "PAS_CLOBITEM")
public class ClobItem implements Identifiable {

	@Id
	@Column(name = "ID")
	@GeneratedValue(generator = "pas_clobitem")
	private Long id;
	
	@SuppressWarnings("unused")
	@Version
	private int version;
	
	@Column(name = "CLOBDATA", nullable = false, updatable = false)
	@Lob
	private String value;
	
	public ClobItem() {	
	}
	
	public ClobItem(String value) {
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
}
