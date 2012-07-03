package com.isencia.passerelle.process.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Events for Passerelle are light-weight objects that can e.g. be used for high-throughput CEP scenarios.
 * <p>
 * This implies :
 * <ul>
 * <li>They have a (start) timestamp</li>
 * <li>They have an optional duration (i.e. could be 0)</li>
 * <li>They have a name, just because that's easy to differentiate them, refer to them etc</li>
 * <li>They are not persisted entities or anything heavy like that...</li>
 * </ul>
 * </p>
 * Implementations can add "content" as necessary, besides the pure time info... <br/>
 * 
 * @author delerw
 */
public interface Event extends Serializable {
  /**
   * @return the attribute's name
   */
  String getName();

  /**
   * @return the creation timestamp of the event
   */
  Date getCreationTS();

  /**
   * When this event represents a temporary "situation", the duration identifies the time (in ms), from the creationTS, that the situation will remain the same.
   * For the vast majority of items, this will be irrelevant, in which case it will just return 0.
   * 
   * @return the duration in ms
   */
  long getDuration();
}
