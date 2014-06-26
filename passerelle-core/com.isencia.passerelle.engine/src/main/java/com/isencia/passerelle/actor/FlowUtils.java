package com.isencia.passerelle.actor;

import com.isencia.passerelle.model.Flow;

import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class FlowUtils {


	public static final String FLOW_SEPARATOR = "#sep";

	/**
	 * TODO check if it is not better to reuse an existing parameter than to recreate it.
	 * TODO add extra type checks on value and create corresponding specialized parameters.
	 * @param flow
	 * @param name
	 * @param value
	 * @return the new parameter instance
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
  public static Parameter addOrUpdateParameter(Flow flow, String name, Object value) throws IllegalActionException, NameDuplicationException {
    Parameter result = null;
    if (value instanceof String) {
      Attribute attribute = flow.getAttribute(name, StringParameter.class);
      if (attribute != null) {
        attribute.setContainer(null);
      }
      result = new StringParameter(flow, name);
      result.setToken((String) value);
    } else {
      Attribute attribute = flow.getAttribute(name, Parameter.class);
      if (attribute != null) {
        attribute.setContainer(null);
      }
      result = new Parameter(flow, name, new ObjectToken(value));
    }
    return result;
  }

	public static String extractFlowName(NamedObj actor) {
		String fullName = actor.getFullName();

		if (fullName.contains(FLOW_SEPARATOR)) {
			return fullName.split(FLOW_SEPARATOR)[0];
		}
		return actor.toplevel().getName();
	}

	public static String generateUniqueFlowName(String name, String uniqueIndex) {
		String fullName = null;
		if (name.contains(FLOW_SEPARATOR)) {
		  fullName =  name.split(FLOW_SEPARATOR)[0];
    }else{
      fullName = name;
    }
	  
	  StringBuffer sb = new StringBuffer(fullName);
		sb.append(FLOW_SEPARATOR);
		sb.append(uniqueIndex);
		return sb.toString();
	}

	public static String generateUniqueFlowName(String name) {

		return generateUniqueFlowName(name, Long.toString(System.currentTimeMillis()));
	}

	public static String getFullNameWithoutFlow(NamedObj no) {
		NamedObj container = no.toplevel();
		return no.getFullName().substring(container.getName().length() + 1);
	}

	public static String getOriginalFullName(NamedObj no) {
		String fullNameWithoutFlow = getFullNameWithoutFlow(no);
		StringBuffer sb = new StringBuffer(extractFlowName(no));
		sb.append(fullNameWithoutFlow);
		return sb.toString();
	}

}
