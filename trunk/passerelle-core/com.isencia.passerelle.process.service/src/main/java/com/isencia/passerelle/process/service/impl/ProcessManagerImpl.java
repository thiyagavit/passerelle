package com.isencia.passerelle.process.service.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerService;
import com.isencia.passerelle.process.service.ProcessPersister;
import com.isencia.passerelle.runtime.ProcessHandle;

public class ProcessManagerImpl implements ProcessManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessManagerImpl.class);
	
	private ProcessManagerService service;
	private final ProcessHandle handle;
	private Request request;

	public ProcessManagerImpl(ProcessManagerService service, Request request) {
		this(service,new ProcessHandleImpl(),request);
	}
	
	public ProcessManagerImpl(ProcessManagerService service, ProcessHandle handle, Request request) {
		this.service = service;
		this.handle = handle;
		this.request = request;
		this.request.getProcessingContext().setProcessId(handle.getProcessId());
	}
	
	@Override
	public String getId() {
	  return getHandle().getProcessId();
	}
	
	@Override
	public ProcessFactory getFactory() {
		return(service.getFactory());
	}
	
	@Override
	public ProcessHandle getHandle() {
		return(handle);
	}
	
	@Override
	public ProcessPersister getPersister() {
		return(service.getPersister());
	}
	
	public Request getRequest() {
		return(request);
	}
	
	@Override
	public Task getTask(long id) {
		for (Task task : request.getProcessingContext().getTasks())
			if (task.getId().longValue() == id)
				return(task);

		boolean shouldClose = getPersister().open(false);
		Task task = getPersister().getTask(request,id);
		if (shouldClose)
			getPersister().close();
		
		return task;
	}
	
	@Override
	public void notifyCancelled() {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyCancelled(Task task) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyError(ErrorItem error, Throwable cause) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyError(Task task, ErrorItem error, Throwable cause) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyError(Task task, Throwable error) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyError(Throwable error) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyEvent(ContextEvent event) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyEvent(String eventType, String message) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyEvent(Task task, String eventType, String message) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyFinished() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void notifyFinished(Task task) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyPendingCompletion() {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyPendingCompletion(Task task) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void notifyRestarted(Task task) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void notifyStarted() {
		// TODO Auto-generated method stub		
	}
	
	@Override
	public void notifyStarted(Task task) {
		// TODO Auto-generated method stub		
	}
	
	@Override
	public void notifyTimeOut() {
		// TODO Auto-generated method stub		
	}
	
	@Override
	public void notifyTimeOut(Task task) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void subscribe(Task task, ContextProcessingCallback callback) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void subscribeAll(ContextProcessingCallback callback) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void unsubscribe(ContextProcessingCallback callback) {
		// TODO Auto-generated method stub	
	}

	@Override
	public boolean pause() {
		try {
			FlowManager.getDefault().pauseExecution(handle.getFlowHandle().getFlow());
			return(true);
		} catch (Exception e) {
			LOGGER.error("Exception while pausing flow.", e);
			return(false);
		}
	}

	@Override
	public boolean restart(long taskId, long timeOut, TimeUnit timeOutUnit) {
		Task task = null;
		for (Task t : request.getProcessingContext().getTasks()) {
			if (t.getId().longValue() == taskId) {
				task = t;
			}
		}
		
		request.getProcessingContext().putEntry(RESTARTING, "true");
		notifyRestarted(task);
		try {
			FlowManager.getDefault().stopExecution(handle.getFlowHandle().getFlow(), timeOutUnit.toMillis(timeOut));
			return(true);
		} catch (Exception e) {
			LOGGER.error("Exception while stopping running flow.", e);
			return(false);
		}
	}

	@Override
	public boolean resume() {
		try {
			FlowManager.getDefault().resumeExecution(handle.getFlowHandle().getFlow());
			return(true);
		} catch (Exception e) {
			LOGGER.error("Exception while resuming flow.", e);
			return(false);
		}
	}

	@Override
	public boolean start() {
		//FIXME implement start of flow and store Flow in FlowHandle of handle
		return(false);
	}

	@Override
	public boolean stop(long timeOut, TimeUnit timeOutUnit) {
		try {
			FlowManager.getDefault().stopExecution(handle.getFlowHandle().getFlow(), timeOutUnit.toMillis(timeOut));
			return(true);
		} catch (Exception e) {
			LOGGER.error("Exception while stopping running flow.", e);
			return(false);
		}
	}
}
