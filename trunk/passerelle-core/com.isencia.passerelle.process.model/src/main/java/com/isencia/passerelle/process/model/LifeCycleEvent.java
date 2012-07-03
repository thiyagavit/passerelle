package com.isencia.passerelle.process.model;

import java.util.Date;

/**
 * @author delerw
 */
public interface LifeCycleEvent extends Event, Comparable<LifeCycleEvent> {
  /**
   * @return the creation date of the event
   */
  Date getCreationTS();

  /**
   * @return the event type, typically these are <code>Status</code> change indicators
   */
  String getEventType();

  /**
   * @return the event message, typically empty except for ERROR events, where error info is then stored in here.
   */
  String getMessage();

  /**
   * 
   * @return the associated entity
   */
  LifeCycleEntity getEntity();
}