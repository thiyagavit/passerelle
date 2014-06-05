/* Copyright 2011 - iSencia Belgium NV

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

package com.isencia.passerelle.actor.examples;

import java.util.Collection;
import java.util.Iterator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.eip.MessageRouter;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * @author delerw
 *
 */
public class RandomMessageRouter extends MessageRouter {
  private static final long serialVersionUID = 1L;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public RandomMessageRouter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
  }

  @Override
  protected String routeToPort(Collection<String> availablePortNames, ManagedMessage msg) {
    String selectedPortName = null;
    int portCount = availablePortNames.size();
    if(portCount>0) {
      long selectedPort = 1+Math.round(Math.random()*(portCount-1));
      int counter = 0;
      Iterator<String> portNameItr = availablePortNames.iterator();
      while (portNameItr.hasNext() && (counter++ < selectedPort)) {
        selectedPortName = portNameItr.next();
      }
    }
    return selectedPortName;
  }
}
