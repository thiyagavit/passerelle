/**
 * 
 */
package com.isencia.passerelle.runtime;

/**
 * @author erwin
 */
public enum ProcessExecutionStatus {

  IDLE, // when the flow is not executing, not even starting to execute
  STARTING, // when the execution has been requested, and the flow is going through it's initialization phases
  ACTIVE, // when the flow is really doing work
  PAUSED, // when the flow is paused; either via a global pause action, a breakpoint, between steps when running in stepping mode etc
  STOPPING, // when the model has done its work and is going through its wrapup phases
  FINISHED, // when the execution has been completed without technical/runtime errors
  INTERRUPTED, // when the execution was interrupted/aborted before its normal completion, typically by a user action
  ERROR; // when the execution has encountered a technical/runtime error, e.g. flow parsing errors or other dramatic technical errors. 
         // Functional errors for specific actors/tasks do not impact the ExecutionStatus.

}
