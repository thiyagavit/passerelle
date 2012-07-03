package com.isencia.passerelle.process.model;

/**
 * 
 * @author erwin
 *
 */
public interface Status {
	String CREATED="CREATED";
	
	String PROCESSING = "PROCESSING";
	String STARTED="STARTED";
	String PENDING="PENDING";
	
  String FINISHED="FINISHED";
	String ERROR="ERROR";
	
  String TIMEOUT="TIMEOUT"; 
	String INTERRUPTED = "INTERRUPTED";
	
	String RESTARTED = "RESTARTED";
}
