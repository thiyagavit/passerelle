/**
 * 
 */
package com.isencia.passerelle.process.model.impl.factory;

import java.util.Date;
import java.util.Set;

import com.isencia.passerelle.core.ErrorCategory;
import com.isencia.passerelle.core.ErrorCode.Severity;
import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.AttributeHolder;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextErrorEvent;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.RawResultBlock;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.impl.CaseImpl;
import com.isencia.passerelle.process.model.impl.ContextEventImpl;
import com.isencia.passerelle.process.model.impl.ErrorItemImpl;
import com.isencia.passerelle.process.model.impl.MainRequestImpl;
import com.isencia.passerelle.process.model.impl.RawResultBlockImpl;
import com.isencia.passerelle.process.model.impl.RequestAttributeImpl;
import com.isencia.passerelle.process.model.impl.ResultBlockAttributeImpl;
import com.isencia.passerelle.process.model.impl.ResultBlockImpl;
import com.isencia.passerelle.process.model.impl.ResultItemAttributeImpl;
import com.isencia.passerelle.process.model.impl.StringResultItemImpl;
import com.isencia.passerelle.process.model.impl.TaskImpl;

/**
 * @author "puidir"
 * 
 */
public class ProcessFactoryImpl implements ProcessFactory {

  public Case createCase(String externalReference) {
    return new CaseImpl();
  }

  public Request createRequest(Case requestCase, String initiator, String category, String type, String correlationId) {
    return new MainRequestImpl(requestCase, initiator, type, correlationId, category);
  }

  public Task createTask(Context parentContext, String initiator, String type) {
    return new TaskImpl(parentContext, initiator, type);
  }
  
  @Override
	public Task createTask(Class<? extends Task> taskClass, Context parentContext, String initiator, String type) throws Exception {
	  	if (taskClass == null)
	  		return(createTask(parentContext, initiator, type));
		return taskClass.getConstructor(Context.class, String.class, String.class).newInstance(parentContext, initiator, type);
	}

  public ResultBlock createResultBlock(Task task, String type) {
    return new ResultBlockImpl(task, type);
  }
  public ResultBlock createResultBlock(Task task, String type,Date date) {
    return new ResultBlockImpl(task, type,date);
  }
  
  public RawResultBlock createRawResultBlock(Task task, String type) {
    return new RawResultBlockImpl(task, type);
  }

  public Attribute createAttribute(AttributeHolder holder, String name, String value) {
    if (holder == null) {
      throw new IllegalArgumentException("AttributeHolder can not be null");
    }
    if (holder instanceof Request) {
      return new RequestAttributeImpl((Request) holder, name, value);
    } else if (holder instanceof ResultBlock) {
      return new ResultBlockAttributeImpl((ResultBlock) holder, name, value);
    } else if (holder instanceof ResultItem) {
      return new ResultItemAttributeImpl((ResultItem<?>) holder, name, value);
    } else {
      throw new IllegalArgumentException("Unknown AttributeHolder type " + holder.getClass());
    }
  }

  public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Date date) {
    return createResultItem(resultBlock, name, value, unit, null, date);
  }

  public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit) {
    return createResultItem(resultBlock, name, value, unit, null, null);
  }

  public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Integer level, Date date) {
    return new StringResultItemImpl(resultBlock, name, value, unit, date, level);
  }

  public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Integer level) {
    return createResultItem(resultBlock, name, value, unit, level, null);
  }

  public ContextEvent createContextEvent(Context context, String topic, String message) {
    return new ContextEventImpl(context, topic, message);
  }

  public ContextErrorEvent createContextErrorEvent(Context context, ErrorItem errorItem) {
    throw new UnsupportedOperationException();
  }

  public ContextErrorEvent createContextErrorEvent(Context context, Severity severity, ErrorCategory category, String code, String shortDescription, String description, Set<String> relatedDataTypes) {
    throw new UnsupportedOperationException();
  }

  public ContextErrorEvent createContextErrorEvent(Context context, Severity severity, ErrorCategory category, String code, String shortDescription, Throwable cause, Set<String> relatedDataTypes) {
    throw new UnsupportedOperationException();
  }

  public ErrorItem createErrorItem(Severity severity, ErrorCategory category, String code, String shortDescription, Throwable cause, Set<String> relatedDataTypes) {
    return new ErrorItemImpl(severity, category, code, shortDescription, cause, relatedDataTypes);
  }

  public ErrorItem createErrorItem(Severity severity, ErrorCategory category, String code, String shortDescription, String description, Set<String> relatedDataTypes) {
    return new ErrorItemImpl(severity, category, code, shortDescription, description, relatedDataTypes);
  }

  public Request createRequest(Case requestCase, String initiator, String executor, String category, String type, String correlationId) {
    return new MainRequestImpl(requestCase, initiator,executor, type, correlationId, category);
  }
}
