/* Copyright 2014 - iSencia Belgium NV

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
package com.isencia.passerelle.process.service;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.factory.EntityManager;

/**
 * A ContextStore represents an interface for simplified (persistent) Context tracing.
 * It can be optionally used, e.g. in a {@link ContextRepository}, next to the full-blown entity persistence offered by an {@link EntityManager}
 * <p>
 * Different use cases can be implemented. 
 * E.g. a ContextStore does not need store all {@link Context}s, it could track only a particular one,
 * or only those of a particular {@link Request} type. Or it could store only selected parts of a {@link Context} etc.
 * </p>
 * @author erwindl
 *
 */
public interface ContextStore {
  
  /**
   * Store a <code>Context</code> in the store
   * 
   * @param context
   */
  void storeContext(Context context);
}
