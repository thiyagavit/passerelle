/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.util.Attribute;
import com.isencia.passerelle.ext.ExecutionControlStrategy;
import com.isencia.passerelle.ext.impl.SuspendResumeExecutionControlStrategy;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.hmi.util.DynamicStepExecutionControlStrategy;

@SuppressWarnings("serial")
public class ModelResumer extends AbstractAction {
  private final static Logger logger = LoggerFactory.getLogger(ModelDebugStepper.class);

  public ModelResumer(final HMIBase base) {
    super(base, HMIMessages.getString(HMIMessages.MENU_RESUME), new ImageIcon(HMIBase.class.getResource("resources/resume.gif")));
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

  public synchronized void actionPerformed(final ActionEvent e) {
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("Model Resume action - entry"); //$NON-NLS-1$
    }

    try {
      final ExecutionControlStrategy execCtrlStr = getHMI().getDirector().getExecutionControlStrategy();
      try {
        ((SuspendResumeExecutionControlStrategy) execCtrlStr).resume();
      } catch (final ClassCastException ex) {
        ((DynamicStepExecutionControlStrategy) execCtrlStr).resume();
        // there's a small time window here where there's a risk that a
        // suspend action
        // is triggered immediately after the resume, where it will
        // still see the Stepping exec ctrl strategy,
        // but that should never happen in reality...
        final Attribute tmp = getHMI().getDirector().getAttribute(HMIBase.EXECUTION_CONTROL_ATTR_NAME);
        if (tmp != null) {
          // remove the previous execution controller
          tmp.setContainer(null);
        }
        new SuspendResumeExecutionControlStrategy(getHMI().getDirector(), HMIBase.EXECUTION_CONTROL_ATTR_NAME);
      }
    } catch (final Exception ex) {
      getLogger().error("Received resume event, but model not configured correctly", ex);
    }

    StateMachine.getInstance().transitionTo(StateMachine.MODEL_EXECUTING);

    if (getLogger().isTraceEnabled()) {
      getLogger().trace("Model Resume action - exit"); //$NON-NLS-1$
    }
  }
}