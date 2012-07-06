package com.isencia.passerelle.process.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;

@Entity
@DiscriminatorValue("TASK")
public class TaskImpl extends RequestImpl implements Task {

	private static final long serialVersionUID = 1L;

	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter methods	
	@ManyToOne(targetEntity = ContextImpl.class, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_CONTEXT_ID", nullable = false, updatable = true)
	private ContextImpl parentContext;

	@OneToMany(targetEntity = ResultBlockImpl.class, mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<ResultBlock> resultBlocks = new HashSet<ResultBlock>();

  public static final String _PARENT_CONTEXT = "parentContext";
  public static final String _RESULT_BLOCKS = "resultBlocks";
  public static final String _RESULT_ITEMS = "resultItems";
  
	public TaskImpl() {
	}
	
	public TaskImpl(Context parentContext, String initiator, String type) {
		super(initiator, type);
		this.parentContext = (ContextImpl)parentContext;
		
		this.parentContext.addTask(this);
	}
	
	public Context getParentContext() {
		return parentContext;
	}

	public boolean addResultBlock(ResultBlock block) {
		return resultBlocks.add(block);
	}

	public Collection<ResultBlock> getResultBlocks() {
		return Collections.unmodifiableSet(resultBlocks);
	}

	@OneToMany(targetEntity = ResultItemImpl.class, mappedBy = "resultBlock.task")
	public Set<ResultItem> getResultItems() {
		Set<ResultItem> items = new HashSet<ResultItem>();
		Collection<ResultBlock> blocks = getResultBlocks();
		for (ResultBlock block : blocks) {
	    items.addAll(block.getAllItems());
    }
		return items;
	}
}
