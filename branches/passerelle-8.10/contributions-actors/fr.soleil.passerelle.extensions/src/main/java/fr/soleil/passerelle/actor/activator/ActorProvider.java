/* Copyright 2013 - Synchrotron Soleil

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

package fr.soleil.passerelle.actor.activator;

import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.validation.version.VersionSpecification;

/**
 * Simple actor class provider.
 * <p>
 * This is registered as an OSGi service implementation,
 * acting as an intermediary between Passerelle's MomlParser and the actual actor's class loader.
 * <br/>
 * This approach allows to dynamically change the actor implementation in an operational Passerelle runtime.
 * </p>
 * @author erwin
 *
 */
public class ActorProvider implements ModelElementClassProvider {

  public Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    if(className.startsWith("fr.soleil.passerelle")) {
      return (Class<? extends NamedObj>) this.getClass().getClassLoader().loadClass(className);
    } else {
      throw new ClassNotFoundException();
    }
  }

}
