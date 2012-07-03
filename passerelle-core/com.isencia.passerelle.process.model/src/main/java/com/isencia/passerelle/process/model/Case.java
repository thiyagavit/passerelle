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

package com.isencia.passerelle.process.model;

import java.io.Serializable;
import java.util.Set;

/**
 * @author erwin
 *
 */
public interface Case extends Serializable, Identifiable {
  
  /**
   * A case can be linked to an external/business entity, e.g. a trouble ticket or a business order etc.
   * The (optional) <code>referenceKey</code> can refer to this external entity.
   * <p>
   * Alternatively, it can just be used to maintain a more-or-less readable key that can be used to refer to this <code>Case</code>.
   * </p>
   * @return an optional key that can be used to refer to an associated (business) entity, or can serve as a simple readable key,
   * to refer to this <code>Case</code>.
   */
  String getReferenceKey();
  
  /**
   * 
   * @return the set of all <code>Request</code>s that are related to this case.
   */
  Set<Request> getRelatedRequests();

}
