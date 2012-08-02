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

package com.isencia.passerelle.domain.et.impl;

import org.slf4j.Logger;
import ptolemy.actor.Actor;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.domain.et.Event;
import com.isencia.passerelle.domain.et.EventHandler;

/**
 * @author delerw
 */
public abstract class AbstractEventHandler implements EventHandler {
  private ETDirector director;

  public AbstractEventHandler(ETDirector director) {
    this.director = director;
  }

  public void initialize() {
  }

  public boolean handle(Event event) throws Exception {
    Actor actor = getDestinationActorFromEvent(event);
    synchronized (actor) {
      if (director.isActorIterating(actor)) {
        getLogger().debug("Skipping {} - Actor {} is busy.", event, actor.getName());
        return false;
      } else if (director.isActorInactive(actor)) {
        getLogger().debug("Skipping {} - Actor {} is inactive.", event, actor.getName());
        return false;
      } else {
        director.notifyActorIteratingForEvent(actor, event);
      }
    }
    try {
      getLogger().debug("Handling {} - iterating Actor {}.", event, actor.getName());
      if (actor.prefire()) {
        actor.fire();
        if (!actor.postfire()) {
          director.notifyActorInactive(actor);
        }
      }
      return true;
    } finally {
      director.notifyActorDoneIteratingForEvent(actor, event);
    }
  }

  protected abstract Logger getLogger();
  protected abstract Actor getDestinationActorFromEvent(Event event);
}
