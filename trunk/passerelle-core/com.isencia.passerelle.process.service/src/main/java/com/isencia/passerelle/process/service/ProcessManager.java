package com.isencia.passerelle.process.service;

import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.Task;

public interface ProcessManager {
	Request getRequest();
	String getId();
	/**
	 * Notify listeners that the processing of the request has started.
	 */
	void notifyStarted();
	/**
	 * Notify listeners that the processing of a task has started.
	 *
	 * @param task Task that started
	 */
	void notifyStarted(Task task);
	/**
	 * Notify listeners that the processing of the request has finished.
	 */
	void notifyFinished();
	/**
	 * Notify listeners that the processing of a task has finished.
	 *
	 * @param task Task that finished
	 */
	void notifyFinished(Task task);
}
