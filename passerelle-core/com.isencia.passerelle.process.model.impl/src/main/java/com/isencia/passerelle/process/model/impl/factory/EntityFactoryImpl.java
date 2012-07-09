/**
 * 
 */
package com.isencia.passerelle.process.model.impl.factory;

import java.util.Date;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.AttributeHolder;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.ErrorItem.Category;
import com.isencia.passerelle.process.model.ErrorItem.Severity;
import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.model.impl.CaseImpl;
import com.isencia.passerelle.process.model.impl.ErrorItemImpl;
import com.isencia.passerelle.process.model.impl.RequestAttributeImpl;
import com.isencia.passerelle.process.model.impl.RequestImpl;
import com.isencia.passerelle.process.model.impl.ResultBlockAttributeImpl;
import com.isencia.passerelle.process.model.impl.ResultBlockImpl;
import com.isencia.passerelle.process.model.impl.ResultItemAttributeImpl;
import com.isencia.passerelle.process.model.impl.StringResultItemImpl;
import com.isencia.passerelle.process.model.impl.TaskImpl;

/**
 * @author "puidir"
 *
 */
public class EntityFactoryImpl implements EntityFactory {

  /* (non-Javadoc)
   * @see com.isencia.passerelle.process.model.factory.EntityFactory#createCase(java.lang.String)
   */
  public Case createCase(String externalReference) {
    return new CaseImpl();
  }

  /* (non-Javadoc)
   * @see com.isencia.passerelle.process.model.factory.EntityFactory#createRequest(com.isencia.passerelle.process.model.Case, java.lang.String, java.lang.String)
   */
  public Request createRequest(Case requestCase,String initiator,String executor, String type, String correlationId) {
    return new RequestImpl(requestCase, initiator, type, correlationId,executor);
  }

  /* (non-Javadoc)
   * @see com.isencia.passerelle.process.model.factory.EntityFactory#createTask(com.isencia.passerelle.process.model.Context, java.lang.String, java.lang.String)
   */
  public Task createTask(Context parentContext, String initiator, String type) {
    return new TaskImpl(parentContext, initiator, type);
  }

  /* (non-Javadoc)
   * @see com.isencia.passerelle.process.model.factory.EntityFactory#createResultBlock(com.isencia.passerelle.process.model.Task, java.lang.String)
   */
  public ResultBlock createResultBlock(Task task, String type) {
    return new ResultBlockImpl(task, type);
  }

  /* (non-Javadoc)
   * @see com.isencia.passerelle.process.model.factory.EntityFactory#createAttribute(com.isencia.passerelle.process.model.AttributeHolder, java.lang.String, java.lang.String)
   */
  public Attribute createAttribute(AttributeHolder holder, String name, String value) {
    if(holder==null) {
      throw new IllegalArgumentException("AttributeHolder can not be null");
    }
    if(holder instanceof Request) {
      return new RequestAttributeImpl((Request)holder, name, value);
    } else if(holder instanceof ResultBlock) {
      return new ResultBlockAttributeImpl((ResultBlock)holder, name, value);
    } else if(holder instanceof ResultItem) {
      return new ResultItemAttributeImpl((ResultItem<?>)holder, name, value);
    } else {
      throw new IllegalArgumentException("Unknown AttributeHolder type " + holder.getClass());
    }
  }

  /* (non-Javadoc)
   * @see com.isencia.passerelle.process.model.factory.EntityFactory#createResultItem(com.isencia.passerelle.process.model.ResultBlock, java.lang.String, java.lang.String, java.lang.String, java.util.Date)
   */
  public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Date date) {
    return new StringResultItemImpl(resultBlock, name, value, unit, date);
  }

  /* (non-Javadoc)
   * @see com.isencia.passerelle.process.model.factory.EntityFactory#createResultItem(com.isencia.passerelle.process.model.ResultBlock, java.lang.String, java.lang.String, java.lang.String)
   */
  public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit) {
    return new StringResultItemImpl(resultBlock, name, value, unit, null);
  }

  /* (non-Javadoc)
   * @see com.isencia.passerelle.process.model.factory.EntityFactory#createErrorItem(com.isencia.passerelle.process.model.ResultBlock, com.isencia.passerelle.process.model.ErrorItem.Severity, com.isencia.passerelle.process.model.ErrorItem.Category, java.lang.String, java.lang.String, java.lang.String)
   */
  public ErrorItem createErrorItem(ResultBlock resultBlock, Severity severity, Category category, String code, String shortDescription, String description) {
    ErrorItem item = new ErrorItemImpl(severity, category, code, shortDescription, description);
    if (resultBlock != null) {
      // FIXME: implement if you want to see those errors!
      // ((ResultBlockImpl)resultBlock).addErrorItem(item);
    }
    return item;
  }

}
