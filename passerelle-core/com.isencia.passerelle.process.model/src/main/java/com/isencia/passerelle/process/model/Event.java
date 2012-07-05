/* Copyright 2012 - iSencia Belgium NV

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
 * <li>They have a topic, just because that's easy to differentiate them, refer to them etc</li>
 * <li>They are not persisted entities or anything heavy like that...</li>
 * </ul>
 * </p>
 * Implementations can add "content" as necessary, besides the pure time info... <br/>
 * 
 * @author delerw
 */
public interface Event extends Serializable {
  /**
   * @return the event's topic
   */
  String getTopic();

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
  Long getDuration();
 }
