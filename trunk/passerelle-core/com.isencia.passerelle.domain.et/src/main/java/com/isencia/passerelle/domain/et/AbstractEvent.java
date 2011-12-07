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
package com.isencia.passerelle.domain.et;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

abstract class AbstractEvent implements Event {

  private Date timeStamp;
  
  protected AbstractEvent(Date timeStamp) {
    this.timeStamp = timeStamp;
  }

  public Date getTimestamp() {
    return timeStamp;
  }

  @Override
  public String toString() {
    return toString(new SimpleDateFormat());
  }

  public abstract String toString(DateFormat dateFormat);
}
