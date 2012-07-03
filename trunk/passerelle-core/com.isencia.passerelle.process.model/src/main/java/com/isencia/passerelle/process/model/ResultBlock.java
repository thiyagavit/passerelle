/* Copyright 2012 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
