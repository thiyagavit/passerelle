/**
 * 
 */
package com.isencia.passerelle.process.model.impl.factory;

import java.util.Date;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.AttributeHolder;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextErrorEvent;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ErrorItem.Category;
import com.isencia.passerelle.process.model.ErrorItem.Severity;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.model.impl.CaseImpl;
import com.isencia.passerelle.process.model.impl.ContextEventImpl;
import com.isencia.passerelle.process.model.impl.MainRequestImpl;
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
public class EntityFactoryImpl implements EntityFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.factory.EntityFactory#createCase
	 * (java.lang.String)
	 */
	public Case createCase(String externalReference) {
		return new CaseImpl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.factory.EntityFactory#createRequest
	 * (com.isencia.passerelle.process.model.Case, java.lang.String,
	 * java.lang.String)
	 */
	public Request createRequest(Case requestCase, String initiator, String category, String type, String correlationId) {
		return new MainRequestImpl(requestCase, initiator, type, correlationId, category);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.factory.EntityFactory#createTask
	 * (com.isencia.passerelle.process.model.Context, java.lang.String,
	 * java.lang.String)
	 */
	public Task createTask(Context parentContext, String initiator, String type) {
		return new TaskImpl(parentContext, initiator, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.factory.EntityFactory#createResultBlock
	 * (com.isencia.passerelle.process.model.Task, java.lang.String)
	 */
	public ResultBlock createResultBlock(Task task, String type) {
		return new ResultBlockImpl(task, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.factory.EntityFactory#createAttribute
	 * (com.isencia.passerelle.process.model.AttributeHolder, java.lang.String,
	 * java.lang.String)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.factory.EntityFactory#createResultItem
	 * (com.isencia.passerelle.process.model.ResultBlock, java.lang.String,
	 * java.lang.String, java.lang.String, java.util.Date)
	 */
	public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit,
			Date date) {
		return createResultItem(resultBlock, name, value, unit, null, date);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.factory.EntityFactory#createResultItem
	 * (com.isencia.passerelle.process.model.ResultBlock, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit) {
		return createResultItem(resultBlock, name, value, unit, null, null);
	}

	public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit,
			Integer level, Date date) {

		return new StringResultItemImpl(resultBlock, name, value, unit, date, level);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.process.model.factory.EntityFactory#createResultItem
	 * (com.isencia.passerelle.process.model.ResultBlock, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit,
			Integer level) {
		return createResultItem(resultBlock, name, value, unit, level, null);

	}

  /* (non-Javadoc)
   * @see com.isencia.passerelle.process.model.factory.EntityFactory#createContextEvent(com.isencia.passerelle.process.model.Context, java.lang.String, java.lang.String)
   */
  public ContextEvent createContextEvent(Context context, String topic, String message) {
    return new ContextEventImpl(context, topic, message);
  }

  public ContextErrorEvent createContextErrorEvent(Context context, Severity severity, Category category, String code, String shortDescription, String description) {
    throw new UnsupportedOperationException();
  }

  public ContextErrorEvent createContextErrorEvent(Context context, Severity severity, Category category, String code, String shortDescription, Throwable cause) {
    throw new UnsupportedOperationException();
  }

}
