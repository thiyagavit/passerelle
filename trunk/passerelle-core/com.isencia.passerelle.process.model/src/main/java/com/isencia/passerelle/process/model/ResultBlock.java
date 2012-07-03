package com.isencia.passerelle.process.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;

/**
 * 
 * @author erwin
 *
 */
public interface ResultBlock extends Serializable, Identifiable, AttributeHolder {

  /**
   * @return the creation timestamp of the request
   */
  Date getCreationTS();

  String getType();

  boolean addItem(ResultItem item);

  /**
   * @return all the ResultItems with their natural order
   */
  Set<ResultItem> getAllItems();

  /**
   * @param comparator
   * @return all the ResultItems, ordered by the comparator
   */
  Set<ResultItem> getAllItems(Comparator<ResultItem> comparator);

  /**
   * @param name
   * @return all the ResultItems, with the given name, in their natural order
   */
  Set<ResultItem> getItemsForName(String name);

  /**
   * @param name
   * @return all the ResultItems, with the given name, ordered by the comparator
   */
  Set<ResultItem> getItemsForName(String name, Comparator<ResultItem> comparator);

  /**
   * @param name
   * @return a single distinct ResultItem for the given name. If multiple ResultItems are present with the given name, just returns one of them.
   */
  ResultItem getDistinctItemForName(String name);

}
