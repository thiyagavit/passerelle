/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.runtime.repository;

import java.util.List;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;

/**
 * This interface offers operations to manage Flows in a repository.
 * <p>
 * As Flows can be large and complex structures, lightweight FlowHandles are used
 * as intermediate entities for most actions that must be done on the repository.
 * <br/>
 * A FlowHandle allows to obtain its associated Flow, and/or metadata like the identifying "code",
 * version info etc.
 * </p>
 * <p>
 * The repository may be able to maintain different versions for the Flows,
 * in which case it is possible to indicate the current active version/revision.
 * The active version is the one that will be used when actions (e.g. executing) 
 * are performed on a Flow by name or identifying code.
 * </p>
 * <p>
 * This interface offers a basic "linear" view on Flows in a repository.
 * More advanced repository interfaces (e.g. including Projects, rules modules, images and other asset types)
 * will/must be defined separately.
 * </p>
 * 
 * @author erwin
 *
 */
public interface FlowRepositoryService {
 
  /**
   * Store the given Flow in the repository, if it does not exist yet,
   * using the Flow's name as identifier.
   * <p>
   * If an entry already exists for the given name, this operation will fail.
   * </p> 
   * @param flow
   * @return the handle to the created repository entry
   * @throws DuplicateEntryException if an entry with the same identifier already exists
   */
  FlowHandle saveFlow(Flow flow) throws DuplicateEntryException;
  
  /**
   * Store the given Flow in the repository, if it does not exist yet,
   * using the given flowCode as identifier.
   * <p>
   * If an entry already exists for the given flowCode, this operation will fail.
   * </p> 
   * @param flowCode the identifier to be used for the flow and its future revisions
   * @param flow
   * @return the handle to the created repository entry
   * @throws DuplicateEntryException if an entry with the same identifier already exists
   */
  FlowHandle saveFlow(String flowCode, Flow flow) throws DuplicateEntryException;
  
  /**
   * 
   * @param flowCode
   * @return all versions of the Flow, that were stored in the repository for the given flowCode.
   * @throws EntryNotFoundException
   */
  List<FlowHandle> deleteFlow(String flowCode) throws EntryNotFoundException;
  
  /**
   * Returns the Flow stored in the repository for the given code, in the version that is currently activated.
   * 
   * @param flowCode
   * @return the FlowHandle active for the given code 
   * @throws EntryNotFoundException when the code does not correspond to a Flow entry in this repository
   */
  FlowHandle getActiveFlow(String flowCode) throws EntryNotFoundException;
  
  /**
   * @return list of all Flow codes, for which Flows are stored in the repository.
   */
  List<String> getAllFlowCodes();
  
  /**
   * 
   * @param flowCode
   * @return handles for all flow versions in the repository for the given code
   * @throws EntryNotFoundException when the code does not correspond to a Flow entry in this repository
   */
  List<FlowHandle> getAllFlowRevisions(String flowCode) throws EntryNotFoundException;
  
  /**
   * 
   * @param handle
   * @return the handle to the flow revision that was active before
   * @throws EntryNotFoundException
   */
  FlowHandle activateFlowRevision(FlowHandle handle) throws EntryNotFoundException;
  
  /**
   * Updates the Flow definition in the repository,
   * using the Flow's name to identify the repository entry that must be updated.
   * <p>
   * In a versioned repository, this should increase the major version nr for the flow.
   * </p>
   * 
   * @param updatedFlow
   * @param activate true if the given flow should be directly set as the active revision;
   *   false if the current active revision for the same flowCode should remain active.
   * @return the handle to the updated repository entry
   * @throws EntryNotFoundException when the flow's name does not correspond to a Flow entry in this repository
   */
  FlowHandle updateFlow(Flow updatedFlow, boolean activate) throws EntryNotFoundException;
  
  /**
   * Updates the Flow definition in the repository,
   * using the given FlowHandle to identify the repository entry that must be updated.
   * <p>
   * In a versioned repository, this should increase the major version nr for the flow.
   * </p>
   * 
   * @param handle
   * @param updatedFlow
   * @param activate true if the given flow should be directly set as the active revision;
   *   false if the current active revision for the same flowCode should remain active.
   * @return the handle to the updated repository entry
   * @throws EntryNotFoundException when the handle does not correspond to a Flow entry in this repository
   */
  FlowHandle updateFlow(FlowHandle handle, Flow updatedFlow, boolean activate) throws EntryNotFoundException;
  
  /**
   * Updates the Flow definition in the repository,
   * using the given code to identify the repository entry that must be updated.
   * <p>
   * In a versioned repository, this should increase the major version nr for the flow.
   * </p>
   * 
   * @param flowCode
   * @param updatedFlow
   * @param activate true if the given flow should be directly set as the active revision;
   *   false if the current active revision for the same flowCode should remain active.
   * @return the handle to the updated repository entry
   * @throws EntryNotFoundException when the code does not correspond to a Flow entry in this repository
   */
  FlowHandle updateFlow(String flowCode, Flow updatedFlow, boolean activate) throws EntryNotFoundException;
  
  
}
