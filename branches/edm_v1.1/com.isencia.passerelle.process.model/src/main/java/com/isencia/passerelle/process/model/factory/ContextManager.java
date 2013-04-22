/**
 * 
 */
package com.isencia.passerelle.process.model.factory;

import java.io.Serializable;
import java.util.Map;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextErrorEvent;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.Task;

/**
 * @author puidir
 *
 */
// TODO: can't we simplify this now that we have an enum for the status?
// TODO: should be moved to the model?
public interface ContextManager {

	/**
	 * 
	 * @return The type of task to create if nobody gives us a specific type
	 */
	Class<? extends Task> getDefaultTaskClass();

	/**
	 * Destroy any listeners
	 */
	void clear();
	
  /**
  * Creates a new event
  *
  * @param task
  * @param eventType
  * @param message
  * @return
  */
 Context notifyEvent(Context context, String eventType,String message);

 /**
	 * Notify all listeners about a given context event.
	 *  
	 * @param event
 * @return 
	 */
  ContextEvent notifyEvent(ContextEvent event);

	
	/**
	 * Notify listeners that the processing of the context has started.
	 *
	 * @param context Context that started
	 */
	Context notifyStarted(Context context);

	/**
	 * Notify listeners that the processing of the context has finished.
	 *
	 * @param context Context that finished
	 */
	Context notifyFinished(Context context);

	/**
	 * Notify listeners that the processing of the context has finished with an error.
	 *
	 * @param context Context that finished with an error
	 * @param error The error that happened during processing
	 */
	Context notifyError(Context context, Throwable error);
	
  /**
   * Notify listeners that the processing of the context has finished with an error.
   *
   * @param context Context that finished with an error
   * @param event The error event that happened during processing
   */
  Context notifyError(Context context, ContextErrorEvent event);

	/**
	 * Notify listeners that the processing of the context was cancelled.
	 * 
	 * @param context Context that got cancelled
	 */
	Context notifyCancelled(Context context);

	/**
	 * Notify listeners that the processing of the context has timed out.
	 *
	 * @param context Context that started
	 */
	Context notifyTimeOut(Context context);
	
	/**
	 * Notify listeners that the processing of the context has to be restarted.
	 *
	 * @param context Context that restarted
	 */
	Context notifyRestarted(Context context,Context taskContext);

	/**
	 * Notify listeners that the processing of the context
	 * for the given key is pending completion (has done its work but remains
	 * in 'ongoing' state until something else finishes it).
	 *
	 * @param entity
	 */
	Context notifyPendingCompletion(Context context);

	/**
	 * Subscribe the given callback to status change notifications of the given context
	 * 
	 * @param context Context for which status change notifications are posted
	 * @param callback Callback that will be notified
	 */
	void subscribe(Context context, ContextProcessingCallback callback);
	
	/**
	 * Subscribe the given callback to status change notifications for any/all context(s)
	 * being processed.
	 * 
	 * @param callback
	 */
	void subscribeAll(ContextProcessingCallback callback);
	
	/**
	 * Unsubscribe the given callback. It could be as well an "all" subscriber or
	 * one subscribed for a specific context...
	 * 
	 * @param callback
	 */
	void unsubscribe(ContextProcessingCallback callback);
	
	/**
	 * Check if the processing of the given context has finished.
	 *
	 * @param context Context to check
	 * @return Whether the context has one of the finished status values
	 */
	boolean isFinished(Context context);
	
	/**
	 * Create a task entity of the given type for the given flow context.
	 * 
	 * @param clazz Type of task to create
	 * @param flowContext Flow in which the task is created
	 * @param taskAttributes Task attributes
	 * @param owner Who created the task
	 * @return type What type of task to create
	 * @throws Exception If the task could not be created
	 */
	Context createTask(Class<? extends Task> taskClass, Context flowContext, Map<String, String> taskAttributes, Map<String, Serializable> taskContextEntries, String initiator, String type) throws Exception;

	/**
	 * Retrieve the context with the given identifier
	 * 
	 * @param id Context identifier
	 * @return The Context, null if not found
	 */
	Context getContext(Long id);

}
