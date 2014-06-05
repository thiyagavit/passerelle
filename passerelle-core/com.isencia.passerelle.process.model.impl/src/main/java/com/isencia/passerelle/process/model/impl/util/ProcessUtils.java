package com.isencia.passerelle.process.model.impl.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProcessUtils {
	/**
	 * determines if a list has been initialized.
	 * Lists in process entities are by default initialized as Collections.EMPTY_LIST.
	 * This method determines if the list has really been initialized,
	 * either through a load from the database or an add. 
	 * 
	 * @param list
	 * @return true or false
	 */
	public static boolean isInitialized(List<?> list) {
		return(list != null && list != Collections.EMPTY_LIST);
	}

	/**
	 * determines if a set has been initialized.
	 * Sets in process entities are by default initialized as Collections.EMPTY_SET.
	 * This method determines if the set has really been initialized,
	 * either through a load from the database or an add. 
	 * 
	 * @param set
	 * @return true or false
	 */
	public static boolean isInitialized(Collection<?> set) {
		return(set != null && set != Collections.EMPTY_SET);
	}
	
	/**
	 * determines if a map has been initialized.
	 * Maps in process entities are by default initialized as Collections.EMPTY_MAP.
	 * This method determines if the map has really been initialized,
	 * either through a load from the database or a put. 
	 * 
	 * @param map
	 * @return true or false
	 */
	public static boolean isInitialized(Map<?,?> map) {
		return(map != null && map != Collections.EMPTY_MAP);
	}
}
