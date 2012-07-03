package com.isencia.passerelle.process.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Basic interface for entities with a status and life cycle.
 * 
 * @author delerw
 */
public interface LifeCycleEntity extends Serializable, Identifiable {

  /**
   * @return current status of this entity
   */
  String getStatus();

  /**
   * @param status
   */
  void setStatus(String status);

  /**
   * @return the list of all events that have happened in this entity's lifecycle up-to "now"
   */
  List<LifeCycleEvent> getEvents();

  /**
   * @return The most recent event on this entity
   */
  LifeCycleEvent getMostRecentEvent();

  /**
   * Is the task still processing or not
   * 
   * @return
   */
  boolean isFinished();

  /**
   * @return the end time stamp
   */
  Date getEndTS();

  /**
   * Set the end time stamp
   * 
   * @param endTS the end time stamp
   */
  void setEndTS(Date endTS);

  /**
   * @return The entity's duration (in milliseconds)
   */
  Long getDuration();

  /**
   * Set the entity's duration
   * 
   * @param duration the entity's duration (in milliseconds)
   */
  void setDuration(Long duration);

}
