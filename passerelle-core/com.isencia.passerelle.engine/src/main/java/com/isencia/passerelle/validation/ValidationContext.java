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

package com.isencia.passerelle.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import com.isencia.passerelle.actor.ValidationException;

/**
 * A <code>ValidationContext</code> serves as a simple container to store validation information.
 * 
 * @author erwin
 *
 */    
public class ValidationContext {
  
  private Collection<ValidationException> errors = new ArrayList<ValidationException>();
  private Collection<String> invalidElements = new ArrayList<String>();

  /**
   * 
   * @param invalidElement the name (typically an actor class name) of a validated element
   * @param e the validation error that was found for the given element
   */
  public void addError(String invalidElement, ValidationException e) {
    if(e!=null) {
      errors.add(e);
    }
    if(invalidElement!=null) {
      invalidElements.add(invalidElement);
    }
  }
  
  public Collection<ValidationException> getErrors() {
    return Collections.unmodifiableCollection(errors);
  }
  
  public Collection<String> getInvalidElements() {
    return Collections.unmodifiableCollection(invalidElements);
  }
  
  public boolean isValid() {
    return errors.isEmpty() && invalidElements.isEmpty();
  }
}
