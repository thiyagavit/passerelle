package com.isencia.passerelle.process.model.event;

import java.util.Date;
import com.isencia.passerelle.process.model.DataTypes;
import com.isencia.passerelle.runtime.Event;

public class DoubleValuedEventImpl extends AbstractResultItemEventImpl<Double> {
  private static final long serialVersionUID = -9093836337714095445L;

  public DoubleValuedEventImpl(String topic, Double value) {
    super(topic, value, new Date(), 0L);
  }
  public DoubleValuedEventImpl(String topic, Double value, Date creationTS, Long duration) {
    super(topic, value, creationTS, duration);
  }

  @Override
  public String getDataType() {
    return DataTypes.DOUBLE;
  }

  @Override
  public Event createDerivedEvent(String namePrefix) {
    return new DoubleValuedEventImpl(namePrefix + "//" + getName()+"//" + "(" + getFormattedCreationTS() + ")", getValue());
  }

  public Event createDerivedResultItem(String namePrefix,Event otherEvent,String separator) {
    return new DoubleValuedEventImpl(namePrefix + "//" + getName()+"//" + "(" + getFormattedCreationTS() + ")", getValue());
  }
}
