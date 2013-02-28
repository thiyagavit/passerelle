package com.isencia.passerelle.process.model.impl.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.EntityManager;
import com.isencia.passerelle.process.model.impl.CaseImpl;
import com.isencia.passerelle.process.model.impl.ContextImpl;
import com.isencia.passerelle.process.model.impl.RequestImpl;

public class EntityManagerImpl implements EntityManager {
  private Long keyGenerator = new Long(DateUtils.format(new Date(), "yyyyMMdd") + "000000000");

  private Map<Long, Case> casesById = new HashMap<Long, Case>();
  private Map<Long, Request> requestsById = new HashMap<Long, Request>();
  private Map<String, Request> requestsByCorrelationId = new HashMap<String, Request>();

  public Case persistCase(Case caze) {
    if (caze != null) {
      if (caze.getId() == null) {
        ((CaseImpl) caze).setId(keyGenerator++);
      }
      casesById.put(caze.getId(), caze);
    }
    return caze;
  }

  public Request persistRequest(Request request) {
    if (request != null) {
      if (request.getId() == null) {
        ((RequestImpl) request).setId(keyGenerator++);
        persistContext(request.getProcessingContext());
      }
      requestsById.put(request.getId(), request);
      requestsByCorrelationId.put(request.getCorrelationId(), request);
    }
    return request;
  }

  public Context persistContext(Context context) {
    if (context != null && context.getId() == null) {
      ((ContextImpl) context).setId(keyGenerator++);
      persistRequest(context.getRequest());
    }
    return context;
  }

  public ResultBlock mergeResultBlock(ResultBlock block) {
    return block;
  }

  public Context mergeContext(Context context) {
    return context;
  }

  public Context mergeWithBranchedContexts(Context context, Collection<Context> branches) {
    for (Context branch : branches) {
      context.join(branch);
    }
    return context;
  }

  public Case getCase(Long id) {
    return casesById.get(id);
  }

  public Request getRequest(Request request) {
    return request;
  }

  public Request getRequest(Long requestId) {
    return requestsById.get(requestId);
  }

  public Task getTask(Long taskId) {
    return (Task) requestsById.get(taskId);
  }

  public Task getTask(Long taskId, boolean bypassCache) {
    return getTask(taskId);
  }

  public Context getContext(Context context) {
    return context;
  }

  public Context getContext(Context context, boolean bypassCache) {
    return context;
  }

  public Request getRequest(String correlationId) {
    return requestsByCorrelationId.get(correlationId);
  }

  public Request persistCorrelatedRequest(Request request) {
    return null;
  }

  public List<Task> getTasksForCase(Long caseId, Long excludedRequestId) {
    return new ArrayList<Task>();
  }

  public List<Request> getRequestsForCase(Long caseId, Long excludedRequestId) {
    return new ArrayList<Request>();
  }

  public List<Request> getRequestsForCase(Request requestToExclude) {
    return new ArrayList<Request>();
  }

  public List<Request> getRequestsForCase(Request requestToExclude, Collection<String> requestTypes) {
    return new ArrayList<Request>();
  }

  public List<Task> getTasksForContext(Context context) {
    return context.getTasks();
  }

  public List<ErrorItem> getErrorsForRequest(Long requestId) {
    return new ArrayList<ErrorItem>();
  }

  public <T extends Serializable> T refresh(T entity) {
    return entity;
  }

  public List<ResultBlock> getResultBlocks(Long caseId, Long requestId, Long taskId, 
      Collection<Long> resultBlockIds, Collection<String> taskTypes, Collection<String> resultBlockTypes) {
    return new ArrayList<ResultBlock>();
  }
}
