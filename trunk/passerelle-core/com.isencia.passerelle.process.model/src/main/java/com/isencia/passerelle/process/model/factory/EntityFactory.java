/**
 * 
 */
package com.isencia.passerelle.process.model.factory;

import java.util.Date;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.AttributeHolder;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
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
  Request createRequest(Case requestCase, String type, String correlationId);

  Task createTask(Context parentContext, String owner, String type);

  ResultBlock createResultBlock(Task task, String type);

  Attribute createAttribute(AttributeHolder request, String name, String value);

  ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Date date);

  ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit);

  ErrorItem createErrorItem(ResultBlock resultBlock, Severity severity, Category category, String code, String shortDescription, String description);

}
