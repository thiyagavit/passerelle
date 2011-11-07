package com.isencia.passerelle.domain.et;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import ptolemy.actor.Actor;

public class FireEvent extends AbstractEvent {

  private static final long serialVersionUID = 5232545627399145043L;
  
  private Actor target;
  
  public FireEvent(Actor target) {
    super(new Date());
    this.target = target;
  }

  public FireEvent(Actor target, Date timeStamp) {
    super(timeStamp);
    this.target = target;
  }

  public Actor getTarget() {
    return target;
  }

  @Override
  public String toString() {
    return toString(new SimpleDateFormat());
  }
  
  public String toString(DateFormat dateFormat) {
    return "FireEvent [timeStamp=" + dateFormat.format(getTimestamp()) + ", target=" + target.getFullName() + "]";
  }
}
