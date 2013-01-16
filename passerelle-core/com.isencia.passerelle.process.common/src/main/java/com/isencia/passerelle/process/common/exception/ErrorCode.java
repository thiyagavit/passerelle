/**
 * 
 */
package com.isencia.passerelle.process.common.exception;

import java.text.MessageFormat;

import com.isencia.passerelle.process.model.ErrorItem.Severity;
import static com.isencia.passerelle.process.model.ErrorItem.Severity.*;

/**
 * @author puidir
 *
 */
public enum ErrorCode {

  SERVICE_REQUEST_PROCEED_ERROR(ERROR,"1008","Error proceeding service request.")

  // 3000 RANGE : errors related to request handling and engine internals
  ,REQUEST_CONTENTS_ERROR(WARNING,"3000","Request contents error")
  ,REQUEST_INIT_ERROR(WARNING,"3001","Request initialisation error")
  ,REQUEST_NOT_FOUND(ERROR,"3002","Unknown reference id")
  
  ,REQUEST_ERROR(WARNING,"3100","request/ERROR","Request processing error")
  ,REQUEST_SLOW(WARNING,"3101","request/SLOW","Request processing slow")
  ,REQUEST_TIMEOUT(WARNING,"3102","request/TIME_OUT","Request processing timed out")
  ,REQUEST_SUBREQUEST_PROCESSING_ERROR(WARNING,"3100","request/ERROR","Error processing (sub)request")

    // 3500... Request scheduling and life-cycle management
  ,REQUEST_LIFECYCLE_ACCEPT_ERROR(ERROR,"3500","request/ERROR","Request lifecycle error : request acceptance error")
  ,REQUEST_LIFECYCLE_REQUEST_REFUSED(WARNING,"3501","request/ERROR","Request lifecycle error : request refused")
  ,REQUEST_LIFECYCLE_EVENT_LOG_ERROR(ERROR,"3502","request/ERROR","Request lifecycle error : event log error")
  ,REQUEST_LIFECYCLE_FINISHED_NOTIF_ERROR(ERROR,"3550","request/ERROR","Request lifecycle error : request finished notification error")
  ,REQUEST_LIFECYCLE_TIMEOUT_NOTIF_ERROR(ERROR,"3551","request/ERROR","Request lifecycle error : request timeout notification error")

  // 3700... Task scheduling/buffering etc
  // when the task involves a backend communication, these errors may also be reported
  // as specific errors for backend error/slow/timeout (5000 range)
  ,SCHEDULER_ERROR(ERROR,"3700","taskscheduler/ERROR","Task Scheduler error")
  ,SCHEDULER_BUFFER_LOAD(ERROR,"3710","taskscheduler/buffer/LOAD","Task Scheduler buffer load")
  ,SCHEDULER_POOL_LOAD(ERROR,"3720","taskscheduler/pool/LOAD","Task Scheduler pool load")
  ,SCHEDULER_WARNING(WARNING, "3730", "scheduler/WARNING", "EDM scheduler error")
  
  ,ANALYSIS_TASK_ERROR(WARNING,"3800","task/ERROR","Task processing error")
  ,ANALYSIS_TASK_SLOW(WARNING,"3801","task/SLOW","Task processing slow")
  ,ANALYSIS_TASK_TIMEOUT(WARNING,"3802","task/TIMEOUT","Task processing timed out")
  
  
  // 5000 RANGE : errors related to asynchronous service communications
  ,BACKEND_ERROR(INFO,"5000","task/ERROR", "Backend request processing error")
  ,BACKEND_SLOW(INFO,"5001","task/SLOW", "Backend request processing slow")
  ,BACKEND_TIMEOUT(INFO,"5002","task/TIME_OUT", "Backend request processing timed out")
  
  ,ASYNC_SERVICE_ERROR(INFO,"5000","task/ERROR", "Asynchronous service request processing error")
  ,INVALID_PARAMETERS(INFO,"5003","task/ERROR", "Request contains invalid parameters")  
  ,AXIS_FAULT(INFO, "5004", "task/ERROR", "Axis error while calling web service")
  ,INVALID_CONFIGURATION(WARNING, "5005", "Invalid configuration")
  
  // 5100 RANGE : errors related to backend request parameters
  ,INCOMPLETE_INPUT_ARGUMENTS(INFO, "5107", "Some input parameter are incomplete")
  ,CLIENT_LOCALE_SYNTAX(ERROR,"5190","Invalid end user language")

   // 8000 RANGE : errors related to OSGi
  ,BUNDLE_START_FAILED(FATAL,"8000","system/ERROR","Bundle start failed")
  ,BUNDLE_STOP_FAILED(WARNING,"8001","system/WARNING","Bundle stop failed")
  
  // 9000 RANGE : dramatic technical errors
  ,SYSTEM_ERROR(FATAL,"9999","system/ERROR","Internal DARE error")
  ,CONFIG_ERROR(FATAL,"9000","system/ERROR","EDM configuration error")

  ;
   
  private Severity severity;
  private String code;
  private String message;
  private String alarmTopic;

  private ErrorCode(Severity severity, String code, String message) {
    this.severity = severity;
    this.code = code;
    this.message = message;
    this.alarmTopic = this.name();
  }

  private ErrorCode(Severity severity, String code, String alarmTopic, String message) {
    this.severity = severity;
    this.code = code;
    this.message = message;
    this.alarmTopic = alarmTopic;
  }

  public Severity getSeverity() {
    return severity;
  }

  public String getAlarmTopic() {
    return alarmTopic;
  }
  
  public String getCode() {
    return code;
  }
  
  public String getFormattedCode() {
    return "[DARE-"+code+"]";
  }

  public String getMessage() {
    return message;
  }

  public String toString() {
    return getFormattedString();
  }

  public synchronized String getFormattedString() {
        return MessageFormat.format("{0} - [EDM-{1}] - {2} - ", new Object[] {getSeverity(),getCode(),getMessage()});
    }
}
