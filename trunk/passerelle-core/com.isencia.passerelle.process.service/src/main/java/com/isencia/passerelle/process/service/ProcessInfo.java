package com.isencia.passerelle.process.service;

import java.io.Serializable;

public class ProcessInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private Long requestID;
	private String requestType;
	private String flowName;
	private String flowID;
	private FlowState flowState;

	public ProcessInfo() {
	}

	public ProcessInfo(String id, Long requestID, String requestType) {
		this.id = id;
		this.requestID = requestID;
		this.requestType = requestType;
	}

	public String getId() {
		return id;
	}

	public Long getRequestID() {
		return requestID;
	}

	public void setRequestID(Long requestID) {
		this.requestID = requestID;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getFlowID() {
		return flowID;
	}

	public void setFlowID(String flowID) {
		this.flowID = flowID;
	}

	public FlowState getFlowState() {
		return flowState;
	}

	public void setFlowState(FlowState flowState) {
		this.flowState = flowState;
	}

	public enum FlowState {
		STARTED, PAUSED, STOPPED
	}
}
