package com.isencia.passerelle.process.actor.trial;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.service.RequestProcessingService;
import com.isencia.passerelle.process.service.ServiceRegistry;
import com.isencia.util.FutureValue;

public class MockRequestProcessingService implements RequestProcessingService {

  private String serviceType;
  private Map<String, String> resultItems;

  public MockRequestProcessingService(String serviceType, Map<String, String> resultItems) {
    this.serviceType = serviceType;
    this.resultItems = resultItems;
  }

  @Override
  public Future<Context> process(Context taskContext, Long timeout, TimeUnit unit) {
    if (serviceType==null || !serviceType.equalsIgnoreCase(taskContext.getRequest().getType())) {
      return null;
    } else {
      try {
        ProcessFactory entityFactory = ServiceRegistry.getInstance().getProcessFactory();
        ResultBlock rb = entityFactory.createResultBlock((Task) taskContext.getRequest(), serviceType);
        for (Entry<String, String> item : resultItems.entrySet()) {
          entityFactory.createResultItem(rb, item.getKey(), item.getValue(), null);
        }
      } catch (Exception e) {
        ServiceRegistry.getInstance().getContextManager().notifyError(taskContext, e);
      }
      ServiceRegistry.getInstance().getContextManager().notifyFinished(taskContext);
      return new FutureValue<Context>(taskContext);
    }
  }
}
