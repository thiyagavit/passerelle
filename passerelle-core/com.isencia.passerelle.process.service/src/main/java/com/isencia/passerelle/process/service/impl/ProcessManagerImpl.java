package com.isencia.passerelle.process.service.impl;

import java.util.concurrent.TimeUnit;

import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.runtime.ProcessHandle;

public class ProcessManagerImpl implements ProcessManager {
	
	private final ProcessHandle handle;
	private Request request;

	public ProcessManagerImpl(Request request) {
		this(new ProcessHandleImpl(),request);
	}
	
	public ProcessManagerImpl(ProcessHandle handle, Request request) {
		this.handle = handle;
		this.request = request;
		this.request.getProcessingContext().setProcessId(handle.getProcessId());
	}
	
	@Override
	public ProcessHandle getHandle() {
		return(handle);
	}
	
	public Request getRequest() {
		return(request);
	}
	
	@Override
	public Task getTask(Long id) {
		// TODO Auto-generated method stub
		return null;
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
	public void pause() {
		// TODO Auto-generated method stub	
	}

	@Override
	public void restart(Long taskId, long timeOut, TimeUnit timeOutUnit) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub	
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub	
	}

	@Override
	public void stop(long timeOut, TimeUnit timeOutUnit) {
		// TODO Auto-generated method stub	
	}
}
