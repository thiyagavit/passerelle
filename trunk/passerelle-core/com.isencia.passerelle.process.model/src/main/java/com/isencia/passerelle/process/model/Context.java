/* Copyright 2012 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.isencia.passerelle.process.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * @author erwin
 *
 */
public interface Context extends Identifiable, Serializable {
	
  /**
   * @return current status of this context
   */
  Status getStatus();
  
  void setStatus(Status status);

	/**
	 * 
	 * @return the request of which this context maintains the lifecycle info/status/results/...
	 */
	Request getRequest();
	
	/**
	 * Add a task that is being (has been) executed during the processing for this context.
	 * @param task
	 */
	void addTask(Task task);
	
	/**
	 * 
	 * @return
	 */
	List<Task> getTasks();
	
	void addEvent(ContextEvent e);

	/**
   * @return the list of all events that have happened in this context's lifecycle up-to "now"
   */
  List<ContextEvent> getEvents();
  
  /**
   * Store some named entry in the context.
   * This is a context-wide storage, not linked to a specific task
   * or its results.
   * 
   * @param name
   * @param value
   */
  void putEntry(String name, Serializable value);
  
  Serializable getEntryValue(String name);
  
  Iterator<String> getEntryNames();
  
	/**
	 * Retrieve the first item found with the given name in the context
	 * and in all linked task results.
	 * <p>
	 * Implementations should typically some prioritized lookup process
	 * in the complete Context structure. For example :
	 * <ul>
	 * <li>Check first in the context entries </li>
   * <li>Then in the result items of all task results, searching from most recent to oldest </li>
   * <li>Then, if still not found, check in the originally received request parameters </li>
	 * </ul>
	 * </p>
	 * @param name
	 * @return
	 */
	String lookupValue(String name);


  /**
   * Is the task still processing or not
   * 
   * @return
   */
  boolean isFinished();

	/**
	 * @return the creation date of the request
	 */
	Date getCreationTS();

  /**
   * @return the end time stamp
   */
  Date getEndTS();

  /**
   * @return The context's duration (in milliseconds)
   */
  Long getDuration();

	// methods to support fork/join
	void join(Context other);
	Context fork();
	boolean isForkedContext();
}
