package com.isencia.passerelle.domain.et;

import java.text.DateFormat;
import java.util.Date;

abstract class AbstractEvent implements Event {

  private Date timeStamp;
  
  protected AbstractEvent(Date timeStamp) {
    this.timeStamp = timeStamp;
  }

  @Override
  public Date getTimestamp() {
    return timeStamp;
  }

  public abstract String toString(DateFormat dateFormat);
}
