package com.isencia.passerelle.process.service;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Context;

public class ExecutionTracerService {

  private final static Logger logger = LoggerFactory.getLogger("trace");

  public static final String ACTOR_MDC_NAME = "actor";

  public static void trace(Context taskContext, Context flowContext, Throwable e) {
    Attribute attribute = flowContext.getRequest().getAttribute("flowname");
    if (attribute != null)
      MDC.put("flowname", attribute.getValueAsString());
    if (taskContext != null)
      MDC.put(ACTOR_MDC_NAME, taskContext.getRequest().getInitiator());
    MDC.put("requestId", flowContext.getRequest().getId().toString());
    logger.error(getStackTrace(e));
  }

  private static String getStackTrace(Throwable e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    String stacktrace = sw.toString();
    return stacktrace;

  }
}
