package com.isencia.passerelle.actor;

import ptolemy.kernel.util.NamedObj;

public class FlowUtils {
	public static final String FLOW_SEPARATOR = "#sep";
	public static final String TRANSLATION_ID = "translationId";

	public static String extractFlowName(NamedObj actor) {
		String fullName = actor.getFullName();

		if (fullName.contains(FLOW_SEPARATOR)) {
			return fullName.split(FLOW_SEPARATOR)[0];
		}
		return getTopLevel(actor).getName();
	}

	public static String generateTranslationKey(NamedObj actor, String key) {
		if (actor == null){
			return null;
		}

		StringBuffer sb = new StringBuffer(getFullNameWithoutFlow(actor));
		
		if (key != null) {
			sb.append(".");
			sb.append(key);
		}
		return sb.substring(1);

	}

	public static String generateUniqueFlowName(String name) {
		StringBuffer sb = new StringBuffer(name);
		sb.append(FLOW_SEPARATOR);
		sb.append(System.currentTimeMillis());
		return sb.toString();
	}

	public static String getFullNameWithoutFlow(NamedObj no) {
		NamedObj container = getTopLevel(no);
		return no.getFullName().substring(container.getName().length() + 1);
	}

	public static NamedObj getTopLevel(NamedObj no) {
		if (no.getContainer() == null) {
			return no;
		}
		return getTopLevel(no.getContainer());
	}
}
