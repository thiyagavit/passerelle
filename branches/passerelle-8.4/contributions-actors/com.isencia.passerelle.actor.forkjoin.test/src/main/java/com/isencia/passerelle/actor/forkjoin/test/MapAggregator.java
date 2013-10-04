package com.isencia.passerelle.actor.forkjoin.test;

import java.util.Map;
import com.isencia.passerelle.actor.forkjoin.AggregationStrategy;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;

public class MapAggregator implements AggregationStrategy {

  @Override
  public ManagedMessage aggregateMessages(ManagedMessage initialMsg, ManagedMessage... otherMessages) throws MessageException {
    ManagedMessage aggregatedMessage = MessageFactory.getInstance().createCausedCopyMessage(initialMsg);
    try {
      Map aggrMap = (Map) aggregatedMessage.getBodyContent();
      if (otherMessages != null) {
        for (ManagedMessage otherMsg : otherMessages) {
          Map oMap = (Map) otherMsg.getBodyContent();
          aggrMap.putAll(oMap);
        }
      }
    } catch (ClassCastException e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Unexpected msg contents", e);
    }
    return aggregatedMessage;
  }
}
