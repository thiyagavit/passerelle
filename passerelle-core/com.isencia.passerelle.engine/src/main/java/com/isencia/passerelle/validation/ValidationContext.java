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

  /**
   * 
   * @param e the validation error that was found for the given element
   */
  public void addError(ValidationException e) {
    if(e!=null) {
      errors.add(e);
    }
  }
  
  public Collection<ValidationException> getErrors() {
    return Collections.unmodifiableCollection(errors);
  }
  
  public boolean isValid() {
    return errors.isEmpty();
  }
}
