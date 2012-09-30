/**
 * 
 */
package com.isencia.passerelle.process.model.factory;

import java.util.Date;
import java.util.Set;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.AttributeHolder;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextErrorEvent;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.ErrorItem.Category;
import com.isencia.passerelle.process.model.ErrorItem.Severity;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;

/**
 * @author "puidir"
 *
 */
public interface EntityFactory {

  /**
   * Create a new Case.
   * 
   * @param externalReference Can be used to link the Case to external system, e.g. order identifiers, client numbers, etc
   */
  Case createCase(String externalReference);

  /**
   * Create a Request.
   * 
   * @param requestCase Requests are always linked to a Case
   * @param type
   * @param correlationId
   */
  Request createRequest(Case requestCase, String initiator,String category, String type, String correlationId);

  Task createTask(Context parentContext, String initiator, String type);

  ResultBlock createResultBlock(Task task, String type);

  Attribute createAttribute(AttributeHolder holder, String name, String value);

  ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Date date);

  ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit);

  ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit,Integer level);
  
  ErrorItem createErrorItem(Severity severity, Category category, String code, String shortDescription, Throwable cause, Set<String> relatedDataTypes);
  ErrorItem createErrorItem(Severity severity, Category category, String code, String shortDescription, String description, Set<String> relatedDataTypes);
  
  ContextErrorEvent createContextErrorEvent(Context context, ErrorItem errorItem);
  ContextErrorEvent createContextErrorEvent(Context context, Severity severity, Category category, String code, String shortDescription, String description, Set<String> relatedDataTypes);
  ContextErrorEvent createContextErrorEvent(Context context, Severity severity, Category category, String code, String shortDescription, Throwable cause, Set<String> relatedDataTypes);

  ContextEvent createContextEvent(Context context, String topic, String message);
}
