package com.isencia.passerelle.process.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;

@Entity
@DiscriminatorValue("TASK")
public class TaskImpl extends RequestImpl implements Task {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ContextImpl.class, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_CONTEXT_ID", nullable = false, updatable = true)
	private ContextImpl parentContext;

	@OneToMany(targetEntity = ResultBlockImpl.class, mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<ResultBlock> resultBlocks = new HashSet<ResultBlock>();

	@Column(name = "OWNER")
	private String owner;

	public TaskImpl() {
	}
	
	public Context getParentContext() {
		return parentContext;
	}

	public String getOwner() {
		return owner;
	}

	public boolean addResultBlock(ResultBlock block) {
		return resultBlocks.add(block);
	}

	public Collection<ResultBlock> getResultBlocks() {
		return Collections.unmodifiableSet(resultBlocks);
	}

}
