package com.isencia.passerelle.process.service;

import java.util.UUID;

import com.isencia.passerelle.process.model.Request;

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
	
	public Request getRequest() {
		return(request);
	}
	
	@Override
	public String getId() {
		return(id);
	}
	
}
