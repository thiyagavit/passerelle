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

package com.isencia.passerelle.validation.version;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The central registry where version specs can be maintained for actor implementations.
 * Currently Ptolemy and Passerelle are only able to maintain one version of a given actor class in a runtime.
 * But in the future, after refactoring the MomlParser's actor loading, multiple versions can be present,
 * taking advantage of OSGi's version management features.
 * 
 * @author erwin
 *
 */
public class ActorVersionRegistry {
  
  private Map<String, SortedSet<VersionSpecification>> actorVersions = new HashMap<String, SortedSet<VersionSpecification>>();

  private final static SortedSet<VersionSpecification> EMPTY_VERSIONSET = Collections.unmodifiableSortedSet(new TreeSet<VersionSpecification>());
  
  private final static ActorVersionRegistry instance = new ActorVersionRegistry();
  
  private ActorVersionRegistry() {
  }
  
  /**
   * 
   * @return the singleton instance of this registry
   */
  public static ActorVersionRegistry getInstance() {
    return instance;
  }
  
  public void clear() {
    for(SortedSet<VersionSpecification> set : actorVersions.values()) {
      set.clear();
    }
    
    actorVersions.clear();
  }
  
  /**
   * 
   * @param actorClassName
   * @return most recent version for the given actor class, or null if no registered version
   */
  public VersionSpecification getMostRecentVersion(String actorClassName) {
    VersionSpecification result = null;
    SortedSet<VersionSpecification> versionSet = actorVersions.get(actorClassName);
    if(versionSet!=null) {
      try {
        result = versionSet.last();
      } catch (NoSuchElementException e) {
      }
    }
    return result;
  }
  
  /**
   * 
   * @param actorClassName
   * @return a SortedSet with the registered versions for the given actor class (sorted from old to new), or an empty set if none registered.
   */
  public SortedSet<VersionSpecification> getAvailableVersions(String actorClassName) {
    SortedSet<VersionSpecification> versionSet = actorVersions.get(actorClassName);
    if(versionSet!=null) {
      return Collections.unmodifiableSortedSet(versionSet);
    } else {
      return EMPTY_VERSIONSET;
    }
  }
  
  /**
   * 
   * @param actorClassName
   * @param version
   * @return true if the registry did not yet contain the given version for the given actorName
   */
  public boolean addActorVersion(String actorClassName, VersionSpecification version) {
    SortedSet<VersionSpecification> versionSet = actorVersions.get(actorClassName);
    if(versionSet==null) {
      versionSet = new TreeSet<VersionSpecification>();
      actorVersions.put(actorClassName, versionSet);
    }
    return versionSet.add(version);
  }
}
