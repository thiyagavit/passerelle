/**
 * 
 */
package com.isencia.passerelle.runtime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author erwin
 *
 */
public abstract class AbstractProcessEvent implements ProcessEvent {

  private Date timeStamp;
  private String topic;
  
  private Map<String, String> eventProperties =  new HashMap<String, String>();
  private Kind kind;
  private Detail detail;
  
  protected AbstractProcessEvent(Kind kind, Detail detail, Date creationTS) {
    this.topic = TOPIC_PREFIX+kind.name()+"/"+detail.name();
    this.timeStamp = creationTS;
    
    this.kind = kind;
    this.detail = detail;
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
  
  @Override
  public String toString() {
    return toString(new SimpleDateFormat("dd/MM/yy HH:mm:ss.SSS"));
  }
  
  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Detail getDetail() {
    return detail;
  }

  /**
   * 
   * @param dateFormat
   * @return a toString representation of the event, 
   * where dates are formatted with the given dateFormat.
   */
  public String toString(DateFormat dateFormat) {
    return dateFormat.format(getCreationTS()) + " ProcessEvent [topic=" + getTopic() + "]";
  }
}