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
import com.isencia.passerelle.ext.impl.SuspendResumeExecutionControlStrategy;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.model.Flow;

@SuppressWarnings("serial")
public class ModelSuspender extends AbstractAction {
  private final static Logger logger = LoggerFactory.getLogger(ModelSuspender.class);

  public ModelSuspender(final HMIBase base) {
    super(base, HMIMessages.getString(HMIMessages.MENU_SUSPEND), new ImageIcon(HMIBase.class.getResource("resources/suspend.gif")));
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

  public synchronized void actionPerformed(final ActionEvent e) {
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("Model Suspend action - entry"); //$NON-NLS-1$
    }

    // DBA : need to be validating : pause execution
    // getHMI().suspendModel();

    if (getHMI().getCurrentModel() instanceof Flow) {
      Flow currentModel = (Flow) getHMI().getCurrentModel();
      if (currentModel.getHandle().isRemote()) {
        PopupUtil.showInfo(getHMI().getDialogHookComponent(), "Suspend not yet supported for remote execution");
        return;
      }
    }

    try {
      ((SuspendResumeExecutionControlStrategy) getHMI().getDirector().getExecutionControlStrategy()).suspend();
    } catch (final Exception ex) {
      getLogger().error("Received suspend event, but model not configured correctly", ex);
    }
    StateMachine.getInstance().transitionTo(StateMachine.MODEL_EXECUTING_SUSPENDED);

    if (getLogger().isTraceEnabled()) {
      getLogger().trace("Model Suspend action - exit"); //$NON-NLS-1$
    }
  }
}