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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.core.Event;

public abstract class AbstractEvent implements Event {
  
  private static volatile AtomicLong idCounter = new AtomicLong(0);

  private Date timeStamp;
  private long id;
  private String topic;
  
  private Map<String, String> eventProperties =  new HashMap<String, String>();
  
  protected AbstractEvent(String topic, Date creationTS) {
    this.topic = topic;
    this.timeStamp = creationTS;
    this.id = idCounter.incrementAndGet();
  }
  
  protected AbstractEvent(NamedObj subject, String topic, Date creationTS) {
    this(topic,creationTS);
    eventProperties.put(SUBJECT, subject.getFullName());
  }

  protected long getId() {
    return id;
  }

  public String getTopic() {
    return topic;
  }
  
  public Date getCreationTS() {
    return timeStamp;
  }

  /**
   * @return 0L as default duration
   */
  public Long getDuration() {
    return 0L;
  }
  
  public String getProperty(String propName) {
    return eventProperties.get(propName);
  }
  
  public Iterator<String> getPropertyNames() {
    return eventProperties.keySet().iterator();
  }
  
  /**
   * 
   * @return a new Event with copied info, but new timestamp
   */
  public abstract Event copy();

  /**
   * 
   * @param dateFormat
   * @return a toString representation of the event, 
   * where dates are formatted with the given dateFormat.
   */
  public abstract String toString(DateFormat dateFormat);

  @Override
  public String toString() {
    return toString(new SimpleDateFormat("dd/MM/yy HH:mm:ss.SSS"));
  }
}
