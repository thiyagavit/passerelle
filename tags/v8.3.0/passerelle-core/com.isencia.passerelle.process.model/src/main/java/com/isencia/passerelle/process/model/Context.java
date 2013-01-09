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
 * A <code>Context</code> maintains all status information and executed work for a certain <code>Request</code>.
 * <p>
 * Besides these main responsibilities, it also offers a storage for named "context entries".
 * This storage can be used to maintain context-wide data, i.e. that can be shared across tasks etc.
 * <br/>
 * Remark that this is typically a transient storage, whereas the "main" elements like <code>Task</code>s
 * and their <code>ResultBlock</code>s, <code>Attribute</code>s etc are often persisted.
 * </p>
 * @author erwin
 *
 */
public interface Context extends Identifiable, Serializable {
	
  /**
   * @return current status of this context
   */
  Status getStatus();
  
  /**
   * Set the new status of the context.
   * There is currently no formally enforced state transition model.
   * The only assumption is that once a <code>Context</code> has been set to a "final" state
   * (cfr <code>Status.isFinalStatus()</code>) the setter will fail if any more state change is attempted.
   * 
   * @param status
   * @return true if the state was successfully set, false if not
   * @see Status
   */
  boolean setStatus(Status status);

	/**
	 * 
	 * @return the request of which this context maintains the lifecycle info/status/results/...
	 */
	Request getRequest();
	
	/**
	 * 
	 * @return a read-only list of all associated tasks
	 */
	List<Task> getTasks();

	/**
   * @return the list of all events that have happened in this context's lifecycle up-to "now"
   */
  List<ContextEvent> getEvents();
  
  /**
   * 
   * @return the list of errors that happened during the processing of the associated request.
   * This also includes the errors for any subtask executed in the this context.
   */
  List<ErrorItem> getErrors();
  
  /**
   * Store some named entry in the context.
   * This is a context-wide storage, not linked to a specific task
   * or its results.
   * 
   * @param name
   * @param value
   */
  void putEntry(String name, Serializable value);

  /**
   * Remove the entry with the given name
   * @param name
   * @return the entry that was present for the given name (or null if none was there)
   */
  Serializable removeEntry(String name);

  /**
   * 
   * @param name
   * @return the entry stored under the given name in the context, or null if not present.
   */
  Serializable getEntryValue(String name);
  
  /**
   * 
   * @return the names of all stored context entries
   */
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
	 * Retrieve the first item found with the given name in the context and in all linked task result, 
	 * limited to result block with the given data type
	 * @param dataType
	 * @param name
	 * @return
	 */
	String lookupValue(String dataType, String name);

  /**
   * Is the request still processing or not
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
   * @return The context's duration (in milliseconds). 
   * It is assumed that this returns the time since the creationTS and until the endTS for finished contexts.
   * For non-finished contexts, this should return null.
   */
  Long getDurationInMillis();

	// methods to support fork/join
  /**
   * To reliably support fork/join semantics in Passerelle flows, forked flow branches should work in isolation of each other,
   * until the join. This can be achieved by sending individual forked <code>Context</code>s across each branch.
   * 
   * @return a forked context
   */
	Context fork();
	
	/**
	 * 
	 * @param other
	 */
	void join(Context other);
	
	/**
	 * 
	 * @return an indicator whether this is a forked context or a "root" context, i.e. outside of any fork/join scope.
	 */
	boolean isForkedContext();
	
	/**
	 * Remove all tasks to minimize memory usage.
	 * 
	 * @return this context without any stored tasks
	 */
	Context minimize();
	
	/**
   * Restore all tasks in memory, if the context has previously been minimized.
   * 
   * @return this context without any stored tasks
	 */
	Context restore();
	
	/**
	 * 
	 * @return whether the context is minimized
	 */
	boolean isMinimized();
}
