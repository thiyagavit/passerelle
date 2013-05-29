/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.runtime.process.impl;

import java.util.Date;
import com.isencia.passerelle.runtime.AbstractProcessEvent;

/**
 * @author erwin
 *
 */
public class ProcessEvent extends AbstractProcessEvent {
  private static final long serialVersionUID = -6809094843226793224L;

  /**
   * @param kind
   * @param detail
   * @param creationTS
   */
  public ProcessEvent(Kind kind, Detail detail, Date creationTS) {
    super(kind, detail, creationTS);
  }

}
