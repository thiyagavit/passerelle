/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.process.common.util;

import com.isencia.passerelle.process.model.Context;

public class ContextUtils {

  private static final String ADVANCED_PLACEHOLDER_START = "#[context://";
  private static final String SIMPLE_PLACEHOLDER_START = "#[";
  private static final int SIMPLE_PLACEHOLDER_START_LENGTH = SIMPLE_PLACEHOLDER_START.length();
//  private static final int ADVANCED_PLACEHOLDER_START_LENGTH = ADVANCED_PLACEHOLDER_START.length();

  /**
   * If the itemValueOrPlaceHolder contains a placeholder, the item with the path given in the placeholder is retrieved from the context and substituted in the
   * given text. If no such item is found, the placeholder is maintained unchanged. If <tt>itemValueOrPlaceHolder</tt> does not contain a valid place holder, it
   * is returned as result value directly.
   * <p>
   * Two formats are supported :
   * <ul>
   * <li> <tt>#[&lt;itemname&gt;[,&lt;default&gt;]]</tt> : returns the first context item found with the given <tt>itemname</tt>. If not found, return the
   * (optional) <tt>default</tt> value.
   * <li> TODO <tt>#[context://[&lt;resultblocktype&gt;/]&lt;itemname&gt;[,&lt;default&gt;]]</tt> : returns the first context item with the given <tt>itemname</tt>
   * from the (optional) given <tt>resultblocktype</tt>. If not found, return the (optional) <tt>default</tt> value.
   * </ul>
   * Examples :
   * <ul>
   * <li><tt>"Hello Mr. #[context://CustomerInfo/lastName] and goodbye"</tt> : could become <tt>"Hello Mr. Smith and goodbye"</tt>
   * <li><tt>"Hello Mr. #[lastName] and goodbye"</tt> : could also become <tt>"Hello Mr. Smith and goodbye"</tt>
   * <li><tt>"Hello Mr. #[lastName] and goodbye"</tt> : would remain <tt>"Hello Mr. #[lastName] and goodbye"</tt> when no <tt>lastName</tt> was found in the
   * given context.
   * <li><tt>"Hello Mr. #[lastName, MacDonald] and goodbye"</tt> : could become <tt>"Hello Mr. MacDonald and goodbye"</tt> when no <tt>lastName</tt> was found
   * in the given context.
   * </ul>
   * REMARK : no support yet for multiple placeholders or recursive substitutions in one invocation
   * TODO integrate with fully-featured PropertyPlaceHolderService to be pushed down from Passerelle EDM.
   * 
   * @param context
   * @param itemValueOrPlaceHolder
   * @return
   */
  public static String lookupValueForPlaceHolder(Context context, String itemValueOrPlaceHolder) {
    int phStart = itemValueOrPlaceHolder.indexOf(SIMPLE_PLACEHOLDER_START);
    int phEnd = itemValueOrPlaceHolder.indexOf(']', phStart);
    if (phStart >= 0 && phEnd >= 0 && phStart < phEnd) {
      // it's a property place holder like thing
      int phStart2 = itemValueOrPlaceHolder.indexOf(ADVANCED_PLACEHOLDER_START);
      if (phStart2 >= 0 && phStart2 < phEnd) {
        // it's the advanced syntax thing
        // not implemented yet
      } else {
        // it's a simple syntax thing
        String itemName = itemValueOrPlaceHolder.substring(phStart + SIMPLE_PLACEHOLDER_START_LENGTH, phEnd);
        String lookupValue = context.lookupValue(itemName);
        return lookupValue != null ? itemValueOrPlaceHolder.replace(SIMPLE_PLACEHOLDER_START + itemName + "]", lookupValue) : itemValueOrPlaceHolder;
      }
    }
    return itemValueOrPlaceHolder;
  }
}
