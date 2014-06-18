package com.isencia.passerelle.process.service;

import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;

public interface ProcessPersister {
	/**
	 * closes a unit of work, committing or rolling back the transaction if one is active. 
	 */
	void close();
	Case getCase(Long id);
	ContextEvent getContextEvent(Request request, Long id);
	Request getRequest(Case caze, Long id);
	Task getTask(Request request, Long id);
	Task getTaskWithResults(Request request, Long id);
	/** 
	 * opens a unit of work, and starts a transaction if required.
	 */
	boolean open(boolean transactional);
	void persistAttributes(Request request);
	void persistCase(Case caze);
	void persistContextEvent(ContextEvent event);
	void persistRequest(Request request);
	void persistResultBlocks(ResultBlock... resultBlocks);
	void persistTask(Task task);
	void updateResultBlock(ResultBlock resultBlock);
	void updateStatus(Request request);
}
