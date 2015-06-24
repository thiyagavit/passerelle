package com.isencia.passerelle.actor.forkjoin.test;

import java.util.HashMap;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.forkjoin.Fork;
import com.isencia.passerelle.message.MessageException;

public class TestForkForMaps extends Fork {

  public TestForkForMaps(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    setAggregationStrategy(new MapAggregator());
  }
  
  @Override
  protected Object cloneScopeMessageBodyContent(Object scopeMessageBodyContent) throws MessageException {
    if(scopeMessageBodyContent instanceof HashMap<?, ?>) {
      HashMap<?,?> o = (HashMap<?,?>) scopeMessageBodyContent;
      return o.clone();
    } else {
      return super.cloneScopeMessageBodyContent(scopeMessageBodyContent);
    }
  }
}
