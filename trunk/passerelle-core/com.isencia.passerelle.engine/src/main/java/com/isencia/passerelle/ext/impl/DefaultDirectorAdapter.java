/* Copyright 2012 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.isencia.passerelle.ext.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.ext.ConfigurationExtender;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.ext.ErrorControlStrategy;
import com.isencia.passerelle.ext.ExecutionControlStrategy;
import com.isencia.passerelle.ext.ExecutionPrePostProcessor;
import com.isencia.passerelle.ext.FiringEventListener;

/**
 * @author erwin
 */
public class DefaultDirectorAdapter extends Attribute implements DirectorAdapter, ConfigurationExtender {
  private Logger LOGGER;

  /**
   * The collection of parameters that are meant to be available to a model configurer tool. The actor's parameters that are not in this collection are not
   * meant to be configurable, but are only meant to be used during model assembly (in addition to the public ones).
   */
  private Collection<Parameter> configurableParameters = new HashSet<Parameter>();

  /**
   * The collection of listeners for FiringEvents. If the collection is empty, no events are generated. If non-empty, inside the ProcessThread.run(), lots of
   * events are generated for each transition in the iteration of an actor.
   */
  private Collection<FiringEventListener> firingEventListeners = new HashSet<FiringEventListener>();

  /**
   * The collection of error collectors, to which the Director forwards any reported errors. If the collection is empty, reported errors are logged.
   */
  private Collection<ErrorCollector> errorCollectors = new HashSet<ErrorCollector>();

  private DefaultExecutionControlStrategy execCtrlStrategy = new DefaultExecutionControlStrategy();
  private ExecutionPrePostProcessor execPrePostProcessor = new DefaultExecutionPrePostProcessor();

  private ErrorControlStrategy errorCtrlStrategy = new DefaultActorErrorControlStrategy();
  private boolean enforcedErrorCtrlStrategy;

  public Parameter mockModeParam = null;
  public Parameter expertModeParam = null;
  public Parameter validateInitializationParam = null;
  public Parameter validateIterationParam = null;
  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public DefaultDirectorAdapter(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    LOGGER = LoggerFactory.getLogger(container.getClass().getName() + "." + this.getClass().getName());

    mockModeParam = new Parameter(container, MOCKMODE_PARAM, new BooleanToken(false));
    mockModeParam.setTypeEquals(BaseType.BOOLEAN);
    new CheckBoxStyle(mockModeParam, "style");
    registerConfigurableParameter(mockModeParam);

    expertModeParam = new Parameter(container, EXPERTMODE_PARAM, new BooleanToken(false));
    expertModeParam.setTypeEquals(BaseType.BOOLEAN);
    new CheckBoxStyle(expertModeParam, "style");
    registerConfigurableParameter(expertModeParam);

    validateInitializationParam = new Parameter(container, VALIDATE_INITIALIZATION_PARAM, new BooleanToken(true));
    validateInitializationParam.setTypeEquals(BaseType.BOOLEAN);
    new CheckBoxStyle(validateInitializationParam, "style");
    registerConfigurableParameter(validateInitializationParam);

    validateIterationParam = new Parameter(container, VALIDATE_ITERATION_PARAM, new BooleanToken(false));
    validateIterationParam.setTypeEquals(BaseType.BOOLEAN);
    new CheckBoxStyle(validateIterationParam, "style");
    registerConfigurableParameter(validateIterationParam);
  }

  public void addErrorCollector(ErrorCollector errCollector) {
    if (errCollector != null) {
      errorCollectors.add(errCollector);
    }
  }

  public boolean removeErrorCollector(ErrorCollector errCollector) {
    boolean res = false;
    if (errCollector != null) {
      res = errorCollectors.remove(errCollector);
    }
    return res;
  }

  public void removeAllErrorCollectors() {
    errorCollectors.clear();
  }

  public void reportError(PasserelleException e) {
    if (!errorCollectors.isEmpty()) {
      for (Iterator<ErrorCollector> errCollItr = errorCollectors.iterator(); errCollItr.hasNext();) {
        ErrorCollector element = errCollItr.next();
        element.acceptError(e);
      }
    } else {
      LOGGER.error("reportError() - no errorCollectors but received exception", e);
    }
  }

  public ErrorControlStrategy getErrorControlStrategy() {
    return errorCtrlStrategy;
  }

  public void setErrorControlStrategy(ErrorControlStrategy errorCtrlStrategy, boolean enforceThisOne) {
    if (enforceThisOne || !this.enforcedErrorCtrlStrategy) {
      this.errorCtrlStrategy = errorCtrlStrategy;
      this.enforcedErrorCtrlStrategy = enforceThisOne;
    }
  }

  public ExecutionControlStrategy getExecutionControlStrategy() {
    return execCtrlStrategy.getDelegate();
  }

  public void setExecutionControlStrategy(ExecutionControlStrategy execCtrlStrategy) {
    this.execCtrlStrategy.setDelegate(execCtrlStrategy);
  }

  public void setExecutionPrePostProcessor(ExecutionPrePostProcessor execPrePostProcessor) {
    this.execPrePostProcessor = execPrePostProcessor;
  }

  public ExecutionPrePostProcessor getExecutionPrePostProcessor() {
    return execPrePostProcessor;
  }

  public boolean isMockMode() {
    try {
      return ((BooleanToken) mockModeParam.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      return false;
    }
  }

  public boolean isExpertMode() {
    try {
      return ((BooleanToken) expertModeParam.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      return false;
    }
  }

  public boolean mustValidateInitialization() {
    try {
      return ((BooleanToken) validateInitializationParam.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      return false;
    }
  }

  public boolean mustValidateIteration() {
    try {
      return ((BooleanToken) validateIterationParam.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      return false;
    }
  }

  public Parameter[] getConfigurableParameters() {
    return (Parameter[]) configurableParameters.toArray(new Parameter[0]);
  }

  public void registerConfigurableParameter(Parameter newParameter) {
    if (newParameter != null && !configurableParameters.contains(newParameter) && newParameter.getContainer().equals(this)) {
      configurableParameters.add(newParameter);
    }
  }

  public void registerFiringEventListener(FiringEventListener listener) {
    if (listener != null)
      firingEventListeners.add(listener);
  }

  public boolean removeFiringEventListener(FiringEventListener listener) {
    return firingEventListeners.remove(listener);
  }

  public boolean hasFiringEventListeners() {
    return !firingEventListeners.isEmpty();
  }

  public void notifyFiringEventListeners(FiringEvent event) {
    if (event != null && event.getDirector().equals(this)) {
      for (Iterator<FiringEventListener> listenerItr = firingEventListeners.iterator(); listenerItr.hasNext();) {
        FiringEventListener listener = listenerItr.next();
        listener.onEvent(event);
      }
    }
  }

}
