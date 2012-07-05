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

import java.util.Date;

/**
 * @author erwin
 *
 */
public interface ResultItem<V> extends NamedValue<V>, Identifiable, AttributeHolder, Coloured {

  static final String _CREATION_TS = "creationTS";
  static final String _UNIT = "unit";
  static final String _DATA_TYPE = "dataType";
  static final String _RESULT_BLOCK = "resultBlock";
  
  /**
   * This can indicate the timestamp when the result item was created inside a Passerelle process,
   * but can also indicate a historical timestamp, e.g. when the result item represents a measurement
   * result obtained from an external system, containing its own timestamp.
   * 
   * @return the creation timestamp of the item
   */
  Date getCreationTS();
  
	String getUnit();
	
	String getDataType();
	
	ResultBlock getResultBlock();

}
