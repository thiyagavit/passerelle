package com.isencia.passerelle.process.service.impl;

import java.util.UUID;

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
	}
	
	@Override
	public String getId() {
		return(id);
	}
	
	public Request getRequest() {
		return(request);
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
	public void notifyStarted() {
		// TODO Auto-generated method stub		
	}
	
	@Override
	public void notifyStarted(Task task) {
		// TODO Auto-generated method stub		
	}
}
