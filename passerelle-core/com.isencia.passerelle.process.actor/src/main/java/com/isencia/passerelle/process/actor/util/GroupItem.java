package com.isencia.passerelle.process.actor.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface GroupItem extends Serializable, Map<String, Object> {
	String getGroupName();

	String getValue(String property);

	Object getResult(String property);

	List<GroupItem> getGroupItems(String property);

	List<GroupItem> getAllChildren();

	void addChildren(String property, List<GroupItem> items);
	
	GroupItem getParent();

}
