package com.isencia.passerelle.process.actor.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.isencia.passerelle.process.model.ResultItem;

public class GroupItemImpl extends HashMap<String, Object> implements GroupItem {
	public GroupItemImpl(GroupItem parent, String name, Map<String, ResultItem> map) {
		super();
		this.groupName = name;
		this.parent = parent;
		for (Map.Entry<String, ResultItem> e : map.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	private String groupName;

	public String getGroupName() {
		return groupName;
	}

	public String getValue(String property) {
		if (get(property) == null) {
			return null;
		}
		if (get(property) instanceof ResultItem) {
			return ((ResultItem) get(property)).getValueAsString();
		}
		return get(property).toString();
	}

	public Object getResult(String property) {
		if (get(property) == null) {
			return null;
		}
		if (get(property) instanceof ResultItem) {
			return ((ResultItem) get(property)).getValue();
		}
		return get(property);
	}

	public List<GroupItem> getGroupItems(String property) {
		if (!(get(property) instanceof List)) {
			return Collections.EMPTY_LIST;
		}
		return (List<GroupItem>) get(property);
	}

	private GroupItem parent;

	public GroupItem getParent() {
		return parent;
	}

	public void setParent(GroupItem parent) {
		this.parent = parent;
	}

	private Map<String, List<GroupItem>> children = new HashMap<String, List<GroupItem>>();

	public List<GroupItem> getChildren(String property) {
		if (children.get(property) == null) {
			return null;
		}
		return children.get(property);
	}

	public void addChildren(String property, List<GroupItem> items) {
		children.put(property, items);
	}

	public Map<String, List<GroupItem>> getChildren() {
		return children;
	}

	public List<GroupItem> getAllChildren() {
		List<GroupItem> children = new ArrayList<GroupItem>();
		addAllChildren(this, children);
		return children;
	}

	private void addAllChildren(GroupItem parent, List<GroupItem> children) {
		if (!(parent instanceof GroupItemImpl)) {
			return;
		}
		GroupItemImpl parentImpl = (GroupItemImpl) parent;
		if (parentImpl.getChildren().isEmpty()) {
			return;
		}
		for (Map.Entry<String, List<GroupItem>> entry : parentImpl.getChildren().entrySet()) {

			if (entry.getValue() != null) {
				for (GroupItem gi : entry.getValue()) {
					children.add(gi);
					addAllChildren(gi, children);
				}
			}
		}

	}

}
