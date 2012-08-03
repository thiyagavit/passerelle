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
package com.isencia.passerelle.ext;

import ptolemy.actor.FiringEvent;
import ptolemy.data.expr.Parameter;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.ext.ErrorControlStrategy;
import com.isencia.passerelle.ext.ExecutionControlStrategy;
import com.isencia.passerelle.ext.ExecutionPrePostProcessor;
import com.isencia.passerelle.ext.FiringEventListener;

/**
 * An interface grouping all extension features that Passerelle assigns to a Director,
 * to centralize them in one spot in a model.
 * 
 * @author erwin
 */
public interface DirectorAdapter {
  
  public static final String MOCKMODE_PARAM = "Mock Mode";
  public static final String EXPERTMODE_PARAM = "Expert Modeler";
  public static final String VALIDATE_INITIALIZATION_PARAM = "Validate Initialization";
  public static final String VALIDATE_ITERATION_PARAM = "Validate Iteration";
  String DEFAULT_ADAPTER_NAME = "__directorAdapter";
      
  void addErrorCollector(ErrorCollector errCollector);

  boolean removeErrorCollector(ErrorCollector errCollector);

  void removeAllErrorCollectors();

  void reportError(PasserelleException e);

  ErrorControlStrategy getErrorControlStrategy();

  void setErrorControlStrategy(ErrorControlStrategy errorCtrlStrategy, boolean enforceThisOne);

  ExecutionControlStrategy getExecutionControlStrategy();

  void setExecutionControlStrategy(ExecutionControlStrategy execCtrlStrategy);
  
  void setExecutionPrePostProcessor(ExecutionPrePostProcessor execPrePostProcessor);

  ExecutionPrePostProcessor getExecutionPrePostProcessor();

  /**
   * @return Returns the mockMode.
   */
  boolean isMockMode();

  /**
   * @return Returns the expertMode.
   */
  boolean isExpertMode();
  
  /**
   * 
   * @return whether each actor should do a validation of its initialization
   */
  boolean mustValidateInitialization();

  /**
   * 
   * @return whether each iteration of each actor should do a validation
   */
  boolean mustValidateIteration();

  /**
   * @return all configurable parameters
   */
  Parameter[] getConfigurableParameters();

  /**
   * Register a director parameter as configurable. Such parameters will be available in the Passerelle model configuration tools. All other actor parameters
   * are only available in model assembly tools.
   * 
   * @param newParameter
   */
  void registerConfigurableParameter(Parameter newParameter);

  /**
   * Register a listener that will be notified of ALL actor iteration transitions.
   * 
   * @see ptolemy.actor.FiringEvent
   * @param listener
   */
  void registerFiringEventListener(FiringEventListener listener);

  /**
   * @param listener
   * @return true if the listener was registered (and is now removed)
   */
  boolean removeFiringEventListener(FiringEventListener listener);

  /**
   * @return true if at least 1 listener is registered
   */
  boolean hasFiringEventListeners();

  /**
   * Forward the event to all registered listeners, iff the event is not-null and its director is me. The listener-related methods are NOT synchronized, to
   * ensure that the model execution does not block completely because of a blocking/long action of a listener... So there's no guarantee against race
   * conditions when someone starts modifying the listener set during model execution!
   * 
   * @param event
   */
  void notifyFiringEventListeners(FiringEvent event);
}