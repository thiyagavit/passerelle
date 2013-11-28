package com.isencia.passerelle.process.service;

import org.apache.commons.lang.StringUtils;

import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Context;

public abstract class AbstractAdapter implements Adapter {

  public static interface ParameterHandlerTemplate {
    boolean handleParameter(String name, String value);
  }

  /**
   * Looks for the parameter with the given key, in the given context's request, obtains
   * its value and, if it is not null and not empty, returns the value.
   * 
   * If a not-null value is not found, throws a ServiceException.
   * 
   * @param key
   * @param context
   * @return the non-empty value of the task parameter with the given key/name
   * @throws ServiceException when the parameter with a non-empty value can not be found.
   */
  protected final String getMandatoryParameterValue(String key, Context context) throws AdapterException {
    String value = getOptionalParameterValue(key, context);

    if (value == null) {
      throw new AdapterException(ErrorCode.INCOMPLETE_INPUT_ARGUMENTS,
          "No valid " + key + " for context " + context.getId());
    }
    
    return value;
  }

  /**
   * Looks for the parameter with the given key, in the given context's request, obtains
   * its value and, if it is not null and not empty, returns it. <br/>
   * If the parameter is not found, or has an empty/null value, returns null. <br/>
   * REMARK : read the above carefully! Parameters with empty value (i.e. "")
   * also return null!
   * 
   * @param key
   * @param context
   * @return the non-empty value of the parameter or null
   */
  protected final String getOptionalParameterValue(String key, Context context) {
    return getOptionalParameterValue(key, context, null);
  }

  /**
   * Looks for the parameter with the given key, in the given context's request, obtains
   * its value and, if it is not null and not empty, returns it. <br/>
   * If the parameter is not found, or has an empty/null value, returns the defaultValue. <br/>
   * @param key
   * @param context
   * @param defaultValue
   * @return
   */
  protected final String getOptionalParameterValue(String key, Context context, String defaultValue) {
    if (key == null || context == null) {
      throw new IllegalArgumentException("key or context is null");
    }

    Attribute parameter = context.getRequest().getAttribute(key);
    
    String value = null;

    if (parameter != null) {
      value = parameter.getValue();
    }
    
    // we allow values with only spaces in it
    if (StringUtils.isNotEmpty(value)) {
      return value;
    } else {
      return defaultValue;
    }
  }

  /**
   * 
   * @param key
   * @param context
   * @param handler
   * @throws ServiceException
   *             when the attribute cannot be applied successfully
   */
  protected final void applyMandatoryParameter(String key, Context context, ParameterHandlerTemplate handler) throws AdapterException {
    boolean success = applyOptionalParameter(key, context, handler);
    if (!success) {
      throw new AdapterException(
          ErrorCode.INCOMPLETE_INPUT_ARGUMENTS,
          "No valid " + key + " for context " + context.getId());
    }
  }

  /**
   * Looks for the parameter with the given key, in the given context's request, obtains
   * its value and, if it is not null and not empty, passes it to the handler.
   * 
   * @param key
   * @param context
   * @return true if the parameter was found and handled successfully, false
   *         otherwise
   */
  protected final boolean applyOptionalParameter(String key, Context context, ParameterHandlerTemplate handler) {
    
    if (key == null || context == null || handler == null) {
      return false;
    }

    Attribute parameter = context.getRequest().getAttribute(key);

    String value = null;

    if (parameter != null) {
      value = parameter.getValue();
    }

    if (StringUtils.isNotBlank(value)) {
      return handler.handleParameter(key, value);
    } else {
      return false;
    }
  }

}
