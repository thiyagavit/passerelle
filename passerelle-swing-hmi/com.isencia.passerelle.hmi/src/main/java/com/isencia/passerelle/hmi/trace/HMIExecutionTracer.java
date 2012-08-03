/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.ext.ExecutionTracer;
import com.isencia.passerelle.hmi.util.DynamicStepExecutionControlStrategy;

/**
 * An ExecutionTracer wrapper that allows to register itself as an attribute on
 * some Passerelle entity (typically a director).
 * 
 * @author erwin.de.ley@isencia.be
 */
public class HMIExecutionTracer implements ExecutionTracer {
  private final static Logger traceLogger = LoggerFactory.getLogger("trace");

  private final ExecutionTracer tracedialog;

  public HMIExecutionTracer(final ExecutionTracer dialog) {
    this.tracedialog = dialog;
  }

  public void trace(final Actor actor, final String message) {
    tracedialog.trace(actor, message);
    try {
      DynamicStepExecutionControlStrategy execCtrl = (DynamicStepExecutionControlStrategy) actor.getDirectorAdapter().getExecutionControlStrategy();

      execCtrl.stopStep();

    } catch (Exception e) {
      // ignore, just means we're certainly not using a step exec ctrl
    }
  }

  public void trace(final Director director, final String message) {
    tracedialog.trace(director, message);
  }
}
