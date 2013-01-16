/**
 * 
 */
package com.isencia.passerelle.process.service;

import java.io.Serializable;
import java.util.Map;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Task;

/**
 * @author puidir
 *
 */
public interface Service {

	/**
	 * @return The name of the service
	 */
	String getName();
	
	/**
	 * @return Name of the preferences node where configuration for this service can be retrieved
	 */
	String getConfigurationNodeName();
	
	/**
	 * Perform service setup
	 * @throws ServiceException
	 */
	void setup() throws ServiceException;
	
	/**
	 * Clean up when the service is finishing
	 * @throws ServiceException
	 */
	void wrapup() throws ServiceException;
	
	/**
	 * Handle a task
	 * 
	 * @param flowContext The flow context in which this task will be performed
	 * @param taskAttributes Attributes for the task
	 * @return The task's context
	 * 
	 * @throws ServiceException
	 */
	Context handleTask(Class<? extends Task> taskClass, Context flowContext, Map<String, String> taskAttributes, Map<String, Serializable> taskContextEntries, String initiator, String type) throws ServiceException;
}
