package com.isencia.passerelle.process.model.event;

import java.util.Date;
import com.isencia.passerelle.process.model.DataTypes;
import com.isencia.passerelle.runtime.Event;

public class StringValuedEventImpl extends AbstractResultItemEventImpl<String> {

  private static final long serialVersionUID = 7401438663940242342L;
  
  public StringValuedEventImpl(String topic, String value, Date creationTS, Long duration) {
    super(topic,value,creationTS,duration);
  }

  public StringValuedEventImpl(String topic, String value) {
    this(topic, value, new Date(), 0L);
  }
  
  @Override
  public String getDataType() {
    return DataTypes.STRING;
  }
  
  @Override
  public Event createDerivedEvent(String namePrefix) {
    return new StringValuedEventImpl(namePrefix + "//" + getName()+"//" + "(" + getFormattedCreationTS() + ")", getValue());
  }
}
