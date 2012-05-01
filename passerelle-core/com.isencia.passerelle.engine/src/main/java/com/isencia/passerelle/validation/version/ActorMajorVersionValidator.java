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

import java.util.SortedSet;
import com.isencia.passerelle.actor.ValidationException;

/**
 * This validator checks whether the major version nr of an actor present in a model,
 * is the same as the major version number as registered for the actor implementation present in the runtime.
 * <p>
 * If no version is registered in the runtime, we assume that no version constraint checking must be performed,
 * so the validator will accept any <code>versionToBeValidated</code> in such cases.
 * </p>
 * 
 * @author erwin
 *
 */
public class ActorMajorVersionValidator implements ModelElementVersionValidationStrategy {

  public void validate(String versionedElementClassName, VersionSpecification versionToBeValidated) throws ValidationException {
    if(versionedElementClassName!=null && versionToBeValidated!=null) { 
      // first check with the most recent version
      VersionSpecification mostRecentVersion = ActorVersionRegistry.getInstance().getMostRecentVersion(versionedElementClassName);
      if(mostRecentVersion==null) {
        // no registered version constraint, so any entered version is valid.
      } else {
        if (mostRecentVersion.getMajor()==versionToBeValidated.getMajor()) {
          // all's well 
        } else if(mostRecentVersion.getMajor()<versionToBeValidated.getMajor()) {
          throw new ValidationException("Available version "+mostRecentVersion+" too old for required version "+versionToBeValidated, versionedElementClassName, null);
        } else {
          // This means the runtime has a more recent major version than what's required for the element.
          // This may also lead to incompatibilities.
          // So need to check if any compatible version is available. 
          // (remark : for the moment only one version is available though!)
          SortedSet<VersionSpecification> availableVersions = ActorVersionRegistry.getInstance().getAvailableVersions(versionedElementClassName);
          boolean foundCompatibleVersion = false;
          for (VersionSpecification availableVersionSpec : availableVersions) {
            if(availableVersionSpec.getMajor()==versionToBeValidated.getMajor()) {
              foundCompatibleVersion = true;
              break;
            }
          }
          if(!foundCompatibleVersion) {
            throw new ValidationException("Available version "+mostRecentVersion+" more recent than required version "+versionToBeValidated, versionedElementClassName, null);
          }
        }
      }
    }
  }
}
