package com.isencia.passerelle.process.service.impl;

import java.util.UUID;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.service.ProcessManager;

public class ProcessManagerImpl implements ProcessManager {
	
	private final String id;
	private Request request;

	public ProcessManagerImpl(Request request) {
		this(UUID.randomUUID().toString(),request);
	}
	
	public ProcessManagerImpl(String id, Request request) {
		this.id = id;
		this.request = request;
		this.request.getProcessingContext().setProcessId(id);
	}
	
	@Override
	public String getId() {
		return(id);
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
}
