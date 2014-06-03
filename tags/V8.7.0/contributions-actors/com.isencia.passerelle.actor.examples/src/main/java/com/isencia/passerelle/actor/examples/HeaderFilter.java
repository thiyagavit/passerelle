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

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * A filter implementation to check on a message's header entries. <br/>
 * In this basic implementation, we just check if a header with a given name is
 * present.
 */
@SuppressWarnings("serial")
public class HeaderFilter extends Filter {

  public StringParameter headerNameParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public HeaderFilter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    headerNameParameter = new StringParameter(this, "Header name");
    headerNameParameter.setExpression("MyHeader");
  }

  @Override
  protected boolean isMatchingFilter(ManagedMessage msg) throws Exception {
    return msg.getBodyHeader(headerNameParameter.getExpression()) != null;
  }
}
