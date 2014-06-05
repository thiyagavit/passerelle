package com.isencia.passerelle.process.service;

import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;

public interface ProcessPersistenceService {
	Case getCase(Long id);
	Request getRequest(Case caze, Long id);
	Task getTask(Request request, Long id);
	Task getTaskWithResults(Request request, Long id);
	void persistAttributes(Request request);
	void persistCase(Case caze);
	void persistRequest(Request request);
	void persistResultBlocks(ResultBlock... resultBlocks);
	void persistTask(Task task);
	void updateResultBlock(ResultBlock resultBlock);
	void updateStatus(Request request);
}
