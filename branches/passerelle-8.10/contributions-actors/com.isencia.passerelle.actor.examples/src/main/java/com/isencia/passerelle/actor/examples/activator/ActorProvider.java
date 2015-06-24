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

package com.isencia.passerelle.actor.examples.activator;

import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.validation.version.VersionSpecification;

/**
 * @author erwin
 * @deprecated since v8.4 use DefaultModelElementClassProvider in your activator i.o. this approach
 *
 */
public class ActorProvider implements ModelElementClassProvider {

  public Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    if(className.startsWith("com.isencia.passerelle.actor.examples")) {
      return (Class<? extends NamedObj>) this.getClass().getClassLoader().loadClass(className);
    } else {
      throw new ClassNotFoundException();
    }
  }

}
