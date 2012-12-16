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
import java.util.HashSet;
import ptolemy.kernel.util.NamedObj;
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
   * @param e the validation error that was found. 
   * It contains the validated element as its exception's context.
   * @see ValidationException 
   */
  public void addError(ValidationException e) {
    if(e!=null) {
      errors.add(e);
    }
  }

  /**
   * 
   * @return all validation errors that have been determined during a validation check for this context
   */
  public Collection<ValidationException> getErrors() {
    return Collections.unmodifiableCollection(errors);
  }

  /**
   * 
   * @param validatedElement
   * @return all validation errors found for the given model element, during a validation check for this context
   */
  public Collection<ValidationException> getErrors(NamedObj validatedElement) {
    Collection<ValidationException> result = new HashSet<ValidationException>();
    for(ValidationException ex : errors) {
      if(ex.getContext().equals(validatedElement)) {
        result.add(ex);
      }
    }
    return result;
  }
  
  /**
   * 
   * @return true if no validation errors were found in this context; false otherwise
   */
  public boolean isValid() {
    return errors.isEmpty();
  }
  
}
