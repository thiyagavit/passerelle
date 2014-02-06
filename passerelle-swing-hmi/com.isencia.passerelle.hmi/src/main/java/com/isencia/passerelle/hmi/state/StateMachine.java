/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.state;

import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Action;

/**
 * State machine for the UI. Singleton, if custom state machines are needed,
 * this can be subclassed...
 * 
 * @author erwin.de.ley@isencia.be
 */
public class StateMachine {
  private final Set<ActionEnabler> allActions = new HashSet<ActionEnabler>();
  private final Map<State, Set<ActionEnabler>> allActionsPerState = new HashMap<State, Set<ActionEnabler>>();

  private final static StateMachine instance = new StateMachine();
  public final static State READY = new State("READY");
  public final static State MODEL_OPEN = new State("MODEL_OPEN");
  public final static State MODEL_EXECUTING = new State("MODEL_EXECUTING");
  public final static State MODEL_EXECUTING_SUSPENDED = new State("MODEL_EXECUTING_SUSPENDED");
  public final static State MODEL_DEBUGGING = new State("MODEL_DEBUGGING");
  public final static State EXITING = new State("EXITING");

  private State currentState;

  private StateMachine() { // Only singleton Allowed
  }

  public static StateMachine getInstance() {
    return instance;
  }

  /**
   * Step 1: register all combinations of state and allowed actions by
   * repeatedly invoking this method.
   * 
   * @param state
   * @param name
   * @param actionComponent
   */
  public void registerActionForState(final State state, final String name, final Component actionComponent) {
    Set<ActionEnabler> actions = allActionsPerState.get(state);
    if (actions == null) {
      actions = new HashSet<ActionEnabler>();
      allActionsPerState.put(state, actions);
    }
    final ActionEnabler acb = new ActionComponentBinding(name, actionComponent);
    actions.add(acb);
    allActions.add(acb);
  }

  public void registerActionForState(final State state, final String name, final Action action) {
    Set<ActionEnabler> actions = allActionsPerState.get(state);
    if (actions == null) {
      actions = new HashSet<ActionEnabler>();
      allActionsPerState.put(state, actions);
    }
    final ActionEnabler acb = new ActionBinding(name, action);
    actions.add(acb);
    allActions.add(acb);
  }

  /**
   * Step 2: compile the state machine
   */
  public void compile() {
    final Iterator<Entry<State, Set<ActionEnabler>>> stateItr = allActionsPerState.entrySet().iterator();
    while (stateItr.hasNext()) {
      final Entry<State, Set<ActionEnabler>> entry = stateItr.next();
      final State state = entry.getKey();
      final Set<ActionEnabler> actions = entry.getValue();
      for (ActionEnabler element : actions) {
        final ActionEnabler acb = element;
        state.addAllowedAction(acb.getActionName());
      }
    }
  }

  /**
   * Step 3: call state transitions, and the actions should get enabled/disabled
   * automatically!
   * 
   * @param newState
   */
  public synchronized void transitionTo(final State newState) {
    currentState = newState;
    final Iterator<ActionEnabler> actionItr = allActions.iterator();
    while (actionItr.hasNext()) {
      final ActionEnabler acb = actionItr.next();
      acb.setEnabled(newState.isAllowed(acb.getActionName()));
    }
  }

  /**
   * @return Returns the currentState.
   */
  public synchronized State getCurrentState() {
    return currentState;
  }

}
