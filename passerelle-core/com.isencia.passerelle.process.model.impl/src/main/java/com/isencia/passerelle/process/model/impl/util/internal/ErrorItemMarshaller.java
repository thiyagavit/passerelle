package com.isencia.passerelle.process.model.impl.util.internal;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.isencia.passerelle.core.ErrorCategory;
import com.isencia.passerelle.core.ErrorCode.Severity;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.impl.ErrorItemImpl;

public class ErrorItemMarshaller {
  
  private static final String DESCR = "descr";
  private static final String SHORT_DESCR = "shortDescr";
  private static final String CODE = "code";
  private static final String CATEGORY = "category";
  private static final String SEVERITY = "severity";
  private static final String RELATED_DATATYPE = "relatedDataType";
  
  public static String marshallErrorInfo(ErrorItem errorItem) throws Exception {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(SEVERITY, errorItem.getSeverity().name());
    jsonObject.put(CATEGORY, errorItem.getCategory().name());
    jsonObject.put(CODE, errorItem.getCode());
    // it seems Set<String> is not correctly marshalled/unmarshalled
    // so we create a single concatenated string
    StringBuilder dtBldr = new StringBuilder();
    for (String dt : errorItem.getRelatedDataTypes()) {
      dtBldr.append(dt + ",");
    }
    jsonObject.put(RELATED_DATATYPE, dtBldr.toString());
    jsonObject.put(SHORT_DESCR, errorItem.getShortDescription());
    jsonObject.put(DESCR, errorItem.getDescription());
    String msg = jsonObject.toString();
    return msg;
  }

  public static ErrorItem unmarshallErrorInfo(String marshalledInfo) {
    ErrorItem errorItem = null;
    JSONObject jsonObject = null;
    if (marshalledInfo != null) {
      try {
        jsonObject = new JSONObject(marshalledInfo);
        Severity severity = Severity.ERROR;
        try {
          severity = Severity.valueOf(getJsonString(jsonObject, SEVERITY, Severity.ERROR.name()));
        } catch (Exception e) {//NOSONAR
          // ignore; may go wrong when loading historical data with other severity names, but we don't want to log that
        }
        ErrorCategory category = ErrorCategory.FUNCTIONAL;
        try {
          category = ErrorCategory.valueOf(getJsonString(jsonObject, CATEGORY, ErrorCategory.FUNCTIONAL.name()));
        } catch (Exception e) { //NOSONAR
          // ignore; may go wrong when loading historical data with other category names, but we don't want to log that
        }
        String code = getJsonString(jsonObject, CODE, "5010");
        String shortDescription = getJsonString(jsonObject, SHORT_DESCR, "Backend request returned a warning");
        String description = getJsonString(jsonObject, DESCR, "");
        Set<String> relatedDataTypes = new HashSet<String>();
        try {
          String dtStr = getJsonString(jsonObject, RELATED_DATATYPE, "unknown");
          String[] dts = dtStr.split(",");
          for (int i = 0; i < dts.length; ++i) {
            relatedDataTypes.add(dts[i]);
          }
        } catch (Exception e) { //NOSONAR
          // ignore, as this info is not available for old/historical error items
          // and we don't want to break compatibility.
        }
        errorItem = new ErrorItemImpl(severity, category, code, shortDescription, description, relatedDataTypes);
      } catch (JSONException e) { //NOSONAR
        // no logging, it is not guaranteed that the event msg represents an error item so...
      }
    }
    return errorItem;
  }

  private static String getJsonString(JSONObject jsonObject, String key, String defaultValue) {
    try {
      return jsonObject.getString(key);
    } catch (Exception e) {
      return defaultValue;
    }
  }
}
