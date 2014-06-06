/**
 * 
 */
package com.isencia.passerelle.process.model.factory;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Task;

/**
 * @author puidir
 *
 */
public interface ContextManager {
  
  String REPORT_EVENT_TYPE = "REPORTED";

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
	 * Notify listeners that the processing of the request has started.
	 *
	 * @param request Request that started
	 */
	void notifyStarted();
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
  Context notifyError(Context context, ErrorItem event, Throwable cause);

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
	 * Persist an event to indicate that some kind of reporting was done on this context.
	 * A report event can be signaled even when the context is finished.
	 * @param ctx Context reported on
	 * @return refreshed context
	 */
	Context notifyReportEvent(Context ctx);

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
	 * Retrieve the context with the given identifier
	 * 
	 * @param id Context identifier
	 * @return The Context, null if not found
	 */
	Context getContext(Long id);

}
