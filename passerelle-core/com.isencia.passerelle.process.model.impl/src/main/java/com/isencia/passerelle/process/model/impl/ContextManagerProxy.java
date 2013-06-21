/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.io.Serializable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.ContextManager;

/**
 * @author puidir
 * 
 */
public class ContextManagerProxy extends ServiceTracker {

  private static ContextManager delegate;

  /**
   * @param context
   * @param filter
   */
  public ContextManagerProxy(BundleContext context) {
    super(context, ContextManager.class.getName(), null);
  }

  @Override
  public Object addingService(ServiceReference reference) {

    delegate = (ContextManager) super.addingService(reference);

    return delegate;
  }

  @Override
  public void removedService(ServiceReference reference, Object service) {
    super.removedService(reference, service);
    delegate = null;
  }

  public static Class<? extends Task> getDefaultTaskClass() {
    return delegate.getDefaultTaskClass();
  }

  public static void subscribeEvents(Context context, ContextProcessingCallback callback) {
    delegate.subscribe(context, callback);
  }

  /**
   * subscribe to lifecycle events of all contexts being processed
   * 
   * @param callback
   */
  public static void subscribeEvents(ContextProcessingCallback callback) {
    delegate.subscribeAll(callback);
  }

  public static void unsubscribeEvents(ContextProcessingCallback callback) {
    delegate.unsubscribe(callback);
  }

  public static boolean isFinished(Context context) {
    return delegate.isFinished(context);
  }

  public static Context notifyStarted(final Context context) {
    return delegate.notifyStarted(context);
  }

  public static Context notifyError(Context context, Throwable cause) {
    return delegate.notifyError(context, cause);
  }

  public static Context notifyError(Context context, ErrorItem error) {
    return delegate.notifyError(context, error);
  }

  public static Context notifyRestarted(Context requestContext, Context taskContext) {
    return delegate.notifyRestarted(requestContext, taskContext);
  }

  public static Context notifyFinished(Context context) {
    return delegate.notifyFinished(context);
  }

  public static Context notifyEvent(Context context, String eventType, String message) {
    return delegate.notifyEvent(context, eventType, message);
  }

  public static void notifyEvent(ContextEvent event) {
    delegate.notifyEvent(event);
  }

  public static Context notifyCancelled(Context context) {
    return delegate.notifyCancelled(context);
  }

  public static Context notifyTimeOut(Context context) {
    return delegate.notifyTimeOut(context);
  }

  public static Context notifyPendingCompletion(final Context context) {
    return delegate.notifyPendingCompletion(context);
  }

  public static Context createTask(Class<? extends Task> taskClass, Context flowContext, Map<String, String> taskAttributes, Map<String, Serializable> taskContextEntries,
      String initiator, String type) throws Exception {
    return delegate.createTask(taskClass, flowContext, taskAttributes, taskContextEntries, initiator, type);
  }

  public static Context getLifeCycleEntity(Long id) {
    return delegate.getContext(id);
  }
}
