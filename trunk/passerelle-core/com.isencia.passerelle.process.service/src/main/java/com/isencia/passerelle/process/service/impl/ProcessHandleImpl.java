package com.isencia.passerelle.process.service.impl;

import java.util.UUID;

import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.ProcessStatus;

public class ProcessHandleImpl implements ProcessHandle {
	private FlowHandle flow;
	private String processId;
	private ProcessStatus executionStatus;
	private String[] suspendedElements;

	public ProcessHandleImpl() {
		this(UUID.randomUUID().toString());
	}
	
	public ProcessHandleImpl(String processId) {
		this.processId = processId; 
	}
	
	@Override
	public ProcessStatus getExecutionStatus() {
		return executionStatus;
	}

	@Override
	public FlowHandle getFlow() {
		return flow;
	}

	@Override
	public String getProcessId() {
		return processId;
	}

	@Override
	public String[] getSuspendedElements() {
		return suspendedElements;
	}

	public void setExecutionStatus(ProcessStatus executionStatus) {
		this.executionStatus = executionStatus;
	}
	
	public void setFlow(FlowHandle flow) {
		this.flow = flow;
	}
	
	public void setSuspendedElements(String[] suspendedElements) {
		this.suspendedElements = suspendedElements;
	}
}
